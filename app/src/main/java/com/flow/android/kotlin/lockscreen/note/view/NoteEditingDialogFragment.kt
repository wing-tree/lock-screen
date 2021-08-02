package com.flow.android.kotlin.lockscreen.note.view

import android.animation.LayoutTransition
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.color.widget.ColorPickerLayout
import com.flow.android.kotlin.lockscreen.databinding.FragmentNoteEditingDialogBinding
import com.flow.android.kotlin.lockscreen.datepicker.DatePickerDialogFragment
import com.flow.android.kotlin.lockscreen.note.checklist.adapter.ChecklistAdapter
import com.flow.android.kotlin.lockscreen.note.listener.ItemChangedListener
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.util.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoteEditingDialogFragment : BaseDialogFragment<FragmentNoteEditingDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentNoteEditingDialogBinding {
        return FragmentNoteEditingDialogBinding.inflate(inflater, container, false)
    }

    private object Duration {
        const val Short = 100L
        const val Medium = 150L
    }

    private var itemChangedListener: ItemChangedListener? = null

    private val simpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

    private val checklistAdapter : ChecklistAdapter by lazy { ChecklistAdapter(object :
        ChecklistAdapter.Listener {
        override fun onCancelClick(item: ChecklistItem) {
            val value = checklist.value ?: return

            value.remove(item)
            checklist.value = value
        }

        override fun onItemCheckedChange(item: ChecklistItem, isChecked: Boolean) {}
    }, isEditable = true) }

    private val checklist = MutableLiveData<ArrayList<ChecklistItem>>()
    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireContext()) }
    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            if (intent.action == Action.Date) {
                val date = intent.getSerializableExtra(Name.Date)

                if (date is Date) {
                    viewBinding.textViewDate.text = date.time.toDateString(simpleDateFormat)

                    val originalGregorianCalendar = GregorianCalendar.getInstance().apply {
                        timeInMillis = note()?.modifiedTime ?: currentTimeMillis
                    }

                    val gregorianCalendar = GregorianCalendar.getInstance().apply { time = date }

                    val originalYear = originalGregorianCalendar.get(GregorianCalendar.YEAR)
                    val originalMonth = originalGregorianCalendar.get(GregorianCalendar.MONTH)
                    val originalDayOfMonth = originalGregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH)

                    val year = gregorianCalendar.get(GregorianCalendar.YEAR)
                    val month = gregorianCalendar.get(GregorianCalendar.MONTH)
                    val dayOfMonth = gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH)

                    val noteModified = note()?.apply {
                        this.modifiedTime = date.time
                    } ?: return

                    if (year != originalYear)
                        setValue(noteModified)

                    if (month != originalMonth)
                        setValue(noteModified)

                    if (dayOfMonth != originalDayOfMonth)
                        setValue(noteModified)
                }
            }
        }
    }

    object Action {
        const val Date = "$PREFIX.Action.Date"
    }

    object Name {
        const val Date = "$PREFIX.Name.Date"
    }

    private val currentTimeMillis = System.currentTimeMillis()
    private var mode = Mode.Add
    private var selectedColor = 0

    private var originalNote: Note? = null
    private val note = MutableLiveData<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        parentFragment?.let {
            if (it is ItemChangedListener)
                this.itemChangedListener = it
        }

        val note = arguments?.getParcelable<Note>(KEY_NOTE)

        selectedColor = ContextCompat.getColor(requireContext(), R.color.white)

        mode = if (note == null) {
            setValue(createEmptyNote())
            Mode.Add
        } else {
            originalNote = note
            selectedColor = note.color
            setValue(originalNote?.deepCopy() ?: createEmptyNote())
            Mode.Edit
        }

        initializeViews(note)
        registerLifecycleObservers()
        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Date))

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.editText.requestFocus()
        with(requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
            toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    override fun onDestroyView() {
        with(requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
            toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)
        super.onDestroyView()
    }

    private fun initializeViews(note: Note?) {
        val checklistHeader = viewBinding.checklistHeader

        viewBinding.recyclerViewChecklist.apply {
            adapter = checklistAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        note?.let {
            viewBinding.viewColor.backgroundTintList = ColorStateList.valueOf(selectedColor)
            viewBinding.textViewDate.text = it.modifiedTime.toDateString(simpleDateFormat)
            viewBinding.editText.setText(it.content)

            checklist.value = ArrayList(it.checklist.toList())
        } ?: run {
            viewBinding.textViewDate.text = currentTimeMillis.toDateString(simpleDateFormat)
        }

        viewBinding.textViewDate.setOnClickListener {
            DatePickerDialogFragment.getInstance(note()?.modifiedTime ?: currentTimeMillis).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        checklistHeader.imageView.setOnClickListener {
            val content = checklistHeader.editText.text.toString()

            if (content.isNotBlank()) {
                val value = checklist.value ?: arrayListOf()
                val size = value.size

                value.add(size, ChecklistItem(content, size.toLong(), false))
                checklist.value = value
                checklistHeader.editText.text?.clear()
            }
        }

        viewBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                note().also {
                    it?.content = s.toString()
                    setValue(it)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewBinding.colorPickerLayout.select(selectedColor)
        viewBinding.colorPickerLayout.setOnColorSelectedListener(object :
            ColorPickerLayout.OnColorSelectedListener {
            override fun onColorSelected(color: Int) {
                viewBinding.viewColor.backgroundTintList = ColorStateList.valueOf(color)

                if (selectedColor == color)
                    return

                selectedColor = color

                note()?.let {
                    setValue(it.apply {
                        this.color = selectedColor
                    })
                }
            }
        })

        viewBinding.textViewCancel.setOnClickListener {
            dismiss()
        }

        viewBinding.textViewSave.setOnClickListener {
            if (checkForChanges()) {
                if (mode == Mode.Add)
                    note()?.let { note -> itemChangedListener?.onInsert(note) }
                else if (mode == Mode.Edit) {
                    note()?.let { note ->
                        localBroadcastManager.sendBroadcastSync(
                            Intent(NoteDetailDialogFragment.Action.Note).apply {
                                putExtra(NoteDetailDialogFragment.Name.Note, note)
                            }
                        )
                        itemChangedListener?.onUpdate(note)
                    }
                }
            }

            dismiss()
        }

        setupLayoutTransition()
    }

    private fun registerLifecycleObservers() {
        this.note.observe(viewLifecycleOwner, {
            if (checkForChanges()) {
                if (isContentBlank())
                    disableSaveButton()
                else
                    enableSaveButton()
            } else
                disableSaveButton()
        })

        checklist.observe(viewLifecycleOwner, {
            note()?.let { memo ->
                setValue(memo.apply {
                    checklist = it.toTypedArray()
                })
            }

            if (it.isNullOrEmpty()) {
                viewBinding.recyclerViewChecklist.hide()
                viewBinding.viewDividerBottom.hide(true)
                viewBinding.viewDividerTop.hide(true)
            } else {
                viewBinding.recyclerViewChecklist.show()

                if (viewBinding.viewDividerBottom.isVisible.not())
                    viewBinding.viewDividerBottom.fadeIn(Duration.Short)

                if (viewBinding.viewDividerTop.isVisible.not())
                    viewBinding.viewDividerTop.fadeIn(Duration.Short)
            }

            checklistAdapter.submitList(it.toList())
        })
    }

    private fun setupLayoutTransition() {
        requireDialog().window?.decorView?.let {
            if (it is ViewGroup) {
                it.layoutTransition = LayoutTransition()

                it.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                it.layoutTransition.setDuration(LayoutTransition.CHANGING, Duration.Medium)
                it.layoutTransition.setStartDelay(LayoutTransition.CHANGING, Duration.Medium)
            }
        }

        viewBinding.constraintLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        viewBinding.constraintLayout.layoutTransition.setDuration(
            LayoutTransition.CHANGING,
            Duration.Medium
        )
        viewBinding.constraintLayout.layoutTransition.setStartDelay(
            LayoutTransition.CHANGING,
            Duration.Medium
        )
        viewBinding.root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        viewBinding.root.layoutTransition.setDuration(LayoutTransition.CHANGING, Duration.Medium)
        viewBinding.root.layoutTransition.setStartDelay(LayoutTransition.CHANGING, Duration.Medium)
    }

    private fun enableSaveButton() {
        viewBinding.textViewSave.isEnabled = true
    }

    private fun disableSaveButton() {
        viewBinding.textViewSave.isEnabled = false
    }

    private fun isContentBlank() = viewBinding.editText.text.isNullOrBlank()

    private fun checkForChanges() : Boolean {
        val note = note() ?: return false
        val originalNote = this.originalNote ?: return true

        return originalNote.contentEquals(note).not()
    }

    private fun createEmptyNote(): Note {
        return Note(
            checklist = arrayOf(),
            content = BLANK,
            color = selectedColor,
            isDone = false,
            modifiedTime = currentTimeMillis,
            priority = currentTimeMillis
        )
    }

    private fun note() = note.value

    private fun setValue(value: Note?) {
        value?.let { this.note.value = it }
    }

    private object Mode {
        const val Add = 0
        const val Edit = 1
    }

    companion object {
        private const val PREFIX = "com.flow.android.kotlin.lockscreen.note.view" +
                ".MemoEditingDialogFragment.companion"

        private const val KEY_NOTE = "$PREFIX.KEY_NOTE"

        fun getInstance(note: Note?): NoteEditingDialogFragment {
            val instance = NoteEditingDialogFragment()

            instance.arguments = bundleOf(KEY_NOTE to note)

            return instance
        }
    }
}
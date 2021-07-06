package com.flow.android.kotlin.lockscreen.memo.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.color.widget.ColorPickerLayout
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoEditingDialogBinding
import com.flow.android.kotlin.lockscreen.datepicker.DatePickerDialogFragment
import com.flow.android.kotlin.lockscreen.memo._interface.OnMemoChangedListener
import com.flow.android.kotlin.lockscreen.memo.checklist.adapter.ChecklistAdapter
import com.flow.android.kotlin.lockscreen.persistence.data.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Memo
import com.flow.android.kotlin.lockscreen.util.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoEditingDialogFragment : BaseDialogFragment<FragmentMemoEditingDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoEditingDialogBinding {
        return FragmentMemoEditingDialogBinding.inflate(inflater, container, false)
    }

    private val simpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

    private val checklistAdapter : ChecklistAdapter by lazy { ChecklistAdapter(object : ChecklistAdapter.Listener {
        override fun onMoreClick(item: ChecklistItem) {
            val popupWindow = PopupWindow()
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
                        timeInMillis = memo()?.modifiedTime ?: currentTimeMillis
                    }

                    val gregorianCalendar = GregorianCalendar.getInstance().apply { time = date }

                    val originalYear = originalGregorianCalendar.get(GregorianCalendar.YEAR)
                    val originalMonth = originalGregorianCalendar.get(GregorianCalendar.MONTH)
                    val originalDayOfMonth = originalGregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH)

                    val year = gregorianCalendar.get(GregorianCalendar.YEAR)
                    val month = gregorianCalendar.get(GregorianCalendar.MONTH)
                    val dayOfMonth = gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH)

                    val memoModified = memo()?.apply {
                        this.modifiedTime = date.time
                    } ?: return

                    if (year != originalYear)
                        setMemo(memoModified)

                    if (month != originalMonth)
                        setMemo(memoModified)

                    if (dayOfMonth != originalDayOfMonth)
                        setMemo(memoModified)
                }
            }
        }
    }

    object Action {
        const val Date = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoEditingDialogFragment.Action.Date"
    }

    object Name {
        const val Date = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoEditingDialogFragment.Name.Date"
    }

    private val duration = 200
    private val currentTimeMillis = System.currentTimeMillis()
    private var mode = Mode.Add
    private var selectedColor = Color.WHITE

    private var originalMemo: Memo? = null
    private val memo = MutableLiveData<Memo>()

    private var onMemoChangedListener: OnMemoChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnMemoChangedListener)
            onMemoChangedListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val memo = arguments?.getParcelable<Memo>(KEY_MEMO)

        mode =
            if (memo == null) {
                setMemo(createEmptyMemo())
                Mode.Add
            } else {
                originalMemo = memo
                selectedColor = memo.color
                setMemo(originalMemo?.deepCopy() ?: createEmptyMemo())
                Mode.Edit
            }

        initializeView(memo)
        registerObservers()
        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Date))

        return view
    }

    override fun onDestroyView() {
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)

        super.onDestroyView()
    }

    private fun initializeView(memo: Memo?) {
        val checklistHeader = viewBinding.checklistHeader

        viewBinding.recyclerViewChecklist.apply {
            adapter = checklistAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        memo?.let {
            viewBinding.viewMemoColor.backgroundTintList = ColorStateList.valueOf(selectedColor)
            viewBinding.textViewDate.text = it.modifiedTime.toDateString(simpleDateFormat)
            viewBinding.editTextContent.setText(it.content)

            checklist.value = ArrayList(it.checklist.toList())
        } ?: run {
            viewBinding.textViewDate.text = currentTimeMillis.toDateString(simpleDateFormat)
        }

        viewBinding.textViewDate.setOnClickListener {
            DatePickerDialogFragment.getInstance(memo()?.modifiedTime ?: currentTimeMillis).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        checklistHeader.imageView.setOnClickListener {
            val content = checklistHeader.editText.text.toString()

            if (content.isNotBlank()) {
                val value = checklist.value ?: arrayListOf()
                val size = value.size

                value.add(size, ChecklistItem(size.toLong(), content, false))
                checklist.value = value
                checklistHeader.editText.text?.clear()
            }
        }

        viewBinding.editTextContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memo().also {
                    it?.content = s.toString()
                    setMemo(it)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // recycler view item 마다 변화 감지.

        viewBinding.imageViewDetail.setOnClickListener {
            // todo show recyclerview.
        }

        viewBinding.colorPickerLayout.select(selectedColor)
        viewBinding.colorPickerLayout.setOnColorSelectedListener(object :
            ColorPickerLayout.OnColorSelectedListener {
            override fun onColorSelected(color: Int) {
                viewBinding.viewMemoColor.backgroundTintList = ColorStateList.valueOf(color)

                if (selectedColor == color)
                    return

                selectedColor = color

                memo()?.let {
                    setMemo(it.apply {
                        this.color = selectedColor
                    })
                }
            }
        })

        viewBinding.materialButtonClose.setOnClickListener {
            dismiss()
        }

        viewBinding.materialButtonSave.setOnClickListener {
            if (checkForChanges()) {
                if (mode == Mode.Add)
                    memo()?.let { memo -> onMemoChangedListener?.onMemoInserted(memo) }
                else if (mode == Mode.Edit) {
                    memo()?.let { memo ->
                        localBroadcastManager.sendBroadcastSync(
                            Intent(MemoDetailDialogFragment.Action.Memo).apply {
                                putExtra(MemoDetailDialogFragment.Name.Memo, memo)
                            }
                        )
                        onMemoChangedListener?.onMemoUpdated(memo)
                    }
                }
            }

            dismiss()
        }
    }

    private fun registerObservers() {
        this.memo.observe(viewLifecycleOwner, {
            if (checkForChanges()) {
                if (isContentBlank())
                    disableSaveButton()
                else
                    enableSaveButton()
            } else
                disableSaveButton()
        })

        checklist.observe(viewLifecycleOwner, {
            memo()?.let { memo ->
                setMemo(memo.apply {
                    this.checklist = it.toTypedArray()
                })
            }

            checklistAdapter.submitList(it.toList())
        })
    }

    private fun enableSaveButton() {
        viewBinding.materialButtonSave.isEnabled = true
    }

    private fun disableSaveButton() {
        viewBinding.materialButtonSave.isEnabled = false
    }

    private fun isContentBlank() = viewBinding.editTextContent.text.isNullOrBlank()

    private fun checkForChanges() : Boolean {
        val memo = memo() ?: return false
        val originalMemo = this.originalMemo ?: return true

        return originalMemo.contentEquals(memo).not()
    }

    private fun createEmptyMemo(): Memo {
        return Memo(
            checklist = arrayOf(),
            content = BLANK,
            color = selectedColor,
            isDone = false,
            modifiedTime = currentTimeMillis,
            priority = currentTimeMillis
        )
    }

    private fun memo() = memo.value

    private fun setMemo(value: Memo?) {
        value?.let { this.memo.value = it }
    }

    private object Mode {
        const val Add = 0
        const val Edit = 1
    }

    companion object {
        private const val KEY_MEMO = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoEditingDialogFragment.companion.KEY_MEMO"

        fun getInstance(memo: Memo?): MemoEditingDialogFragment {
            val instance = MemoEditingDialogFragment()

            instance.arguments = bundleOf(KEY_MEMO to memo)

            return instance
        }
    }
}
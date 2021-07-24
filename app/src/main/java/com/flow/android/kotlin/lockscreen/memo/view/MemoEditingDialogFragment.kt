package com.flow.android.kotlin.lockscreen.memo.view

import android.animation.Animator
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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.color.widget.ColorPickerLayout
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoEditingDialogBinding
import com.flow.android.kotlin.lockscreen.datepicker.DatePickerDialogFragment
import com.flow.android.kotlin.lockscreen.memo.checklist.adapter.ChecklistAdapter
import com.flow.android.kotlin.lockscreen.memo.viewmodel.MemoViewModel
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.util.*
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoEditingDialogFragment : BaseDialogFragment<FragmentMemoEditingDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoEditingDialogBinding {
        return FragmentMemoEditingDialogBinding.inflate(inflater, container, false)
    }

    private val viewModel by activityViewModels<MemoViewModel>()
    private val duration = 150L

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
                        setValue(memoModified)

                    if (month != originalMonth)
                        setValue(memoModified)

                    if (dayOfMonth != originalDayOfMonth)
                        setValue(memoModified)
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

    private var originalMemo: Memo? = null
    private val memo = MutableLiveData<Memo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val memo = arguments?.getParcelable<Memo>(KEY_MEMO)

        selectedColor = ContextCompat.getColor(requireContext(), R.color.white)

        mode = if (memo == null) {
            setValue(createEmptyMemo())
            Mode.Add
        } else {
            originalMemo = memo
            selectedColor = memo.color
            setValue(originalMemo?.deepCopy() ?: createEmptyMemo())
            Mode.Edit
        }

        initializeViews(memo)
        registerLifecycleObservers()
        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Date))

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.editText.requestFocus()
    }

    override fun onDestroyView() {
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)
        super.onDestroyView()
    }

    private fun initializeViews(memo: Memo?) {
        val checklistHeader = viewBinding.checklistHeader

        viewBinding.recyclerViewChecklist.apply {
            adapter = checklistAdapter
            //itemAnimator = null
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        memo?.let {
            viewBinding.viewColor.backgroundTintList = ColorStateList.valueOf(selectedColor)
            viewBinding.textViewDate.text = it.modifiedTime.toDateString(simpleDateFormat)
            viewBinding.editText.setText(it.content)

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

                value.add(size, ChecklistItem(content, size.toLong(), false))
                checklist.value = value
                checklistHeader.editText.text?.clear()
            }
        }

        viewBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memo().also {
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

                memo()?.let {
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
                    memo()?.let { memo -> viewModel.insert(memo) }
                else if (mode == Mode.Edit) {
                    memo()?.let { memo ->
                        localBroadcastManager.sendBroadcastSync(
                            Intent(MemoDetailDialogFragment.Action.Memo).apply {
                                putExtra(MemoDetailDialogFragment.Name.Memo, memo)
                            }
                        )
                        viewModel.update(memo)
                    }
                }
            }

            dismiss()
        }

        setupLayoutTransition()
    }

    private fun registerLifecycleObservers() {
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
                    viewBinding.viewDividerBottom.fadeIn(duration)

                if (viewBinding.viewDividerTop.isVisible.not())
                    viewBinding.viewDividerTop.fadeIn(duration)
            }

            checklistAdapter.submitList(it.toList())
        })
    }

    private fun setupLayoutTransition() {
        requireDialog().window?.decorView?.let {
            if (it is ViewGroup) {
                it.layoutTransition = LayoutTransition()

                it.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                it.layoutTransition.setDuration(LayoutTransition.CHANGING, duration)
                it.layoutTransition.setStartDelay(LayoutTransition.CHANGING, duration)
            }
        }

        viewBinding.constraintLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        viewBinding.constraintLayout.layoutTransition.setDuration(LayoutTransition.CHANGING, duration)
        viewBinding.constraintLayout.layoutTransition.setStartDelay(LayoutTransition.CHANGING, duration)
        viewBinding.root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        viewBinding.root.layoutTransition.setDuration(LayoutTransition.CHANGING, duration)
        viewBinding.root.layoutTransition.setStartDelay(LayoutTransition.CHANGING, duration)
    }

    private fun enableSaveButton() {
        viewBinding.textViewSave.isEnabled = true
    }

    private fun disableSaveButton() {
        viewBinding.textViewSave.isEnabled = false
    }

    private fun isContentBlank() = viewBinding.editText.text.isNullOrBlank()

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

    private fun setValue(value: Memo?) {
        value?.let { this.memo.value = it }
    }

    private object Mode {
        const val Add = 0
        const val Edit = 1
    }

    companion object {
        private const val PREFIX = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoEditingDialogFragment.companion"

        private const val KEY_MEMO = "$PREFIX.KEY_MEMO"

        fun getInstance(memo: Memo?): MemoEditingDialogFragment {
            val instance = MemoEditingDialogFragment()

            instance.arguments = bundleOf(KEY_MEMO to memo)

            return instance
        }
    }
}
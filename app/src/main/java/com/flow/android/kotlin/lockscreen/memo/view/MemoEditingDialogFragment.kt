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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.color.widget.ColorPickerLayout
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoEditingDialogBinding
import com.flow.android.kotlin.lockscreen.datepicker.DatePickerDialogFragment
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.memo._interface.OnMemoChangedListener
import com.flow.android.kotlin.lockscreen.util.*
import java.text.SimpleDateFormat
import java.util.*

class MemoEditingDialogFragment : BaseDialogFragment<FragmentMemoEditingDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoEditingDialogBinding {
        return FragmentMemoEditingDialogBinding.inflate(inflater, container, false)
    }

    private val simpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

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

        this.memo.observe(viewLifecycleOwner, {
            if (checkForChanges()) {
                if (isContentBlank())
                    disableMaterialButtonSave()
                else
                    enableMaterialButtonSave()
            } else
                disableMaterialButtonSave()
        })

        initializeView(memo)
        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Date))

        return view
    }

    override fun onStart() {
        super.onStart()

        if (mode == Mode.Edit)
            dialog?.window?.setWindowAnimations(R.style.WindowAnimation_DialogFragment_Null)
    }

    override fun onDestroyView() {
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)

        super.onDestroyView()
    }

    private fun initializeView(memo: Memo?) {
        memo?.let {
            viewBinding.viewMemoColor.backgroundTintList = ColorStateList.valueOf(selectedColor)
            viewBinding.textViewDate.text = it.modifiedTime.toDateString(simpleDateFormat)
            viewBinding.editTextContent.setText(it.content)

            if (it.detail.isNotBlank()) {
                viewBinding.editTextDetail.show()
                viewBinding.editTextDetail.setText(it.detail)
            }
        } ?: run {
            viewBinding.textViewDate.text = currentTimeMillis.toDateString(simpleDateFormat)
        }

        viewBinding.textViewDate.setOnClickListener {
            DatePickerDialogFragment.getInstance(memo()?.modifiedTime ?: currentTimeMillis).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
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

        viewBinding.editTextDetail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memo().also {
                    it?.detail = s.toString()
                    setMemo(it)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewBinding.imageViewDetail.setOnClickListener {
            if (viewBinding.editTextDetail.isVisible) {
                viewBinding.editTextDetail.fadeOut(duration)
                viewBinding.editTextContent.setBackgroundResource(R.drawable.rounded_corners)
            } else {
                viewBinding.editTextContent.setBackgroundResource(R.drawable.rounded_corners_top)
                viewBinding.editTextDetail.visibility = View.INVISIBLE
                viewBinding.editTextDetail.fadeIn(duration)
            }
        }

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

        viewBinding.colorPickerLayout.post {
            viewBinding.colorPickerLayout.select(selectedColor)
        }

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

    private fun enableMaterialButtonSave() {
        viewBinding.materialButtonSave.isEnabled = true
    }

    private fun disableMaterialButtonSave() {
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
            content = BLANK,
            color = selectedColor,
            detail = BLANK,
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
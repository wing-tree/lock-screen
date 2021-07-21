package com.flow.android.kotlin.lockscreen.datepicker

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentDatePickerDialogBinding
import com.flow.android.kotlin.lockscreen.memo.view.MemoEditingDialogFragment
import java.util.*

class DatePickerDialogFragment: BaseDialogFragment<FragmentDatePickerDialogBinding>() {
    private lateinit var date: Date

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentDatePickerDialogBinding {
        return FragmentDatePickerDialogBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val timeInMillis = arguments?.getLong(KEY_TIME_IN_MILLIS) ?: System.currentTimeMillis()

        val calendar: Calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
        }

        date = calendar.time

        viewBinding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            date = GregorianCalendar(year, monthOfYear, dayOfMonth).time
        }

        viewBinding.textViewClose.setOnClickListener {
            dismiss()
        }

        viewBinding.textViewConfirm.setOnClickListener {
            val intent = Intent(MemoEditingDialogFragment.Action.Date).apply {
                putExtra(MemoEditingDialogFragment.Name.Date, date)
            }

            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    companion object {
        private const val KEY_TIME_IN_MILLIS = "com.flow.android.kotlin.lockscreen.datepicker" +
                ".DatePickerDialogFragment.companion.KEY_TIME_IN_MILLIS"

        fun getInstance(timeInMillis: Long): DatePickerDialogFragment {
            val instance = DatePickerDialogFragment()

            instance.arguments = bundleOf(KEY_TIME_IN_MILLIS to timeInMillis)

            return instance
        }
    }
}
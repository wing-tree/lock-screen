package com.flow.android.kotlin.lockscreen.memo.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoDetailDialogBinding
import com.flow.android.kotlin.lockscreen.memo.listener.OnMemoChangedListener
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import com.flow.android.kotlin.lockscreen.util.toDateString
import java.text.SimpleDateFormat
import java.util.*

class MemoDetailDialogFragment : DialogFragment() {
    private lateinit var viewBinding: FragmentMemoDetailDialogBinding
    private val simpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

    private var onMemoChangedListener: OnMemoChangedListener? = null

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireContext()) }
    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            if (intent.action == Action.Memo) {
                val memo = intent.getParcelableExtra<Memo>(Name.Memo) ?: return

                initializeView(memo)
            }
        }
    }

    object Action {
        const val Memo = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoDetailDialogFragment.Action.Memo"
    }

    object Name {
        const val Memo = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoDetailDialogFragment.Name.Memo"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnMemoChangedListener)
            onMemoChangedListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentMemoDetailDialogBinding.inflate(inflater, container, false)

        val memo = arguments?.getParcelable<Memo>(KEY_MEMO)

        memo?.let {
            initializeView(it)
        } ?: run {
            // show error.
        }

        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Memo))

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return viewBinding.root
    }

    override fun onDestroyView() {
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)

        super.onDestroyView()
    }

    private fun initializeView(memo: Memo) {
        viewBinding.textViewDate.text = memo.modifiedTime.toDateString(simpleDateFormat)
        viewBinding.textViewContent.text = memo.content

        viewBinding.imageViewDelete.setOnClickListener {
            onMemoChangedListener?.onMemoDeleted(memo)
            dismiss()
        }

        viewBinding.imageViewShare.setOnClickListener {

        }

        viewBinding.imageViewCalendar.setOnClickListener {
            addToCalendar(memo)
        }

        viewBinding.imageViewEdit.setOnClickListener {
            MemoEditingDialogFragment.getInstance(memo).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.materialButtonClose.setOnClickListener {
            dismiss()
        }

        viewBinding.materialButtonDone.setOnClickListener {
            onMemoChangedListener?.onMemoUpdated(memo.apply { isDone = isDone.not() })
        }
    }

    private fun addToCalendar(memo: Memo) {
        val gregorianCalendar = GregorianCalendar.getInstance().apply {
            timeInMillis = memo.modifiedTime
        }

        val year = gregorianCalendar.get(GregorianCalendar.YEAR)
        val month = gregorianCalendar.get(GregorianCalendar.MONTH)
        val dayOfMonth = gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH)
        val hourOfDay = gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)

        val startMillis: Long = Calendar.getInstance().run {
            set(year, month, dayOfMonth, hourOfDay, 0)
            timeInMillis
        }

        val endMillis: Long = Calendar.getInstance().run {
            set(year, month, dayOfMonth, hourOfDay + 1, 0)
            timeInMillis
        }

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.TITLE, memo.content)
            .putExtra(CalendarContract.Events.DESCRIPTION, memo.detail)
            .putExtra(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_BUSY
            )

        intent.resolveActivity(requireContext().packageManager)?.let {
            startActivity(intent)
        } ?: run {
            // todo show toast.
        }
    }

    companion object {
        private const val KEY_MEMO = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoDetailDialogFragment.companion.KEY_MEMO"

        fun getInstance(memo: Memo): MemoDetailDialogFragment {
            val instance = MemoDetailDialogFragment()

            instance.arguments = bundleOf(KEY_MEMO to memo)

            return instance
        }
    }
}
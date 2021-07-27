package com.flow.android.kotlin.lockscreen.memo.view

import android.content.*
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.CalendarContract
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoDetailDialogBinding
import com.flow.android.kotlin.lockscreen.memo.checklist.adapter.ChecklistAdapter
import com.flow.android.kotlin.lockscreen.memo.util.share
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import com.flow.android.kotlin.lockscreen.util.show
import com.flow.android.kotlin.lockscreen.util.toDateString
import com.flow.android.kotlin.lockscreen.util.view.ConfirmationDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class MemoDetailDialogFragment : BaseDialogFragment<FragmentMemoDetailDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentMemoDetailDialogBinding {
        return FragmentMemoDetailDialogBinding.inflate(inflater, container, false)
    }

    private val viewModel by lazy { mainViewModel.memoViewModel }
    private var memo: Memo? = null

    private val checklist = MutableLiveData<ArrayList<ChecklistItem>>()
    private val checklistAdapter = ChecklistAdapter(object : ChecklistAdapter.Listener {
        override fun onCancelClick(item: ChecklistItem) {}

        override fun onItemCheckedChange(item: ChecklistItem, isChecked: Boolean) {
            val checklistItem = memo?.checklist?.find { it.id == item.id } ?: return
            val index = memo?.checklist?.indexOf(checklistItem) ?: return

            memo?.checklist?.set(index, checklistItem.apply { isDone = isChecked })

            memo?.let { viewModel.update(it) {
                val value = checklist.value ?: return@update

                checklist.value = value.apply { set(index, checklistItem) }
            } }
        }
    }, isEditable = false)

    private val simpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())
    }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireContext()) }
    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            if (intent.action == Action.Memo) {
                memo = intent.getParcelableExtra(Name.Memo) ?: return
                memo?.let { initializeViews(it) }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        memo = arguments?.getParcelable(KEY_MEMO)

        memo?.let {
            initializeViews(it)
            registerLifecycleObservers()
        } ?: run {
            // show error.
        }

        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Memo))

        return view
    }

    override fun onDestroyView() {
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)
        super.onDestroyView()
    }

    private fun initializeViews(memo: Memo) {
        viewBinding.viewColor.backgroundTintList = ColorStateList.valueOf(memo.color)
        viewBinding.textViewDate.text = memo.modifiedTime.toDateString(simpleDateFormat)
        viewBinding.textViewContent.movementMethod = ScrollingMovementMethod()
        viewBinding.textViewContent.text = memo.content

        if (memo.checklist.isNotEmpty()) {
            viewBinding.recyclerViewChecklist.show()
            viewBinding.recyclerViewChecklist.apply {
                adapter = checklistAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext())
            }

            viewBinding.viewDividerBottom.show()
            viewBinding.viewDividerTop.show()

            checklist.value = ArrayList(memo.checklist.toList())
        }

        viewBinding.imageViewDelete.setOnClickListener {
            ConfirmationDialogFragment().also {
                it.setTitle(getString(R.string.memo_detail_dialog_fragment_005))
                it.setMessage(getString(R.string.memo_detail_dialog_fragment_002))
                it.setNegativeButton(getString(R.string.memo_detail_dialog_fragment_003)) { dialogFragment ->
                    dialogFragment.dismiss()
                }

                it.setPositiveButton(getString(R.string.memo_detail_dialog_fragment_004)) { dialogFragment ->
                    viewModel.delete(memo)
                    dialogFragment.dismiss()
                    this.dismiss()
                }

                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.imageViewEdit.setOnClickListener {
            MemoEditingDialogFragment.getInstance(memo).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.imageViewMoreVert.setOnClickListener {
            val items = resources.getStringArray(R.array.memo_detail_dialog_fragment_005)

            MaterialAlertDialogBuilder(requireContext()).setItems(items) { _: DialogInterface, i: Int ->
                when(i) {
                    0 -> insertToCalendar(memo)
                    1 -> share(requireContext(), memo)
                }
            }.show()
        }

        viewBinding.textViewClose.setOnClickListener {
            dismiss()
        }

        viewBinding.textViewDone.setOnClickListener {
            viewModel.update(memo.apply {
                isDone = isDone.not()
            })

            dismiss()
        }
    }

    private fun registerLifecycleObservers() {
        checklist.observe(viewLifecycleOwner, { list ->
            checklistAdapter.submitList(list.map { it.deepCopy() })
        })
    }

    private fun insertToCalendar(memo: Memo) {
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
            .putExtra(CalendarContract.Events.DESCRIPTION, memo.checkListToString())
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
        private const val PREFIX = "com.flow.android.kotlin.lockscreen.memo.view" +
                ".MemoDetailDialogFragment.companion"
        private const val KEY_MEMO = "$PREFIX.KEY_MEMO"

        fun getInstance(memo: Memo): MemoDetailDialogFragment {
            val instance = MemoDetailDialogFragment()

            instance.arguments = bundleOf(KEY_MEMO to memo)

            return instance
        }
    }
}
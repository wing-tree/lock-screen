package com.flow.android.kotlin.lockscreen.note.view

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
import com.flow.android.kotlin.lockscreen.databinding.FragmentNoteDetailDialogBinding
import com.flow.android.kotlin.lockscreen.note.checklist.adapter.ChecklistAdapter
import com.flow.android.kotlin.lockscreen.note.listener.ItemChangedListener
import com.flow.android.kotlin.lockscreen.note.util.share
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import com.flow.android.kotlin.lockscreen.util.show
import com.flow.android.kotlin.lockscreen.util.toDateString
import com.flow.android.kotlin.lockscreen.util.view.ConfirmationDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class NoteDetailDialogFragment : BaseDialogFragment<FragmentNoteDetailDialogBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentNoteDetailDialogBinding {
        return FragmentNoteDetailDialogBinding.inflate(inflater, container, false)
    }

    private var note: Note? = null
    private var itemChangedListener: ItemChangedListener? = null

    private val checklist = MutableLiveData<ArrayList<ChecklistItem>>()
    private val checklistAdapter = ChecklistAdapter(object : ChecklistAdapter.Listener {
        override fun onCancelClick(item: ChecklistItem) {}

        override fun onItemCheckedChange(item: ChecklistItem, isChecked: Boolean) {
            val checklistItem = note?.checklist?.find { it.id == item.id } ?: return
            val index = note?.checklist?.indexOf(checklistItem) ?: return

            note?.checklist?.set(index, checklistItem.apply { isDone = isChecked })

            note?.let { itemChangedListener?.onUpdate(it) {
                val value = checklist.value ?: return@onUpdate

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

            if (intent.action == Action.Note) {
                note = intent.getParcelableExtra(Name.Note) ?: return
                note?.let { initializeViews(it) }
            }
        }
    }

    object Action {
        const val Note = "com.flow.android.kotlin.lockscreen.note.view" +
                ".NoteDetailDialogFragment.Action.Note"
    }

    object Name {
        const val Note = "com.flow.android.kotlin.lockscreen.note.view" +
                ".NoteDetailDialogFragment.Name.Note"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        parentFragment?.let {
            if (it is ItemChangedListener)
                this.itemChangedListener = it
        }

        note = arguments?.getParcelable(KEY_NOTE)

        note?.let {
            initializeViews(it)
            registerLifecycleObservers()
        } ?: run {
            // show error.
        }

        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter(Action.Note))

        return view
    }

    override fun onDestroyView() {
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver)
        super.onDestroyView()
    }

    private fun initializeViews(note: Note) {
        viewBinding.viewColor.backgroundTintList = ColorStateList.valueOf(note.color)
        viewBinding.textViewDate.text = note.modifiedTime.toDateString(simpleDateFormat)
        viewBinding.textViewContent.movementMethod = ScrollingMovementMethod()
        viewBinding.textViewContent.text = note.content

        if (note.checklist.isNotEmpty()) {
            viewBinding.recyclerViewChecklist.show()
            viewBinding.recyclerViewChecklist.apply {
                adapter = checklistAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext())
            }

            viewBinding.viewDividerBottom.show()
            viewBinding.viewDividerTop.show()

            checklist.value = ArrayList(note.checklist.toList())
        }

        viewBinding.imageViewDelete.setOnClickListener {
            ConfirmationDialogFragment().also {
                it.setTitle(getString(R.string.note_detail_dialog_fragment_004))
                it.setMessage(getString(R.string.note_detail_dialog_fragment_005))
                it.setNegativeButton(getString(R.string.note_detail_dialog_fragment_001)) { dialogFragment ->
                    dialogFragment.dismiss()
                }

                it.setPositiveButton(getString(R.string.note_detail_dialog_fragment_003)) { dialogFragment ->
                    itemChangedListener?.onDelete(note)
                    dialogFragment.dismiss()
                    this.dismiss()
                }

                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.imageViewEdit.setOnClickListener {
            NoteEditingDialogFragment.getInstance(note).also {
                it.show(requireActivity().supportFragmentManager, it.tag)
            }
        }

        viewBinding.imageViewMoreVert.setOnClickListener {
            val items = resources.getStringArray(R.array.note_detail_dialog_fragment_007)

            MaterialAlertDialogBuilder(requireContext()).setItems(items) { _: DialogInterface, i: Int ->
                when(i) {
                    0 -> insertToCalendar(note)
                    1 -> share(requireContext(), note)
                }
            }.show()
        }

        viewBinding.textViewClose.setOnClickListener {
            dismiss()
        }

        viewBinding.textViewDone.setOnClickListener {
            itemChangedListener?.onUpdate(note.apply {
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

    private fun insertToCalendar(note: Note) {
        val gregorianCalendar = GregorianCalendar.getInstance().apply {
            timeInMillis = note.modifiedTime
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
            .putExtra(CalendarContract.Events.TITLE, note.content)
            .putExtra(CalendarContract.Events.DESCRIPTION, note.checkListToString())
            .putExtra(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_BUSY
            )

        intent.resolveActivity(requireContext().packageManager)?.let {
            startActivity(intent)
        } ?: run {
            showToast(getString(R.string.note_detail_dialog_fragment_000))
        }
    }

    companion object {
        private const val PREFIX = "com.flow.android.kotlin.lockscreen.note.view" +
                ".NoteDetailDialogFragment.companion"
        private const val KEY_NOTE = "$PREFIX.KEY_NOTE"

        fun getInstance(note: Note): NoteDetailDialogFragment {
            val instance = NoteDetailDialogFragment()

            instance.arguments = bundleOf(KEY_NOTE to note)

            return instance
        }
    }
}
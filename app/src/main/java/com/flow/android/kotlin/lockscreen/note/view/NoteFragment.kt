package com.flow.android.kotlin.lockscreen.note.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.base.DataChanged
import com.flow.android.kotlin.lockscreen.base.DataChangedState
import com.flow.android.kotlin.lockscreen.databinding.FragmentNoteBinding
import com.flow.android.kotlin.lockscreen.eventbus.EventBus
import com.flow.android.kotlin.lockscreen.main.viewmodel.Refresh
import com.flow.android.kotlin.lockscreen.note.adapter.ItemTouchCallback
import com.flow.android.kotlin.lockscreen.note.adapter.NoteAdapter
import com.flow.android.kotlin.lockscreen.note.listener.ItemChangedListener
import com.flow.android.kotlin.lockscreen.note.viewmodel.NoteViewModel
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class NoteFragment: BaseMainFragment<FragmentNoteBinding>(), ItemChangedListener {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentNoteBinding {
        return FragmentNoteBinding.inflate(inflater, container, false)
    }

    private val viewModel by viewModels<NoteViewModel>()
    private val compositeDisposable = CompositeDisposable()
    private val publishSubject = PublishSubject.create<DataChanged<Note>>()

    private val adapter: NoteAdapter by lazy {
        NoteAdapter(object : NoteAdapter.Listener {
            override fun onItemClick(item: Note) {
                NoteDetailDialogFragment.getInstance(item.deepCopy()).also {
                    it.show(childFragmentManager, it.tag)
                }
            }

            override fun onSwapIconTouch(viewHolder: NoteAdapter.ViewHolder) {
                itemTouchHelper.startDrag(viewHolder)
            }
        })
    }

    private val itemTouchHelper by lazy { ItemTouchHelper(ItemTouchCallback(adapter) {
        viewModel.updateAll(adapter.currentList())
    }) }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        initializeViews()
        initializeData()
        subscribeObservables()
        firstRun()

        return viewBinding.root
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    private fun initializeViews() {
        viewBinding.appCompatImageView.setOnClickListener {
            NoteEditingDialogFragment.getInstance(null).also {
                it.show(childFragmentManager, it.tag)
            }
        }

        viewBinding.recyclerView.apply {
            adapter = this@NoteFragment.adapter
            itemAnimator = FadeInDownAnimator()
            layoutManager = LinearLayoutManagerWrapper(requireContext())
            scheduleLayoutAnimation()
        }

        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
    }

    private fun initializeData() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAll().also {
                withContext(Dispatchers.Main) {
                    adapter.addAll(it)
                }
            }
        }
    }

    private fun subscribeObservables() {
        compositeDisposable.add(publishSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when(it.state) {
                        DataChangedState.Deleted -> adapter.remove(it.data)
                        DataChangedState.Inserted -> {
                            adapter.add(it.data)
                            viewBinding.recyclerView.scrollToPosition(0)
                        }
                        DataChangedState.Updated -> adapter.update(it.data)
                    }
                }) {
                    Timber.e(it)
                }
        )

        compositeDisposable.add(
            EventBus.getInstance().subscribe({
                if (it == Refresh.Note)
                    adapter.refresh()
            }) {
                Timber.e(it)
            }
        )
    }

    private fun firstRun() {
        if (Preference.getFirstRun(requireContext())) {
            val note = Note(
                content = getString(R.string.note_fragment_000),
                color = ContextCompat.getColor(requireContext(), R.color.white),
                id = -20210513L,
                modifiedTime = System.currentTimeMillis(),
                priority = System.currentTimeMillis()
            )

            viewModel.insert(note) {
                publishSubject.onNext(DataChanged(note, DataChangedState.Inserted))
            }

            Preference.putFirstRun(requireContext(), false)
        }
    }

    override fun onDelete(note: Note) {
        viewModel.delete(note) { publishSubject.onNext(DataChanged(note, DataChangedState.Deleted)) }
    }

    override fun onInsert(note: Note) {
        viewModel.insert(note) { publishSubject.onNext(DataChanged(note, DataChangedState.Inserted)) }
    }

    override fun onUpdate(note: Note, onComplete: (() -> Unit)?) {
        viewModel.update(note) {
            onComplete?.invoke()
            publishSubject.onNext(DataChanged(note, DataChangedState.Updated))
        }
    }
}
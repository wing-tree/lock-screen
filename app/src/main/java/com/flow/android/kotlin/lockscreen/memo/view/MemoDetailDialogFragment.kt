package com.flow.android.kotlin.lockscreen.memo.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.FragmentMemoDetailDialogBinding
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import com.flow.android.kotlin.lockscreen.util.toDateString
import java.text.SimpleDateFormat
import java.util.*

class MemoDetailDialogFragment: DialogFragment() {
    private lateinit var viewBinding: FragmentMemoDetailDialogBinding

    private var onMemoChangedListener: OnMemoChangedListener? = null

    interface OnMemoChangedListener {
        fun onMemoDeleted(memo: Memo)
        fun onMemoDone(memo: Memo)
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
        val simpleDateFormat = SimpleDateFormat(getString(R.string.format_date_001), Locale.getDefault())

        viewBinding.textViewDate.text = memo?.modifiedTime?.toDateString(simpleDateFormat)
        viewBinding.textViewContent.text = memo?.content

        viewBinding.materialButtonClose.setOnClickListener {
            dismiss()
        }

        viewBinding.materialButtonDone.setOnClickListener {
            memo?.let { memo -> onMemoChangedListener?.onMemoDone(memo.apply { isDone = isDone.not() }) }
                    ?: run {
                        // show toast for error.
                        dismiss()
                    }
        }

        return viewBinding.root
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
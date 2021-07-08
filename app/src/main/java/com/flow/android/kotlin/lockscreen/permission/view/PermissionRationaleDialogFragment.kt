package com.flow.android.kotlin.lockscreen.permission.view

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentPermissionRationaleDialogBinding
import com.flow.android.kotlin.lockscreen.permission.PermissionChecker
import com.flow.android.kotlin.lockscreen.permission._interface.OnPermissionAllowClickListener
import com.flow.android.kotlin.lockscreen.permission.adapter.PermissionRationaleAdapter

@RequiresApi(Build.VERSION_CODES.M)
class PermissionRationaleDialogFragment: BaseDialogFragment<FragmentPermissionRationaleDialogBinding>() {
    private val permissionsDenied = mutableListOf<PermissionRationale>()
    private var onPermissionAllowClickListener: OnPermissionAllowClickListener? = null

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentPermissionRationaleDialogBinding {
        return FragmentPermissionRationaleDialogBinding.inflate(inflater, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnPermissionAllowClickListener)
            onPermissionAllowClickListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val permissions = arrayOf(
                PermissionRationale(
                        icon = R.drawable.ic_round_today_24,
                        permission = Manifest.permission.READ_CALENDAR,
                        permissionName = getString(R.string.permission_rationale_dialog_fragment_000),
                        rationale = getString(R.string.permission_rationale_dialog_fragment_001)
                ),
                PermissionRationale(
                        icon = 0,
                        permission = Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        permissionName = getString(R.string.permission_rationale_dialog_fragment_004),
                        rationale = getString(R.string.permission_rationale_dialog_fragment_005)
                )
        )

        for (permission in permissions) {
            if (permission.permission == Settings.ACTION_MANAGE_OVERLAY_PERMISSION) {
                if (Settings.canDrawOverlays(requireContext()).not())
                    permissionsDenied.add(permission)

                continue
            }

            if (PermissionChecker.checkPermission(requireContext(), permission.permission))
                continue

            permissionsDenied.add(permission)
        }

        viewBinding.recyclerView.apply {
            adapter = PermissionRationaleAdapter(permissionsDenied)
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewBinding.materialButtonAllow.setOnClickListener {
            onPermissionAllowClickListener?.onPermissionAllowClick()
            dismiss()
        }

        viewBinding.materialButtonDeny.setOnClickListener {
            dismiss()
        }

        return view
    }

    companion object {
        fun permissionsGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionChecker.checkPermission(context, Manifest.permission.READ_CALENDAR).not())
                    return false

                if (Settings.canDrawOverlays(context).not())
                    return false

                return true
            } else
                return true
        }
    }
}

data class PermissionRationale(
        @DrawableRes
        val icon: Int,
        val permission: String,
        val permissionName: String,
        val rationale: String
)
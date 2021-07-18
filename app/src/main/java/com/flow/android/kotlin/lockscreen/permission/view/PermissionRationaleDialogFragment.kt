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
import com.flow.android.kotlin.lockscreen.permission.adapter.PermissionAdapter
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

@RequiresApi(Build.VERSION_CODES.M)
class PermissionRationaleDialogFragment: BaseDialogFragment<FragmentPermissionRationaleDialogBinding>() {
    private val permissionsDenied = mutableListOf<Permission>()
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
                Permission(
                        icon = R.drawable.ic_mobile_48px,
                        isRequired = true,
                        permissions = arrayOf(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                        permissionName = getString(R.string.permission_rationale_dialog_fragment_004),
                ),
                Permission(
                        icon = R.drawable.ic_round_calendar_today_24,
                        isRequired = false,
                        permissions = arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                        ),
                        permissionName = getString(R.string.permission_rationale_dialog_fragment_000)
                )
        )

        for (permission in permissions) {
            if (permission.permissions.contains(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) {
                if (Settings.canDrawOverlays(requireContext()).not())
                    permissionsDenied.add(permission)

                continue
            }

            if (PermissionChecker.checkPermissions(requireContext(), permission.permissions))
                continue

            permissionsDenied.add(permission)
        }

        viewBinding.recyclerView.apply {
            adapter = PermissionAdapter(permissionsDenied)
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }

        viewBinding.textViewAllow.setOnClickListener {
            onPermissionAllowClickListener?.onPermissionAllowClick()
            dismiss()
        }

        viewBinding.textViewDeny.setOnClickListener {
            onPermissionAllowClickListener?.onPermissionDenyClick()
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

data class Permission(
        @DrawableRes
        val icon: Int,
        val isRequired: Boolean,
        val permissions: Array<String>,
        val permissionName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Permission

        if (icon != other.icon) return false
        if (!permissions.contentEquals(other.permissions)) return false
        if (permissionName != other.permissionName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = icon
        result = 31 * result + permissions.contentHashCode()
        result = 31 * result + permissionName.hashCode()
        return result
    }
}
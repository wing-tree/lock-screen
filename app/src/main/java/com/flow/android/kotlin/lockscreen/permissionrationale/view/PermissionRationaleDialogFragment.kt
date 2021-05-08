package com.flow.android.kotlin.lockscreen.permissionrationale.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.base.BaseDialogFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentPermissionRationaleDialogBinding
import com.flow.android.kotlin.lockscreen.permissionrationale.adapter.PermissionRationaleAdapter

@RequiresApi(Build.VERSION_CODES.M)
class PermissionRationaleDialogFragment: BaseDialogFragment<FragmentPermissionRationaleDialogBinding>() {
    private val permissionsDenied = mutableListOf<PermissionRationale>()

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentPermissionRationaleDialogBinding {
        return FragmentPermissionRationaleDialogBinding.inflate(inflater, container, false)
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
                        icon = R.drawable.ic_round_folder_open_24,
                        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                        permissionName = getString(R.string.permission_rationale_dialog_fragment_002),
                        rationale = getString(R.string.permission_rationale_dialog_fragment_003)
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

            if (checkPermission(requireContext(), permission.permission))
                continue

            permissionsDenied.add(permission)
        }

        viewBinding.recyclerView.apply {
            adapter = PermissionRationaleAdapter(permissionsDenied)
            layoutManager = LinearLayoutManager(requireContext())
        }

        return view
    }

    companion object {
        fun permissionsGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission(context, Manifest.permission.READ_CALENDAR).not())
                    return false

                if (checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE).not())
                    return false

                if (Settings.canDrawOverlays(context).not())
                    return false

                return true
            } else
                return true
        }

        private fun checkPermission(context: Context, permission: String): Boolean {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
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
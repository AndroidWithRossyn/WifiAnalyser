package com.example.wifianalyser.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.wifianalyser.utils.CheckPermission
import com.example.wifianalyser.databinding.FragmentRequiredPermissionDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RequiredPermissionDialog @Inject constructor() : DialogFragment() {
    lateinit var binding: FragmentRequiredPermissionDialogBinding

    @Inject
    lateinit var permissionDialog: PermissionDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredPermissionDialogBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        setWindow()
        binding.tvAndroid.setOnClickListener {
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            activity?.let {
                if(CheckPermission.isRationaleTrue(it))
                permissionDialog.show(it.supportFragmentManager, "permission dialog")
                else CheckPermission.requestPermission(it)
            }
            dismiss()
        }
    }

    private fun setWindow() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
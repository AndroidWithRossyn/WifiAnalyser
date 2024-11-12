package com.example.wifianalyser.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.wifianalyser.utils.CheckPermission
import com.example.wifianalyser.databinding.FragmentPermissionDialogBinding
import javax.inject.Inject


class PermissionDialog @Inject constructor(): DialogFragment() {
    lateinit var binding: FragmentPermissionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPermissionDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        isCancelable=false
        windowSet()

    }

    private fun windowSet() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,)
    }

    private fun setListener() {
        binding.btnOpenSetting.setOnClickListener() {
            navigateToLocationPermission()
            dismiss()
        }
        binding.imgCross.setOnClickListener {
            dismiss()
        }
    }

    private fun navigateToLocationPermission() {
        activity?.let {
                CheckPermission.requestLocationPermission(it)
        }
    }

}
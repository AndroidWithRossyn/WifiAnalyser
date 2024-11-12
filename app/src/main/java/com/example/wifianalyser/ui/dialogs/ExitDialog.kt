package com.example.wifianalyser.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.wifianalyser.databinding.FragmentExitDialogBinding
import javax.inject.Inject

class ExitDialog @Inject constructor() : DialogFragment() {

lateinit var binding:FragmentExitDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExitDialogBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        windowSet()

        binding.tvOk.setOnClickListener {
            activity?.finishAffinity()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun windowSet() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,)
    }



}
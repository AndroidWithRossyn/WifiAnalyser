package com.example.wifianalyser.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.wifianalyser.ui.adapters.SpinnerListAdapter
import com.example.wifianalyser.models.SpinnerModel
import com.example.wifianalyser.databinding.FragmentSpinnerDialogBinding
import com.example.wifianalyser.ui.fragments.AccessPointsFragment.AccessPointsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SpinnerDialog @Inject constructor() : DialogFragment() {
    lateinit var spinnerListAdapter: SpinnerListAdapter
    private val accessPointsViewModel by activityViewModels<AccessPointsViewModel>()
    private lateinit var accessPointList: ArrayList<SpinnerModel>
     var speedLiveData = MutableLiveData<SpinnerModel>()

    lateinit var binding: FragmentSpinnerDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpinnerDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListener()

    }
    fun setListener(){
        binding.imgCross.setOnClickListener {
            dismiss()
        }
    }

        fun initialize(){
            accessPointList = ArrayList()
            spinnerListAdapter = SpinnerListAdapter(onClick = { logClicked, position ->
                val model = SpinnerModel(logClicked.accessPointName, logClicked.accessPointSpeed)
                speedLiveData.postValue(SpinnerModel(model.accessPointName,model.accessPointSpeed))
                dismiss()
            })
            accessPointsViewModel.accessPoints.observe(viewLifecycleOwner) {
                it.map { accessPointList.add(SpinnerModel(it.accessPointName, it.dbm)) }
                spinnerListAdapter.submitList(accessPointList)
            }

            binding.rvSpinner.adapter = spinnerListAdapter
            windowSet()
        }


    private fun windowSet() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
}
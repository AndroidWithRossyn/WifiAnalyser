package com.example.wifianalyser.ui.fragments.AccessPointsFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wifianalyser.utils.extensions.hide
import com.example.wifianalyser.utils.extensions.visible
import com.example.wifianalyser.utils.CheckPermission
import com.example.wifianalyser.models.AccessPointFilterEnum
import com.example.wifianalyser.utils.UserPreferences
import com.example.wifianalyser.R
import com.example.wifianalyser.receivers.WifiScanReceiver
import com.example.wifianalyser.databinding.FragmentAccessPointBinding
import com.example.wifianalyser.databinding.PermissionNotAllowBinding
import com.example.wifianalyser.ui.dialogs.PermissionDialog
import com.example.wifianalyser.ui.setting.UserSetting
import com.example.wifianalyser.ui.viewModel.UnRegisterReceiverViewModel
import com.example.wifianalyzer.Adapter.WifiListAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AccessPointFragment : Fragment() {
    private lateinit var binding: FragmentAccessPointBinding
    private val accessPointsViewModel by activityViewModels<AccessPointsViewModel>()
    private val unregisterReceiverViewModel by activityViewModels<UnRegisterReceiverViewModel>()
    private val accessPointAdapter by lazy { WifiListAdapter() }
    private var scanPauseResumeTag: Boolean = true

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var permissionDialog: PermissionDialog
    private var fiveGhztag: Boolean = false
    private var twoGhztag: Boolean = false
    lateinit var permissionNotAllowBinding: PermissionNotAllowBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAccessPointBinding.inflate(inflater, container, false)

        MobileAds.initialize(requireActivity()) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.permissionNotAllow.setOnInflateListener { stub, inflated ->
            permissionNotAllowBinding = PermissionNotAllowBinding.bind(inflated)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
        setListeners()

    }

    private fun initialize() {
        binding.permissionNotAllow.inflate()
        setUpRecyclerView()
    }

    private fun setListeners() {
        handleFilters()
        activity?.let { activity ->
            permissionNotAllowBinding.btnPermission.setOnClickListener {
                permissionDialog.show(activity.supportFragmentManager, "permissionDialog")
            }

            binding.toolbar.imgCapture.setOnClickListener {
                val imageUri = accessPointsViewModel.screenShotAndShare(activity, binding.clRoot)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/png"
                intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivity(intent)
            }
            binding.toolbar.imgMore.setOnClickListener {
                startActivity(Intent(activity, UserSetting::class.java))
            }
        }
    }

    val TAG = "accessLogcat"
    private fun setObservers() {
        accessPointsViewModel.accessPoints.observe(viewLifecycleOwner) {
            accessPointAdapter.submitList(it)
             Toast.makeText(activity, "list Submitted", Toast.LENGTH_LONG).show()
        }

        unregisterReceiverViewModel.receiverWatcher.observe(viewLifecycleOwner) {
            scanPauseResumeTag = it
        }
        //Pause Resume Scan
        binding.toolbar.imgStartStopScan.setOnClickListener {

            when (scanPauseResumeTag) {
                true -> unregisterReceiverViewModel.unRegister(false)
                false -> unregisterReceiverViewModel.unRegister(true)
            }
        }
        unregisterReceiverViewModel.state.observe(viewLifecycleOwner) {
            binding.toolbar.imgStartStopScan.setImageResource(it)
        }
        accessPointsViewModel._connectedWifiLiveData.observe(viewLifecycleOwner) {
            Log.i("wifiInfo", "${it.wifiName} and ${it.ipAddress}")
            updateConnectedNetworkData(it.wifiName, it.ipAddress)
        }
    }

    private fun updateConnectedNetworkData(wifiName: String, ipAddress: String) {
//        binding.tvConnectedNetworkName.text = wifiName
//        binding.tvConnectedNetworkIpAddress.text = ipAddress
    }

    private fun setUpRecyclerView() {
        binding.rvWifiAccessPoints.adapter = accessPointAdapter
    }

    private fun handlePermissions() {
        activity?.let {
            if (CheckPermission.verifyLocationPermission(it)) {
                binding.rvWifiAccessPoints.visible()
                binding.toolbar.root.visible()
                permissionNotAllowBinding.clParent.hide()

                WifiScanReceiver.startAlarm(it, true)
            } else {
                permissionNotAllowBinding.clParent.visible()
                binding.rvWifiAccessPoints.hide()
                binding.toolbar.root.hide()
            }
        }
    }

    private fun handleFilters() {
        binding.toolbar.img5ghz.apply {
            setOnClickListener {
                accessPointsViewModel.setFilter(AccessPointFilterEnum.FIVE_G)
                binding.toolbar.img24ghz.setImageResource(R.drawable.ic_twoghz_unactive)
                if (fiveGhztag)
                    accessPointsViewModel.setFilter(AccessPointFilterEnum.ALL)
                fiveGhztag = !fiveGhztag
                twoGhztag = false
                if (fiveGhztag) setImageResource(R.drawable.ic_five_ghz_active)
                else setImageResource(R.drawable.ic_five_ghz_unactive)
            }
        }

        binding.toolbar.img24ghz.apply {
            setOnClickListener {
                accessPointsViewModel.setFilter(AccessPointFilterEnum.TWO_G)
                binding.toolbar.img5ghz.setImageResource(R.drawable.ic_five_ghz_unactive)
                if (twoGhztag)
                    accessPointsViewModel.setFilter(AccessPointFilterEnum.ALL)
                twoGhztag = !twoGhztag
                fiveGhztag = false
                if (twoGhztag) setImageResource(R.drawable.ic_twoghz_active)
                else setImageResource(R.drawable.ic_twoghz_unactive)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handlePermissions()
        activity?.let {
            CheckPermission.requestGPSProvider(it)
        }
        accessPointsViewModel.getConnectedWifiInfo()
    }

}



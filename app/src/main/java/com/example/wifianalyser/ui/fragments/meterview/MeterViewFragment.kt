package com.example.wifianalyser.ui.fragments.meterview

import android.content.Intent
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wifianalyser.utils.extensions.hide
import com.example.wifianalyser.utils.extensions.visible
import com.example.wifianalyser.utils.CheckPermission
import com.example.wifianalyser.models.AccessPointFilterEnum
import com.example.wifianalyser.R
import com.example.wifianalyser.databinding.FragmentMeterViewBinding
import com.example.wifianalyser.databinding.PermissionNotAllowBinding
import com.example.wifianalyser.ui.fragments.AccessPointsFragment.AccessPointsViewModel
import com.example.wifianalyser.ui.dialogs.PermissionDialog
import com.example.wifianalyser.ui.dialogs.SpinnerDialog
import com.example.wifianalyser.ui.setting.UserSetting
import com.example.wifianalyser.ui.viewModel.UnRegisterReceiverViewModel
import com.github.anastr.speedviewlib.components.Style
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MeterViewFragment : Fragment() {

    lateinit var binding: FragmentMeterViewBinding
    private val accessPointsViewModel by activityViewModels<AccessPointsViewModel>()
    private val unregisterReceiverViewModel by activityViewModels<UnRegisterReceiverViewModel>()
    private var permissionNotAllowBinding: PermissionNotAllowBinding?=null
    private var scanPauseResumeTag: Boolean = true
    private var mInterstitialAd: InterstitialAd? = null


    @Inject
    lateinit var wifiManager: WifiManager
    @Inject
    lateinit var spinnerDialog: SpinnerDialog
    @Inject
    lateinit var permissionDialog:PermissionDialog

    private var fiveGhztag: Boolean = false
    private var twoGhztag: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMeterViewBinding.inflate(inflater,container,false)

        MobileAds.initialize(requireActivity()) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(requireActivity(),getString(R.string.interstitial_ad), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("TAG", adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("TAG", "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        binding.permissionNotAllow.setOnInflateListener { stub, inflated ->
            permissionNotAllowBinding = PermissionNotAllowBinding.bind(inflated)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setListener()

    }

    private fun initialize() {
        designSpeedView()
        handleFilters()
        binding.permissionNotAllow.inflate()
        binding.toolbar.tvTitleFragment.text = resources.getString(R.string.speedo_metre)

    }

    private fun setListener() {
        toolBarListener()
        activity?.let {activity ->
        binding.tvShowAll.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(requireActivity())
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
            }
             spinnerDialog.show(activity.supportFragmentManager, "spinnerDialog")
        }
        permissionNotAllowBinding?.btnPermission?.setOnClickListener {
            permissionDialog.show(activity.supportFragmentManager, "permissionDialog")
        }
        }
    }

    private fun setObserver() {
        spinnerDialog.speedLiveData.observe(viewLifecycleOwner) {
            setSpeed(it.accessPointSpeed.toFloat())
            binding.tvSpeedTest.text = it.accessPointName
        }
    }

    private fun designSpeedView() {
        val meterView = binding.speedView
        meterView.apply {
            maxSpeed = -30f
            makeSections(3, Color.CYAN, Style.BUTT)
            activity?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sections[2].color = resources.getColor(R.color.green_color, it.theme)
                    sections[1].color = resources.getColor(R.color.color_blue, it.theme)
                    sections[0].color = resources.getColor(R.color.color_red, it.theme)
                } else {
                    sections[2].color = resources.getColor(R.color.green_color)
                    sections[1].color = resources.getColor(R.color.color_blue)
                    sections[0].color = resources.getColor(R.color.color_red)
                }
            }
        }
    }

    private fun setSpeed(speedTo: Float) {
        binding.speedView.speedTo(speedTo, 5000)
    }

    private fun toolBarListener() {
        activity?.let { activity ->
            binding.toolbar.imgMore.setOnClickListener {
                startActivity(Intent(activity, UserSetting::class.java))
            }
            binding.toolbar.imgCapture.setOnClickListener {
                val imageUri = accessPointsViewModel.screenShotAndShare(activity, binding.root)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/png"
                intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivity(intent)
            }
            unregisterReceiverViewModel.receiverWatcher.observe(viewLifecycleOwner){
                scanPauseResumeTag = it
            }
            binding.toolbar.imgStartStopScan.setOnClickListener {

                when (scanPauseResumeTag) {
                    true -> {
                        unregisterReceiverViewModel.unRegister(false)

                    }
                    false -> {
                        unregisterReceiverViewModel.unRegister(true)
                    }
                }

            }
            unregisterReceiverViewModel.state.observe(viewLifecycleOwner) {
                binding.toolbar.imgStartStopScan.setImageResource(it)
            }
        }
    }

    private fun handlePermissions() {
        activity?.let {
            if (CheckPermission.verifyLocationPermission(it)) {
                binding.permissionNotAllow.hide()
                binding.clContent.visible()
            } else {
                binding.clContent.hide()
                binding.permissionNotAllow.visible()
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
    }
}
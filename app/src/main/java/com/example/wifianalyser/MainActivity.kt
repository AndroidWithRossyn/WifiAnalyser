package com.example.wifianalyser


import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.wifianalyser.utils.CheckPermission
import com.example.wifianalyser.utils.UserPreferences
import com.example.wifianalyser.receivers.WifiScanReceiver
import com.example.wifianalyser.databinding.ActivityMainBinding
import com.example.wifianalyser.ui.dialogs.ExitDialog
import com.example.wifianalyser.ui.dialogs.RequiredPermissionDialog
import com.example.wifianalyser.ui.viewModel.UnRegisterReceiverViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var mAdView : AdView

    private lateinit var wifiScannerReceiver: WifiScanReceiver
    private lateinit var binding: ActivityMainBinding
    private val unregisterReceiverViewModel by viewModels<UnRegisterReceiverViewModel>()
    private lateinit var navController: NavController

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var requiredPermissionDialog: RequiredPermissionDialog

    @Inject
    lateinit var exitDialog: ExitDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        setObserver()
        setListener()
        checkDarkMode()

    }

    private fun initialize() {

        //register Receiver
        wifiScannerReceiver=WifiScanReceiver()
        registerReceiver(
            wifiScannerReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        //start Alarm
        WifiScanReceiver.startAlarm(this, true)
        //bottom nav with navigation component
        binding.bottomNavigationView.background = null
        val navView: BottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        //dialog
        if (!CheckPermission.verifyLocationPermission(this)) {
            requiredPermissionDialog.show(supportFragmentManager, "required permission")
        }

        navController.addOnDestinationChangedListener { _, nd, _ ->
            if (nd.id == R.id.access_point_destination || nd.id == R.id.bar_graph_destination) {
                binding.btnFab.setImageDrawable(getDrawable(R.drawable.ic_speed_unactive))
            } else {
                binding.btnFab.setImageDrawable(getDrawable(R.drawable.ic_speed_active))
            }
        }
    }

    private fun setListener() {
        binding.btnFab.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.speedometer_destination)
        }
    }

    private fun setObserver() {
        unregisterReceiverViewModel.receiverWatcher.observe(this) {
            if (!it) {
                unregisterReceiver(wifiScannerReceiver)
                Toast.makeText(this, "Scanning Paused...", Toast.LENGTH_SHORT).show()

            } else {
                registerReceiver(
                    wifiScannerReceiver,
                    IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                )
                Toast.makeText(this, "Scanning Registered...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkDarkMode() {
        var mode: Boolean
        CoroutineScope(IO).launch {
            mode = userPreferences.getThemeStatus().first()
            withContext(Main) {
                if (mode) AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                else AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }
    }

    override fun onBackPressed() {
        exitDialog.show(supportFragmentManager, "exit dialog")
    }
}
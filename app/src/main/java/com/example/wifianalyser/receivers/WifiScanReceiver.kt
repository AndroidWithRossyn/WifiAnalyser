package com.example.wifianalyser.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.wifianalyser.roomdb.AccessPointDao
import com.example.wifianalyser.roomdb.AccessPointsModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WifiScanReceiver : BroadcastReceiver() {

    @Inject
    lateinit var accessPointDao: AccessPointDao

    @Inject
    lateinit var wifiManager: WifiManager
    private var wifiList: List<ScanResult> = arrayListOf()

    override fun onReceive(context: Context?, intent: Intent) {
        Log.d(TAG, "onReceive: ")

        when (intent.action) {
            ACTION_START -> {
                wifiManager.startScan()
                context?.let { startAlarm(it) }
            }
            else -> {
                wifiManager =
                    context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
                if (success) {
                    wifiList = if (wifiManager.scanResults?.isEmpty() == false) {
                        wifiManager.scanResults ?: listOf()
                    } else {
                        wifiList
                    }
                    //SCAN SUCCESS FUNCTION CALL
                    scanSuccess(wifiList)
                }
            }
        }
    }

    private fun scanSuccess(wifiList: List<ScanResult>) {
        transformWifiAccessPoints(wifiList)
    }

    private fun transformWifiAccessPoints(scanResult: List<ScanResult>) {
        val temporaryWifiList = ArrayList<AccessPointsModel>()


        scanResult.map {
                temporaryWifiList.add(
                    AccessPointsModel(
                        it.SSID,
                        it.level,
                        it.BSSID,
                        frequencyToChannel(it.frequency),
                        it.frequency,
                        getAccessPointSecurity(it.capabilities)
                    )
                )
        }
        CoroutineScope(Dispatchers.IO).launch {
            accessPointDao.updateData(temporaryWifiList)
        }
    }

    private fun frequencyToChannel(freq: Int): Int {
        return when{
            freq == 2484 ->  14
            freq < 2484 -> (freq - 2407) / 5
            else -> freq / 5 - 1000
        }
    }

    private fun calculateChannelWidth(channelWidth: Int) =
        when (channelWidth) {
            0 -> 20
            1 -> 40
            2 -> 80
            else -> 20
        }

    private fun getAccessPointSecurity(capabilities: String): String {
        val security = capabilities.split("]")
        return security.getOrNull(0)?.plus("]") ?: "N/A"
    }

    companion object {
        const val TAG = "WifiScanLogCat"
        const val ACTION_START = "action_start"

        fun startAlarm(context: Context, firstLaunch: Boolean = false) {
            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent =
                Intent(context, WifiScanReceiver::class.java).apply { action = ACTION_START }
            Log.d(TAG, "onstart: ")
            val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            val triggerAt =
                if (firstLaunch) System.currentTimeMillis()
                else System.currentTimeMillis() + 30000
            alarmManager.setExact(AlarmManager.RTC, triggerAt, pendingIntent)
        }
    }
}

package com.example.wifianalyser.ui.fragments.AccessPointsFragment

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.example.wifianalyser.utils.extensions.getBitmapFromView
import com.example.wifianalyser.models.AccessPointFilterEnum
import com.example.wifianalyser.roomdb.AccessPointDao
import com.example.wifianalyzer.Model.ConnectedNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class AccessPointsViewModel @Inject constructor(
    private val accessPointDao: AccessPointDao,
) : ViewModel() {
    @Inject
    lateinit var wifiManager: WifiManager
    private var currentFilter = MutableLiveData(AccessPointFilterEnum.ALL)
    var _connectedWifiLiveData = MutableLiveData<ConnectedNetwork>()
    var tempCounter = 0

    fun setFilter(filterEnum: AccessPointFilterEnum) {
        currentFilter.postValue(filterEnum)
    }

    val accessPoints = currentFilter.switchMap { filter ->
        when (filter) {
            AccessPointFilterEnum.ALL -> accessPointsAll
            AccessPointFilterEnum.FIVE_G -> accessPoints5g
            AccessPointFilterEnum.TWO_G -> accessPoints2g
            null -> accessPointsAll
        }
    }
    private val accessPointsAll by lazy { accessPointDao.getAccessPointsFromDB() }

    //returning liveData of only 5gHz access points
    private val accessPoints5g by lazy {
        accessPointsAll.switchMap { accessPoints ->
            liveData { emit(accessPoints.filter { it.frequency > 2484 }) }
        }
    }

    //returning liveData of only 2.4gHz access points
    private val accessPoints2g by lazy {
        accessPointsAll.switchMap { accessPoints ->
            liveData { emit(accessPoints.filter { it.frequency <= 2484 }) }
        }
    }
    val TAG = "AcceesLogcat"
    fun getConnectedWifiInfo() {
        Log.d(TAG, "I am on getConnected: ")
        val wifiInfo = wifiManager.connectionInfo

        val ip = wifiInfo?.ipAddress
        val ipAddress: String = ip?.let { Formatter.formatIpAddress(it) } ?: "Not Found"
        val wifiName = wifiInfo.ssid
        Log.i("wifiInfos", "$wifiName and $ipAddress")
        _connectedWifiLiveData.postValue(ConnectedNetwork(wifiName, ipAddress))
        Log.d("wifiInfos", "getConnectedWifiInfo: ")
    }

     fun createStorageDirectories(context: Context): File {
        val localFile = File(
            context.getExternalFilesDir(null)
                .toString() + File.separator + "Images"
        )
       // Log.d(tag, localFile.absolutePath)
        if (!localFile.exists()) {
            localFile.mkdirs()
        }
        return localFile
    }

     fun screenShotAndShare(context: Context,layout: ConstraintLayout):Uri? {
         var imageUri: Uri? = null
        val bitmap = layout.getBitmapFromView()
        val directory = createStorageDirectories(context)
        tempCounter++
        val myPath = File(directory, "image$tempCounter.png")
        val fileOutputStream = FileOutputStream(myPath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        if (myPath.exists()) {

             imageUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",  //(use your app signature + ".provider" )
                myPath
            )

        } else {
            Toast.makeText(context, "Error occur while fetching image", Toast.LENGTH_SHORT).show()
        }
         return imageUri
    }

    init {
        viewModelScope.launch {
            val filter = AccessPointFilterEnum.ALL
            currentFilter.postValue(filter)
        }
    }
}


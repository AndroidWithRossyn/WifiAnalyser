package com.example.wifianalyser.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.wifianalyser.R

object CheckPermission {

    fun askLocationPermission(fragment: Fragment): Boolean {
        val permission =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                }
            }
        return false
    }

    fun requestLocationPermission(context: Activity) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val intentPermission = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intentPermission.data = uri
                context.startActivity(intentPermission)
            } else {
                // No explanation needed, we can request the permission.
                requestPermission(context)
            }
        }
    }

    fun isRationaleTrue(context: Activity) : Boolean{
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        return false
    }

     fun requestPermission(activity: Activity) {

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1112
        )
    }

    fun requestGPSProvider(context: Activity) {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.gpsPermissionTitle))
                .setPositiveButton(
                    context.resources.getString(R.string.enable)
                ) { dialog, _ ->
                    context.startActivity(intent)
                    dialog.dismiss()
                    //Prompt the user once explanation has been shown
                }
                .setNegativeButton(
                    context.resources.getString(R.string.cancel)
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    fun verifyGPSEnable(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun verifyLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
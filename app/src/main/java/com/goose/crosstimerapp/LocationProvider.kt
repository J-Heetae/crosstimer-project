package com.goose.crosstimerapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

class LocationProvider(val context: Context) {
    private var location: Location? = null
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val TAG = LocationProvider::class.java.simpleName

    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "위치 권한이 없습니다.")
            onLocationReceived(null)
            return
        }

        val isGpsProviderEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkProviderEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsProviderEnable && !isNetworkProviderEnable) {
            Log.w(TAG, "GPS, Network 모두 비활성화됨")
            onLocationReceived(null)
            return
        }

        //현재는 GPS 우선, GPS 비활성화인 경우 NETWORK로 제공
        //추후에 FusedLocationProviderClient로 리팩토링
        val provider: String? = if (isGpsProviderEnable) {
            LocationManager.GPS_PROVIDER
        } else {
            LocationManager.NETWORK_PROVIDER
        }

        locationManager.requestSingleUpdate(provider.toString(), object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                Log.d(TAG, "위치 수신 완료: $loc")
                location = loc
                onLocationReceived(loc)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }, null)
    }
}
package com.goose.crosstimerapp

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.goose.crosstimerapp.databinding.ActivityMainBinding
import com.goose.crosstimerapp.retrofit.CrossroadRequest
import com.goose.crosstimerapp.retrofit.CrossroadResponse
import com.goose.crosstimerapp.retrofit.CrossroadService
import com.goose.crosstimerapp.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Response
import kotlin.math.cos

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityMainBinding
    lateinit var locationProvider: LocationProvider

    private val TAG = MainActivity::class.java.simpleName

    private var mMap: GoogleMap? = null

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    //런타임 권한 요청 코드
    private val PERMISSION_REQUEST_CODE = 100

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    //위치 서비스 및 권한 요청시 필요한 런쳐
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>
    lateinit var getLocationAccessPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAllPermissions()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setButton()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.let { map ->
            map.setMaxZoomPreference(20.0f)
            map.setMinZoomPreference(12.0f)

            map.uiSettings.apply {
                isZoomControlsEnabled = true
            }

//            getCurrentLocationAndUpdateUI()
        }
    }

    private fun getCurrentLocationAndUpdateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        locationProvider.getCurrentLocation { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude

                Log.i(TAG, "getCurrentLocationAndUpdateUI: 현재 위치 $latitude , $longitude")

                moveCameraToCurrentLocation()
                setMarker(latitude, longitude, "현재 위치")
            } else {
                Log.w(TAG, "getCurrentLocationAndUpdateUI: 현재 위치 정보를 가져올 수 없습니다.")
                Toast.makeText(this, "현재 위치 정보를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setButton() {
        binding.fab.setOnClickListener {
            Log.w(TAG, "setButton: getCurrentLocationAndUpdateUI() 호출")
            getCurrentLocationAndUpdateUI()
        }
    }

    private fun setMarker(latitude: Double, longitude: Double, titleString: String) {
        mMap?.let { map ->
            map.clear()

            val currentLocation = LatLng(latitude, longitude)

            val markerOption = MarkerOptions()
                .position(currentLocation)
                .title(titleString)

            map.addMarker(markerOption)
        }
    }

    private fun moveCameraToCurrentLocation() {
        Log.i(TAG, "setButton: 현재 위치로 카메라 이동")
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 18.0f))
    }

    private fun getBoundingBoxAround(lat: Double, lot: Double): Pair<LatLng, LatLng> {
        val radius = 500.0

        val latOffset = radius / 111000.0
        val lotOffset = radius / (111000.0 * cos(Math.toRadians(lat)))

        val southWest = LatLng(lat - latOffset, lot - lotOffset) // 왼쪽 아래
        val northEast = LatLng(lat + latOffset, lot + lotOffset) // 오른쪽 위

        return Pair(southWest, northEast)
    }

    private fun getCrossroadDataInRange(lat: Double, lot: Double) {
        val retrofitAPI = RetrofitConnection.getInstance().create(
            CrossroadService::class.java
        )
        Log.i(TAG, "getCrossroadDataInRange: retrofitAPI 생성")

        val boundingBoxAround = getBoundingBoxAround(lat, lot)
        Log.i(TAG, "getCrossroadDataInRange: getBoundingBoxAround() 실행 $boundingBoxAround")

        retrofitAPI.getCrossroadDataInRange(
            CrossroadRequest(
                swLat = boundingBoxAround.first.latitude,
                swLot = boundingBoxAround.first.longitude,
                neLat = boundingBoxAround.second.latitude,
                neLot = boundingBoxAround.second.longitude
            )
        ).enqueue(object : retrofit2.Callback<List<CrossroadResponse>> {
            override fun onResponse(
                call: Call<List<CrossroadResponse>>,
                response: Response<List<CrossroadResponse>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "교차로 데이터 업데이트 완료", Toast.LENGTH_LONG).show()
                    response.body()?.let {
                        Log.d(TAG, "onResponse: $it")
                    }
                } else {
                    Toast.makeText(this@MainActivity, "교차로 데이터를 가져오는데 실패했습니다.", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<CrossroadResponse>?>, t: Throwable) {
                t.printStackTrace()
                Log.w(
                    TAG,
                    "getCrossroadDataInRange: onFailure: 교차로 데이터를 가져오는데 실패했습니다. ${t.message}"
                )
                Toast.makeText(this@MainActivity, "교차로 데이터를 가져오는데 실패했습니다.", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun checkAllPermissions() {
        Log.d(TAG, "checkAllPermissions: 위치 서비스 및 런타임 권한 사용 가능 여부 체크")
        if (isLocationServiceAvailable()) {
            Log.i(TAG, "checkAllPermissions: 위치 서비스 사용 가능")
            checkRuntimeLocationPermissions()
        } else {
            Log.w(TAG, "checkAllPermissions: 위치 서비스 사용 불가")
            showDialogForLocationServiceSetting()
        }
    }

    private fun isLocationServiceAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return (gpsEnabled || networkEnabled)
    }

    private fun checkRuntimeLocationPermissions() {
        if (isLocationPermissionsAvailable()) {
            Log.i(TAG, "checkRuntimeLocationPermissions: 위치 권한 사용 가능, getCurrentLocationAndUpdateUI 호출")
            //위치 정보 데이터 가져오기
            getCurrentLocationAndUpdateUI()
        } else {
            Log.w(TAG, "checkRuntimeLocationPermissions: 위치 권한 사용 불가")
            requestRuntimeLocationPermissions()
        }
    }

    private fun isLocationPermissionsAvailable(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
        )

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestRuntimeLocationPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    showDialogForLocationPermissionSetting()
                    break
                }
            }
            Log.i(TAG, "onRequestPermissionsResult: 위치 권한 사용 가능, getCurrentLocationAndUpdateUI 호출")
            getCurrentLocationAndUpdateUI()
        } else {
            appFinishWithToast("위치 권한을 사용할 수 없어 앱을 종료합니다.")
        }
    }

    private fun showDialogForLocationServiceSetting() {
        Log.d(TAG, "showDialogForLocationServiceSetting: 위치 서비스 사용 요청")

        registerGpsPermissionLauncher()

        AlertDialog.Builder(this@MainActivity)
            .setTitle("위치 서비스 비활성화")
            .setMessage("앱을 사용하려면 위치 서비스가 필요합니다.")
            .setCancelable(true)
            .setPositiveButton("설정", DialogInterface.OnClickListener { dialogInterface, i ->
                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                getGPSPermissionLauncher.launch(callGPSSettingIntent)
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
                appFinishWithToast("위치 서비스를 사용할 수 없어 앱을 종료합니다.")
            })
            .create()
            .show()
    }

    private fun registerGpsPermissionLauncher() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "registerGpsPermissionLauncher: $result")
            if (isLocationServiceAvailable()) {
                Log.i(TAG, "checkAllPermissions: 위치 서비스 사용 가능")
                checkRuntimeLocationPermissions()
            } else {
                appFinishWithToast("위치 서비스를 사용할 수 없어 앱을 종료합니다.")
            }
        }
    }

    private fun showDialogForLocationPermissionSetting() {
        Log.d(TAG, "showDialogForLocationPermissionSetting: 위치 권한 허용 요청 (설정으로 이동)")

        registerLocationAccessPermissionLauncher()

        AlertDialog.Builder(this@MainActivity)
            .setTitle("위치 권한 필요")
            .setMessage("이 앱은 위치 정보를 사용하기 위해 권한이 필요합니다. 설정에서 권한을 허용해 주세요.")
            .setCancelable(false)
            .setPositiveButton("설정") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.fromParts("package", packageName, null)
                getLocationAccessPermissionLauncher.launch(intent)
            }
            .setNegativeButton("종료", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
                appFinishWithToast("위치 권한을 사용할 수 없어 앱을 종료합니다.")
            })
            .create()
            .show()
    }

    private fun registerLocationAccessPermissionLauncher() {
        getLocationAccessPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "registerLocationAccessPermissionLauncher: $result")
            if (isLocationPermissionsAvailable()) {
                Log.w(TAG, "registerLocationAccessPermissionLauncher: 위치 권한 사용 가능, getCurrentLocationAndUpdateUI 호출")
                //위치 정보 데이터 가져오기
                getCurrentLocationAndUpdateUI()
            } else {
                appFinishWithToast("위치 권한을 사용할 수 없어 앱을 종료합니다.")
            }
        }
    }

    private fun appFinishWithToast(toastText: String) {
        Toast.makeText(this@MainActivity, toastText, Toast.LENGTH_LONG).show()
        finish()
    }
}

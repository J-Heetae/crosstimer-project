package com.goose.crosstimerapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import com.goose.crosstimerapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val TAG = MainActivity::class.java.simpleName

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

        registerLocationAccessPermissionLauncher()
        registerGpsPermissionLauncher()
        checkAllPermissions()
    }

    private fun registerGpsPermissionLauncher() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "registerGpsPermissionLauncher: $result")
            if (!isLocationServiceAvailable()) {
                appFinishWithToast("위치 서비스를 사용할 수 없어 앱을 종료합니다.")
            }
        }
    }

    private fun registerLocationAccessPermissionLauncher() {
        getLocationAccessPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "registerLocationAccessPermissionLauncher: $result")
            if (!isLocationPermissionsAvailable()) {
                appFinishWithToast("위치 권한을 사용할 수 없어 앱을 종료합니다.")
            }
        }
    }

    private fun checkAllPermissions() {
        Log.d(TAG, "checkAllPermissions: 위치 서비스 및 런타인 권한 사용 가능 여부 체크")
        checkLocationService()
        checkRuntimeLocationPermissions()
    }

    private fun checkLocationService() {
        if (!isLocationServiceAvailable()) {
            Log.w(TAG, "checkLocationService: 위치 서비스 사용 불가")
            showDialogForLocationServiceSetting()
        }
    }

    private fun checkRuntimeLocationPermissions() {
        if (!isLocationPermissionsAvailable()) {
            Log.w(TAG, "checkLocationPermissions: 위치 권한 사용 불가")
            requestRuntimeLocationPermissions()
        }
    }

    private fun isLocationServiceAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return (gpsEnabled || networkEnabled)
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


    private fun showDialogForLocationServiceSetting() {
        Log.d(TAG, "showDialogForLocationServiceSetting: 위치 서비스 사용 요청")

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

    private fun showDialogForLocationPermissionSetting() {
        Log.d(TAG, "showDialogForLocationPermissionSetting: 위치 권한 허용 요청")

        AlertDialog.Builder(this@MainActivity)
            .setTitle("위치 권한 필요")
            .setMessage("이 앱은 위치 정보를 사용하기 위해 권한이 필요합니다. 설정에서 권한을 허용해 주세요.")
            .setCancelable(false)
            .setPositiveButton("설정") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.fromParts("package", packageName, null)
                getLocationAccessPermissionLauncher.launch(intent)

                Toast.makeText(this@MainActivity, "위치 권한을 허용해 주세요.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("종료", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
                appFinishWithToast("위치 권한을 사용할 수 없어 앱을 종료합니다.")
            })
            .create()
            .show()
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
        } else {
            appFinishWithToast("위치 권한을 사용할 수 없어 앱을 종료합니다.")
        }
    }

    private fun appFinishWithToast(toastText: String) {
        Toast.makeText(this@MainActivity, toastText, Toast.LENGTH_LONG).show()
        finish()
    }
}
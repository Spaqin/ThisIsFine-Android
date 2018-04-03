@file:Suppress("DEPRECATION")

package com.spaqin.finecompanion

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.support.annotation.RequiresApi
import android.util.Log
import com.spaqin.finecompanion.Communications.BLeSerialPortService
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    var mScanning = false

    private val mHandler = Handler()
    private val btManager: BluetoothManager by lazy{ getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val btAdapter: BluetoothAdapter by lazy { btManager.adapter }
    private val btScanner: BluetoothLeScanner by lazy { btAdapter.bluetoothLeScanner }

    private val REQUEST_BT_ON = 3
    private val REQUEST_PERM = 4
    private val REQUEST_BT_PR = 5
    private val SCAN_TIME: Long = 30000

    var devFound = false

    val scanFilter: ScanFilter by lazy {
        val scanFilterBuilder = ScanFilter.Builder()
        scanFilterBuilder.setDeviceName("ThisIsFine")
        //scanFilterBuilder.setServiceUuid(ParcelUuid(BLeSerialPortService.SERIAL_SERVICE_UUID))
        scanFilterBuilder.build()
    }

    val scanSettings: ScanSettings by lazy {
        var scanSettingsBuilder = ScanSettings.Builder()
        scanSettingsBuilder.setReportDelay(0)
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        scanSettingsBuilder.build()
    }

    val mLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result == null)
                return
            checkDevice(result.device, result.rssi)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERM)
        //scanInit()
    }

    private fun scanInit() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast(R.string.bleunsupported)
            finish()
        }

        if (btAdapter == null) {
            toast(R.string.bleunsupported)
            finish()
        }

        if (!btAdapter.isEnabled)
        {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BT_ON)
        }

        scanDevices()
    }

    private fun scanDevices() {
        mHandler.postDelayed({
            stopScanning()
            if(!devFound)
            {
                Log.d("No dev", "no dev found")
                toast(R.string.notfound_toast)
                finish()
            }
        }, SCAN_TIME)
        mScanning = true
        var scanSettingsBuilder = ScanSettings.Builder()
        scanSettingsBuilder.setReportDelay(0)
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        val sSettings = scanSettingsBuilder.build()
        btScanner.startScan(listOf(scanFilter), sSettings, mLeScanCallback)
    }

    private fun stopScanning()
    {
        mScanning = false
        btScanner.stopScan(mLeScanCallback)
    }

    private fun checkDevice(device: BluetoothDevice, rssi: Int)
    {
        Log.d("BLE", "checking device: " + device.address +" "+ device.type +" "+ device.name + " " +device.uuids)
        if(device.type == BluetoothDevice.DEVICE_TYPE_LE && // is LE
               // device.address == "00:15:85:10:87:2E" && // correct device
                device.bondState == BluetoothDevice.BOND_NONE
                && !devFound
                )  // not connected yet
        {
            devFound = true
            toast(R.string.found_toast)
            stopScanning()
            finish()
            startActivity(intentFor<RealMainScreen>(BluetoothDevice.EXTRA_DEVICE to device.address))
        }
    }

    override fun onStop() {
        super.onStop()
       //stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode)
        {
            REQUEST_BT_ON -> {
                if(resultCode != Activity.RESULT_OK) {
                    toast("This application requires Bluetooth to work properly. Please enable it.")
                }
            }
            REQUEST_PERM -> {
                scanInit()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERM -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    scanInit()
                } else {
                    toast("The app needs location to function.")
                    finish()
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.

            else -> {
                // Ignore all other requests.
            }
        }
    }

}
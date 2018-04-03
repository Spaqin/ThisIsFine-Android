package com.spaqin.finecompanion

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.spaqin.fineapp.Communications.FineMessage
import com.spaqin.fineapp.Sensors.RawSensorData
import com.spaqin.fineapp.Sensors.SensorType
import com.spaqin.finecompanion.Communications.BLeSerialPortService
import com.spaqin.finecompanion.Communications.FineSet
import com.spaqin.finecompanion.Communications.FineSetAdapter
import com.spaqin.finecompanion.FineDevice.FineActivityCallbacks
import com.spaqin.finecompanion.FineDevice.FineController
import com.spaqin.finecompanion.FineDevice.FineDevice
import kotlinx.android.synthetic.main.activity_main_screen.*

import kotlinx.android.synthetic.main.activity_real_main_screen.*
import kotlinx.android.synthetic.main.content_real_main_screen.*
import org.jetbrains.anko.toast

class RealMainScreen : AppCompatActivity(), BLeSerialPortService.Callback, FineActivityCallbacks {

    var btAddr: String = ""
    private val btManager: BluetoothManager by lazy { getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val btAdapter: BluetoothAdapter by lazy { btManager.adapter }
    private val btDev: BluetoothDevice by lazy { btAddr = intent.extras.get(BluetoothDevice.EXTRA_DEVICE) as String
        btAdapter.getRemoteDevice(btAddr)
    }

    var lastSensor: SensorType? = null
    var lastSensorData: RawSensorData? = null
    var wasDiscon = true
    var current_latitude: Double = 0.0
    var current_longitude: Double = 0.0

    var datasetList: ArrayList<FineSet> = arrayListOf()
    val fineSetAdp by lazy { FineSetAdapter(this, datasetList) }

    val locListen = object: LocationListener {
        override fun onProviderEnabled(provider: String?) {
            // ignore
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // it happens
        }

        override fun onLocationChanged(location: Location?) {
            current_latitude = location!!.latitude
            current_longitude = location.longitude
            Log.d("LOCATION", "\nlong: " + current_longitude + " lat: " + current_latitude)
        }

        override fun onProviderDisabled(provider: String?) {
            // that happens too
        }
    }

    private val locManager: LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    override fun onFineCommunicationError(fm: FineMessage) {
        // ignore
    }

    override fun notifySensorChange(s: SensorType) {
        runOnUiThread {
            Log.d("SensorChange", "sensor: " + s + ": " + FineDevice.sensorData[s.protoValue.toInt()] + " flag: " + (lastSensor == s && lastSensorData == FineDevice.sensorData[s.protoValue.toInt()]))
            if (lastSensor != s || lastSensorData != FineDevice.sensorData[s.protoValue.toInt()]) {
                fineSetAdp.add(
                        FineSet(s,
                                FineDevice.sensorData[s.protoValue.toInt()].getConvertedData(),
                                current_latitude,
                                current_longitude)
                )
                //listView.setSelection(listView.count-1)
            }
            lastSensor = s
            lastSensorData = FineDevice.sensorData[s.protoValue.toInt()]
        }
    }

    override fun notifyCapabilities() {
        //create elements as necessary, to be finished later
    }

    override fun onCommunicationError(status: Int, msg: String?) {
        //try to reconnect?
        fineController.connect(btDev)
    }

    override fun onDeviceInfoAvailable() {
        // ignore, we know it's our device
    }

    override fun onDeviceFound(device: BluetoothDevice?) {
    }

    override fun onReceive(context: Context?, rx: BluetoothGattCharacteristic?) {
        val btarr = rx!!.value
        fineController.parseMessage(btarr)

    }

    override fun onDisconnected(context: Context?) {
        // try to reconnect
        if(Looper.myLooper() == null)
            Looper.prepare()
        wasDiscon = true
        fineController.connect(btDev)
        if(btDev.bondState == BluetoothDevice.BOND_NONE) {
            toast("cannot connect to device, please restart")
            finish()
        }
    }

    override fun onConnectFailed(context: Context?) {
        //Looper.prepare()
        toast("failed to connect!")
        finish()
    }

    override fun onConnected(context: Context?) {
        if(Looper.myLooper() == null)
            Looper.prepare()
        toast(R.string.connected)
        if(wasDiscon)
            fineController.setContinuousMode(10)
        wasDiscon = false
    }

    val serialService = BLeSerialPortService()
    val fineController = FineController(serialService,this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_main_screen)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FineCompanion"
        listView.adapter = fineSetAdp
        try {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0.0f, locListen)
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0.0f, locListen)
        }
        catch(e: SecurityException )
        {
            toast("Location will be unavailable due to missing permissions")
        }
        serialService.registerCallback(this)
        fineController.connect(btDev)
    }

    override fun onDestroy() {
        super.onDestroy()
        serialService.close()
    }

}

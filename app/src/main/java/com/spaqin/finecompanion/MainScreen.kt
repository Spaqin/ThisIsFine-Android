package com.spaqin.finecompanion

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.spaqin.fineapp.Communications.FineMessage
import com.spaqin.fineapp.Communications.FineMessageCommand
import com.spaqin.fineapp.Sensors.RawSensorData
import com.spaqin.fineapp.Sensors.SensorType
import com.spaqin.finecompanion.Communications.BLeSerialPortService
import com.spaqin.finecompanion.FineDevice.FineActivityCallbacks
import com.spaqin.finecompanion.FineDevice.FineController
import com.spaqin.finecompanion.FineDevice.FineDevice
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.app_bar_main_screen.*
import org.jetbrains.anko.locationManager
import org.jetbrains.anko.toast

class MainScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, BLeSerialPortService.Callback, FineActivityCallbacks {

    var btAddr: String = ""
    private val btManager: BluetoothManager by lazy{ getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val btAdapter: BluetoothAdapter by lazy { btManager.adapter }
    private val btDev: BluetoothDevice by lazy { btAddr = intent.extras.get(BluetoothDevice.EXTRA_DEVICE) as String
        btAdapter.getRemoteDevice(btAddr)
        }

    var current_latitude: Double = 0.0
    var current_longitude: Double = 0.0

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
            textView3.setText("\nlong: " + current_longitude + " lat: " + current_latitude)
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
        Log.d("SensorChange", "sensor: " + s + ": " + FineDevice.sensorData[s.protoValue.toInt()])
        textView2.setText("sensor: " + s + ": " + FineDevice.sensorData[s.protoValue.toInt()])
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
        fineController.connect(btDev)
        if(btDev.bondState == BluetoothDevice.BOND_NONE) {
            toast("cannot connect to device, please restart")
            finish()
        }
    }

    override fun onConnectFailed(context: Context?) {
        toast("failed to connect!")
        finish()
    }

    override fun onConnected(context: Context?) {
        toast(R.string.connected)
        fineController.setContinuousMode(60)
    }

    val serialService = BLeSerialPortService()
    val fineController = FineController(serialService,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        try {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 50.0f, locListen)
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 50.0f, locListen)
        }
        catch(e: SecurityException )
        {
            toast("Location will be unavailable due to missing permissions")
        }
        fineController.connect(btDev)
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_screen, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        serialService.close()
    }
}

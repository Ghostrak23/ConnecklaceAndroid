package fr.isen.albergucci.connecklace

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.bluetooth.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import fr.isen.albergucci.connecklace.databinding.ActivityMainBinding
import java.util.*
import android.view.Window
import android.view.WindowManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    var bluetoothGatt: BluetoothGatt? = null
    var serviceUUID = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
    private val characteristicButtonUUID = UUID.fromString("00001234-8e22-4541-9d4c-21edae82ed19")
    private val characteristicLatitudeUUID = UUID.fromString("0000adda-8e22-4541-9d4c-21edae82ed19")
    private val characteristicLongitudeUUID = UUID.fromString("00005678-8e22-4541-9d4c-21edae82ed19")
    private val configNotifications = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    companion object {
        var latitude: Double = 0.0
        var longitude: Double = 0.0
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val enableBtActivityResult =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val permissionG = permissions.all { it.value }
            val scanBlue = Manifest.permission.BLUETOOTH_SCAN
            if(ContextCompat.checkSelfPermission(
                    this,
                    scanBlue
                ) == PackageManager.PERMISSION_GRANTED){
                Log.e("Scan","OK")
                // startScan()
            }else{
                Log.e("Scan","Pas Ok")
            }
        }

    @SuppressLint("MissingPermission")
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                bluetoothGatt?.discoverServices()
                Log.e("Bluetooth", "Connected to GATT server.")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(serviceUUID)

                /*// Lecture de la caractéristique du bouton
                val characteristicButton = service?.getCharacteristic(characteristicButtonUUID)
                enableNotifications(characteristicButton!!)
                gatt?.readCharacteristic(characteristicButton)*/

                // Lecture de la caractéristique de latitude
                val characteristicLatitude = service?.getCharacteristic(characteristicLatitudeUUID)
                enableNotifications(characteristicLatitude!!)
                gatt?.readCharacteristic(characteristicLatitude)

                // Attente de la fin de la lecture de la caractéristique de latitude
                Handler(Looper.getMainLooper()).postDelayed({
                    // Lecture de la caractéristique de longitude
                    val characteristicLongitude = service?.getCharacteristic(characteristicLongitudeUUID)
                    enableNotifications(characteristicLongitude!!)
                    gatt?.readCharacteristic(characteristicLongitude)
                }, 500) // Attendez 1 seconde avant de lire la caractéristique de longitude
            }
        }


        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d("characteristic uuid", "${characteristic?.uuid}")
            if (characteristic?.uuid == characteristicButtonUUID) {
                val value = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                runOnUiThread {
                    //binding.nombre.text = value.toString()
                }
                Log.d("Bluetooth", "Received value: $value")
            }

            if (characteristic?.uuid == characteristicLatitudeUUID) {
                // Récupérer les données de latitude en tant que tableau de bytes
                val latitudeBytes = characteristic?.value
                if (latitudeBytes != null) {
                    // Convertir les données en String
                    val latitudeString = String(latitudeBytes)

                    // Convertir le String en Double
                    latitude = latitudeString.toDouble()
                    Log.d("AQW", latitude.toString())

                    // Mettre à jour l'interface utilisateur avec la latitude
                    runOnUiThread {
                        updateMapMarker()
                    }
                    Log.d("Test", latitudeString)
                }
            }

            if (characteristic?.uuid == characteristicLongitudeUUID) {
                // Récupérer les données de longitude en tant que tableau de bytes
                val longitudeBytes = characteristic?.value
                if (longitudeBytes != null) {
                    // Convertir les données en String
                    val longitudeString = String(longitudeBytes)

                    // Convertir le String en Double
                    longitude = longitudeString.toDouble()

                    // Mettre à jour l'interface utilisateur avec la longitude
                    runOnUiThread {
                        updateMapMarker()
                    }
                    Log.d("Test", longitudeString)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Cette directive enlève la barre de titre
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Cette directive permet d'enlever la barre de notifications pour afficher l'application en plein écran
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val deviceAddress = "00:80:E1:26:77:72"
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        }else {
            if (permissionGranted()) {
                Log.d("Permission", "OK");
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
            }
            else  {
                enableBtActivityResult.launch(getAllPermissions())
            }
        }

    }

    private fun getAllPermissions(): Array<String> {
        val listOfPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                //Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        return listOfPermission
    }

    private fun permissionGranted(): Boolean {
        val permG = getAllPermissions()
        return permG.all {
            it
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val descriptor = characteristic.getDescriptor(configNotifications)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt?.writeDescriptor(descriptor)
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        updateMapMarker()
    }

    private fun updateMapMarker() {
        val gps = LatLng(latitude, longitude)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(gps).title("Position du chien"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gps))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17f))
    }
}

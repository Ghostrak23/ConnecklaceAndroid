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
import android.os.SystemClock.sleep
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import fr.isen.albergucci.connecklace.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var bluetoothGatt: BluetoothGatt? = null
    var serviceUUID = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
    private val characteristicButtonUUID = UUID.fromString("00001234-8e22-4541-9d4c-21edae82ed19")
    private val characteristicLatitudeUUID = UUID.fromString("0000adda-8e22-4541-9d4c-21edae82ed19")
    private val characteristicLongitudeUUID = UUID.fromString("00005678-8e22-4541-9d4c-21edae82ed19")
    private val configNotifications = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

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
                    val latitude = latitudeString.toDouble()
                    Log.d("AQW", latitude.toString())

                    // Envoyer la latitude à MapsActivity
                    val intent = Intent(applicationContext, MapsActivity::class.java)
                    intent.putExtra("latitude", latitude)

                    val requestCode = 1 // Choisissez le code de requête approprié
                    startActivityForResult(intent, requestCode)

                    // Mettre à jour l'interface utilisateur avec la latitude
                    runOnUiThread {
                        binding.nombre.text = latitudeString
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
                    val longitude = longitudeString.toDouble()

                    // Envoyer la longitude à MapsActivity
                    val intent = Intent(applicationContext, MapsActivity::class.java)
                    intent.putExtra("longitude", longitude)

                    // Mettre à jour l'interface utilisateur avec la longitude
                    runOnUiThread {
                        binding.nombre2.text = longitudeString
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
        setContentView(R.layout.activity_main)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gpsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

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

   /* @SuppressLint("MissingPermission")
    private fun startScan() {
        // Démarrer le scan des appareils Bluetooth
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val deviceAddress = "00:80:E1:26:82:D8"
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
    }*/

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
}

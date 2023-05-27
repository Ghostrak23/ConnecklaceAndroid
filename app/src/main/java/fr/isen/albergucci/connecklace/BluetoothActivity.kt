package fr.isen.albergucci.connecklace

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class BluetoothActivity : AppCompatActivity() {
    /*private val REQUEST_CODE_ENABLE_BT:Int = 1
    private val REQUEST_ENABLE_BT:Int = 2
    private val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 3
    private val PERMISSION_REQUEST_BLUETOOTH_SCAN = 4

    lateinit var bAdapter: BluetoothAdapter
    @SuppressLint("MissingPermission")

    // Déclarer une variable pour stocker les appareils découverts
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    // Déclarer le BroadcastReceiver pour la recherche Bluetooth
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    // Ajouter l'appareil à la liste des appareils découverts et mettre à jour l'adapter
                    if (!discoveredDevices.contains(it)) {
                        discoveredDevices.add(it)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        //Init bluetooth adapter
        bAdapter = BluetoothAdapter.getDefaultAdapter()
        //Check if bluetooth is on/off
        if(bAdapter == null) {
            // Ecrire sur bluetoothAvailable que le bluetooth n'est pas disponible
            findViewById<AppCompatTextView>(R.id.bluetoothAvailable).text = "Bluetooth non disponible"
        }
        else {
            // Ecrire sur bluetoothAvailable que le bluetooth est disponible
            findViewById<AppCompatTextView>(R.id.bluetoothAvailable).text = "Bluetooth disponible"
        }

        //Turn on bluetooth with bluetoothOn button
        findViewById<MaterialButton>(R.id.bluetoothOn).setOnClickListener {
            if(bAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth déjà activé", Toast.LENGTH_SHORT).show()
            }
            else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT)
            }
        }

        //Turn off bluetooth with bluetoothOff button
        findViewById<MaterialButton>(R.id.bluetoothOff).setOnClickListener {
            if(!bAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth déjà désactivé", Toast.LENGTH_SHORT).show()
            }
            else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED) {
                    // L'application a déjà l'autorisation, désactiver le Bluetooth
                    bAdapter.disable()
                    Toast.makeText(this, "Bluetooth désactivé", Toast.LENGTH_SHORT).show()
                } else {
                    // Demander à l'utilisateur d'accorder la permission
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                        PERMISSION_REQUEST_BLUETOOTH_CONNECT)
                }
            }
        }

        // CONNNEXION AVEC LE STM32

        /* val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter == null) {
            // Le dispositif ne supporte pas le Bluetooth
            return
        }
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }


        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val discoveryIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                // Faire quelque chose avec le dispositif Bluetooth trouvé
            }
        }, discoveryIntent)

        bluetoothAdapter.startDiscovery() */


        /*
        // Discoverable bluetooth with bluetoothDiscoverable button
        findViewById<MaterialButton>(R.id.bluetoothDiscover).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED
            ) {
                if (!bAdapter.isDiscovering) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.BLUETOOTH_SCAN
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        // L'application a déjà l'autorisation, rendre le dispositif Bluetooth découvrable
                        Toast.makeText(this, "Making your device discoverable", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                        startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
                    } else {
                        // Demander à l'utilisateur d'accorder la permission
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.BLUETOOTH_SCAN),
                            PERMISSION_REQUEST_BLUETOOTH_SCAN
                        )
                    }
                } else {
                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    startActivity(discoverableIntent)
                }
            }
        }

        //Get list of paired devices with bluetoothPaired button
        findViewById<MaterialButton>(R.id.bluetoothPaired).setOnClickListener {
            if(bAdapter.isEnabled) {
                findViewById<AppCompatTextView>(R.id.pairedDevices).text = "Paired devices"
                //Get list of paired devices
                val devices = bAdapter.bondedDevices
                for(device in devices) {
                    findViewById<AppCompatTextView>(R.id.pairedDevices).append("\nDevice: " + device.name + ", " + device)
                }

            }
            else {
                Toast.makeText(this, "Turn on bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }*/

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_CODE_ENABLE_BT -> {
                if(resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth activé", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "Bluetooth non activé", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }*/
}
package com.example.controlbt

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

abstract class MainActivity : AppCompatActivity() {

    abstract val REQUEST_BLUETOOTH_SCAN_PERMISSION: Int
    private lateinit var mBtAdapter: BluetoothAdapter
    private var mAddressDevices: ArrayAdapter<String>? = null
    private var mNameDevices: ArrayAdapter<String>? = null

    private lateinit var mBluetoothSocket: BluetoothSocket
    private var mIsConnected: Boolean = false
    private lateinit var mAddress: String
    private val mMyUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val REQUEST_BLUETOOTH_PERMISSION = 3

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        val someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.i("MainActivity", "Bluetooth enabled")
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        val idSpinDisp = findViewById<Spinner>(R.id.idSpinDisp)

        val idBtnOnBT = findViewById<Button>(R.id.idBtnOnBT)
        val idBtnOffBT = findViewById<Button>(R.id.idBtnOffBT)
        val idBtnDispBT = findViewById<Button>(R.id.idBtnDispBT)
        val idBtnConnect = findViewById<Button>(R.id.idBtnConect)

        idBtnOnBT.setOnClickListener {
            if (!mBtAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                someActivityResultLauncher.launch(enableBtIntent)
            } else {
                Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_LONG).show()
            }
        }

        idBtnOffBT.setOnClickListener {
            if (!mBtAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth is already disabled", Toast.LENGTH_LONG).show()
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@setOnClickListener
                }
                mBtAdapter.disable()
                Toast.makeText(this, "Bluetooth has been disconected", Toast.LENGTH_LONG).show()
            }
        }

        idBtnDispBT.setOnClickListener {
            if (mBtAdapter.isEnabled) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_BLUETOOTH_PERMISSION
                    )
                } else {
                    val pairedDevices: Set<BluetoothDevice>? = mBtAdapter.bondedDevices
                    mAddressDevices?.clear()
                    mNameDevices?.clear()

                    pairedDevices?.forEach { device ->
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address
                        mAddressDevices?.add(deviceHardwareAddress)
                        mNameDevices?.add(deviceName)
                    }
                    idSpinDisp.adapter = mNameDevices
                }
            } else {
                Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_LONG).show()
            }
        }

        idBtnConnect.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    REQUEST_BLUETOOTH_SCAN_PERMISSION
                )
            } else {
                try {
                    val intValSpin = idSpinDisp.selectedItemPosition
                    mAddress = mAddressDevices?.getItem(intValSpin).toString()
                    Toast.makeText(this, "Connecting to $mAddress", Toast.LENGTH_LONG).show()

                    mBtAdapter.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(mAddress)
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(mMyUUID)
                    mBluetoothSocket.connect()

                    Toast.makeText(this, "Successfully connected", Toast.LENGTH_LONG).show()
                    Log.i("MainActivity", "Successfully connected")
                    mIsConnected = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Connection error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("MainActivity", "Connection error: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("MainActivity", "Unexpected error: ${e.message}")
                }
            }
        }


    }
}

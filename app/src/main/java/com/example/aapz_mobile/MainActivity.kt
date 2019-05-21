package com.example.aapz_mobile

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import com.example.aapz_mobile.R
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private var seconds = 0
    private lateinit var timer: Timer
    private var timerRunning = false

    companion object {
        var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var progress: ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected: Boolean = false
        val address = "B8:27:EB:5D:93:59"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            toast("this device doesn't support bluetooth")
            return
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        connect_button.setOnClickListener {
            ConnectToDevice(this, connect_button).execute()
        }

        startRideButton.setOnClickListener {
            if (!timerRunning) {
                currentLengthTextView.text = "00:00:00"
                seconds = 0
                timer = Timer()
                timer.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            seconds++
                            var x = seconds
                            val secs = x % 60
                            x /= 60
                            val minutes = x % 60
                            x /= 60
                            val hours = x % 24
                            currentLengthTextView.text =
                                String.format("%02d:%02d:%02d", hours, minutes, secs)
                        }
                    },
                    1000, 1000
                )
                timerRunning = true
            }
            sendCommand("1")

        }
        endRideButton.setOnClickListener {
            if (timerRunning) {
                timer.cancel()
                timerRunning = false
            }

            sendCommand("0")
        }
    }

    override fun onStop() {
        disconnect()
        super.onStop()
    }

    private fun sendCommand(input: String) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context, connectBtn: Button) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context
        private val btn: Button

        init {
            this.context = c
            this.btn = connectBtn
        }

        override fun onPreExecute() {
            super.onPreExecute()
            btn.setText(R.string.connecting)
            progress = ProgressDialog.show(
                context,
                context.getString(R.string.connecting),
                context.getString(R.string.please_wait)
            )
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                isConnected = true
                btn.setText(R.string.connect)
                btn.isEnabled = false
            }
            progress.dismiss()
        }
    }

    // "B8:27:EB:5D:93:59"


}

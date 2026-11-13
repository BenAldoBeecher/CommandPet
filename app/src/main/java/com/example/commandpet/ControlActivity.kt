package com.example.commandpet

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ControlActivity : AppCompatActivity() {

    private lateinit var tvEstadoConexion: TextView
    private lateinit var tvEstadoPlato: TextView
    private lateinit var tvDistancia: TextView
    private lateinit var tvDispensadoHoy: TextView
    private lateinit var btnConectar: Button
    private lateinit var btnDispensar: Button
    private lateinit var btnVolver: Button

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private var conectado = false
    private var ultimaDistancia = 0f
    private var ultimoEstado = "---"

    private val uuidSpp: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        tvEstadoConexion = findViewById(R.id.tvEstadoConexion)
        tvEstadoPlato = findViewById(R.id.tvEstadoPlato)
        tvDistancia = findViewById(R.id.tvDistancia)
        tvDispensadoHoy = findViewById(R.id.tvDispensadoHoy)
        btnConectar = findViewById(R.id.btnConectar)
        btnDispensar = findViewById(R.id.btnDispensar)
        btnVolver = findViewById(R.id.btnVolver)

        prefs = getSharedPreferences("CommandPetPrefs", Context.MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        actualizarContadorHoy()

        btnConectar.setOnClickListener {
            if (conectado) {
                desconectar()
            } else {
                verificarPermisosYConectar()
            }
        }

        btnDispensar.setOnClickListener {
            enviarComandoDispensar()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun verificarPermisosYConectar() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 101)
                return
            }
        }

        mostrarDispositivosVinculados()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mostrarDispositivosVinculados()
        } else {
            Toast.makeText(this, "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDispositivosVinculados() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val dispositivos = bluetoothAdapter?.bondedDevices
        if (dispositivos.isNullOrEmpty()) {
            Toast.makeText(this, "No hay dispositivos Bluetooth vinculados", Toast.LENGTH_SHORT).show()
            return
        }

        val listaNombres = ArrayList<String>()
        val listaDispositivos = ArrayList<BluetoothDevice>()

        for (dispositivo in dispositivos) {
            listaNombres.add("${dispositivo.name}\n${dispositivo.address}")
            listaDispositivos.add(dispositivo)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar dispositivo")
        builder.setItems(listaNombres.toTypedArray()) { _, which ->
            conectar(listaDispositivos[which])
        }
        builder.show()
    }

    private fun conectar(dispositivo: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        tvEstadoConexion.text = "Conectando..."
        tvEstadoConexion.setTextColor(Color.parseColor("#FB8C00"))

        Thread {
            try {
                bluetoothSocket = dispositivo.createRfcommSocketToServiceRecord(uuidSpp)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                inputStream = bluetoothSocket?.inputStream
                conectado = true

                runOnUiThread {
                    tvEstadoConexion.text = "Conectado a ${dispositivo.name}"
                    tvEstadoConexion.setTextColor(Color.parseColor("#2E7D32"))
                    btnConectar.text = "Desconectar"
                    btnDispensar.isEnabled = true
                    Toast.makeText(this, "Conexión exitosa", Toast.LENGTH_SHORT).show()
                }

                recibirDatos()
            } catch (e: Exception) {
                desconectar()
                runOnUiThread {
                    tvEstadoConexion.text = "Error al conectar"
                    tvEstadoConexion.setTextColor(Color.parseColor("#D32F2F"))
                    Toast.makeText(this, "No se pudo conectar al HC-05", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun recibirDatos() {
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            while (conectado) {
                val linea = reader.readLine() ?: break
                runOnUiThread {
                    procesarLinea(linea.trim())
                }
            }
        } catch (_: Exception) {
        } finally {
            if (conectado) {
                runOnUiThread {
                    desconectar()
                }
            }
        }
    }

    private fun procesarLinea(linea: String) {
        if (linea.isEmpty()) return

        val partes = linea.split(",")
        val comando = partes[0]

        when (comando) {
            "DATA" -> {
                if (partes.size >= 3) {
                    val distanciaStr = partes[1]
                    val estadoStr = partes[2]

                    ultimaDistancia = distanciaStr.toFloatOrNull() ?: 0f
                    ultimoEstado = estadoStr

                    tvDistancia.text = "Distancia: $distanciaStr cm"
                    tvEstadoPlato.text = estadoStr

                    if (estadoStr == "COMIDA") {
                        tvEstadoPlato.setTextColor(Color.parseColor("#2E7D32"))
                    } else {
                        tvEstadoPlato.setTextColor(Color.parseColor("#D32F2F"))
                    }
                }
            }
            "EVT" -> {
                if (partes.size >= 2) {
                    val evento = partes[1]
                    if (evento == "DISPENSADO_AUTO") {
                        incrementarContador()
                        dbHelper.insertarHistorial("AUTOMÁTICO", ultimaDistancia, ultimoEstado)
                        Toast.makeText(this, "¡Comida dispensada automáticamente!", Toast.LENGTH_SHORT).show()
                    } else if (evento == "DISPENSADO_MANUAL") {
                        incrementarContador()
                        dbHelper.insertarHistorial("MANUAL", ultimaDistancia, ultimoEstado)
                        Toast.makeText(this, "¡Comida dispensada manualmente!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "ERR" -> {
                tvDistancia.text = "Distancia: Sin eco"
            }
            "INFO" -> {
                Toast.makeText(this, "Comedero listo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarComandoDispensar() {
        if (!conectado || outputStream == null) {
            Toast.makeText(this, "No hay conexión con el comedero", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                outputStream?.write("D\n".toByteArray())
                outputStream?.flush()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error al enviar comando", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun desconectar() {
        conectado = false
        try {
            outputStream?.close()
            inputStream?.close()
            bluetoothSocket?.close()
        } catch (_: Exception) {
        }

        bluetoothSocket = null
        outputStream = null
        inputStream = null

        runOnUiThread {
            tvEstadoConexion.text = "Desconectado"
            tvEstadoConexion.setTextColor(Color.parseColor("#D32F2F"))
            btnConectar.text = "Conectar Bluetooth"
            btnDispensar.isEnabled = false
            tvEstadoPlato.text = "---"
            tvEstadoPlato.setTextColor(Color.BLACK)
            tvDistancia.text = "Distancia: -- cm"
        }
    }

    private fun actualizarContadorHoy() {
        val fechaHoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val fechaGuardada = prefs.getString("fecha", "")

        if (fechaGuardada != fechaHoy) {
            prefs.edit()
                .putString("fecha", fechaHoy)
                .putInt("contador", 0)
                .apply()
        }

        val contador = prefs.getInt("contador", 0)
        tvDispensadoHoy.text = "Dispensado hoy: $contador veces"
    }

    private fun incrementarContador() {
        val contador = prefs.getInt("contador", 0) + 1
        prefs.edit().putInt("contador", contador).apply()
        tvDispensadoHoy.text = "Dispensado hoy: $contador veces"
    }

    override fun onDestroy() {
        super.onDestroy()
        desconectar()
        dbHelper.close()
    }
}

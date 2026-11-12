package com.example.commandpet

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var txtHistorial: TextView
    private lateinit var btnVolver: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        txtHistorial = findViewById(R.id.txtHistorial)
        btnVolver = findViewById(R.id.btnVolver)
        dbHelper = DatabaseHelper(this)

        cargarRegistros()

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarRegistros() {
        val lista = dbHelper.obtenerHistorial()

        if (lista.isEmpty()) {
            txtHistorial.text = "Aún no hay registros de dispensación guardados."
        } else {
            val texto = lista.joinToString("\n\n") { item ->
                "• [${item.fechaHora}]\n  Tipo: ${item.tipo} | Distancia: ${item.distancia} cm | Plato: ${item.estado}"
            }
            txtHistorial.text = texto
        }
    }
}

package com.example.commandpet

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var listaView: ListView
    private lateinit var btnVolver: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        listaView = findViewById(R.id.listaHistorial)
        btnVolver = findViewById(R.id.btnVolver)
        dbHelper = DatabaseHelper(this)

        btnVolver.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val lista = dbHelper.obtenerHistorial()

        if (lista.isEmpty()) {
            val sinDatos = listOf("Aún no hay registros de dispensación guardados.")
            listaView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sinDatos)
        } else {
            val textos = lista.map { item ->
                "[${item.fechaHora}]\nTipo: ${item.tipo} | Distancia: ${item.distancia} cm | Plato: ${item.estado}"
            }
            listaView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, textos)
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

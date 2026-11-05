package com.example.commandpet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val btnComenzar = findViewById<Button>(R.id.btnComenzar)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        val usuario = intent.getStringExtra("USUARIO") ?: "Usuario"
        tvBienvenida.text = "¡Hola, $usuario!"

        btnComenzar.setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }

        btnHistorial.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

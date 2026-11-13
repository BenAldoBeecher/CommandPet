package com.example.commandpet

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var edtNombre: EditText
    private lateinit var edtApellido: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtUsuarioRegistro: EditText
    private lateinit var edtPasswordRegistro: EditText
    private lateinit var btnConfirmarRegistro: Button
    private lateinit var btnVolverLogin: Button

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        edtNombre = findViewById(R.id.edtNombre)
        edtApellido = findViewById(R.id.edtApellido)
        edtEmail = findViewById(R.id.edtEmail)
        edtUsuarioRegistro = findViewById(R.id.edtUsuarioRegistro)
        edtPasswordRegistro = findViewById(R.id.edtPasswordRegistro)
        btnConfirmarRegistro = findViewById(R.id.btnConfirmarRegistro)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)

        dbHelper = DatabaseHelper(this)

        btnConfirmarRegistro.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val apellido = edtApellido.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val usuario = edtUsuarioRegistro.text.toString().trim()
            val password = edtPasswordRegistro.text.toString().trim()

            if (nombre.isEmpty()) {
                edtNombre.error = "Ingrese su nombre"
                return@setOnClickListener
            }
            if (apellido.isEmpty()) {
                edtApellido.error = "Ingrese su apellido"
                return@setOnClickListener
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = "Ingrese un correo válido"
                return@setOnClickListener
            }
            if (usuario.isEmpty()) {
                edtUsuarioRegistro.error = "Ingrese un nombre de usuario"
                return@setOnClickListener
            }
            if (password.length < 4) {
                edtPasswordRegistro.error = "La contraseña debe tener al menos 4 caracteres"
                return@setOnClickListener
            }

            val registrado = dbHelper.registrarUsuario(nombre, apellido, email, usuario, password)
            if (registrado) {
                Toast.makeText(this, "Cuenta creada con éxito. Inicia sesión", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "El nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show()
            }
        }

        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

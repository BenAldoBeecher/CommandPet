package com.example.commandpet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtUsuario = findViewById(R.id.edtUsuario)
        edtPassword = findViewById(R.id.edtPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        dbHelper = DatabaseHelper(this)

        btnIngresar.setOnClickListener {
            val usuario = edtUsuario.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (usuario.isEmpty()) {
                edtUsuario.error = "Debe ingresar el usuario"
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                edtPassword.error = "Debe ingresar la contraseña"
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginValido = dbHelper.validarUsuario(usuario, password)

            if (loginValido) {
                Toast.makeText(this, "¡Bienvenido $usuario!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USUARIO", usuario)
                startActivity(intent)

                finish()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegistrar.setOnClickListener {
            val usuario = edtUsuario.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa usuario y contraseña para registrarte", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 4) {
                edtPassword.error = "La contraseña debe tener al menos 4 caracteres"
                return@setOnClickListener
            }

            val registrado = dbHelper.registrarUsuario(usuario, password)

            if (registrado) {
                Toast.makeText(this, "Usuario registrado con éxito. ¡Ahora presiona Ingresar!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "El nombre de usuario ya está registrado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

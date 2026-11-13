package com.example.commandpet

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "commandpet.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                apellido TEXT,
                email TEXT,
                usuario TEXT UNIQUE,
                password_hash TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE historial (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha_hora TEXT,
                tipo TEXT,
                distancia REAL,
                estado TEXT
            )
        """.trimIndent())

        val cv = ContentValues().apply {
            put("nombre", "Administrador")
            put("apellido", "Sistema")
            put("email", "admin@commandpet.com")
            put("usuario", "admin")
            put("password_hash", cifrarSHA256("admin123"))
        }
        db.insert("usuarios", null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS historial")
        onCreate(db)
    }

    fun cifrarSHA256(texto: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(texto.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun registrarUsuario(
        nombre: String,
        apellido: String,
        email: String,
        usuario: String,
        contrasena: String
    ): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("nombre", nombre)
            put("apellido", apellido)
            put("email", email)
            put("usuario", usuario)
            put("password_hash", cifrarSHA256(contrasena))
        }
        val resultado = db.insert("usuarios", null, cv)
        return resultado != -1L
    }

    fun validarUsuario(usuario: String, contrasena: String): Boolean {
        val db = readableDatabase
        val hash = cifrarSHA256(contrasena)

        val cursor = db.rawQuery(
            "SELECT * FROM usuarios WHERE usuario = ? AND password_hash = ?",
            arrayOf(usuario, hash)
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun insertarHistorial(tipo: String, distancia: Float, estado: String): Boolean {
        val db = writableDatabase
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val cv = ContentValues().apply {
            put("fecha_hora", fechaActual)
            put("tipo", tipo)
            put("distancia", distancia)
            put("estado", estado)
        }
        val resultado = db.insert("historial", null, cv)
        return resultado != -1L
    }

    fun obtenerHistorial(): List<Dispensacion> {
        val lista = ArrayList<Dispensacion>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, fecha_hora, tipo, distancia, estado FROM historial ORDER BY id DESC",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val item = Dispensacion(
                    id = cursor.getInt(0),
                    fechaHora = cursor.getString(1),
                    tipo = cursor.getString(2),
                    distancia = cursor.getFloat(3),
                    estado = cursor.getString(4)
                )
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}

package com.example.commandpet

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "commandpet.db"
        private const val DATABASE_VERSION = 1

        const val TABLA_USUARIOS = "usuarios"
        const val COL_ID_USUARIO = "id"
        const val COL_USUARIO = "usuario"
        const val COL_PASSWORD = "password_hash"

        const val TABLA_HISTORIAL = "historial"
        const val COL_ID_HISTORIAL = "id"
        const val COL_FECHA_HORA = "fecha_hora"
        const val COL_TIPO = "tipo"
        const val COL_DISTANCIA = "distancia"
        const val COL_ESTADO = "estado"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCrearUsuarios = """
            CREATE TABLE $TABLA_USUARIOS (
                $COL_ID_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USUARIO TEXT UNIQUE,
                $COL_PASSWORD TEXT
            )
        """.trimIndent()
        db.execSQL(sqlCrearUsuarios)

        val sqlCrearHistorial = """
            CREATE TABLE $TABLA_HISTORIAL (
                $COL_ID_HISTORIAL INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FECHA_HORA TEXT,
                $COL_TIPO TEXT,
                $COL_DISTANCIA REAL,
                $COL_ESTADO TEXT
            )
        """.trimIndent()
        db.execSQL(sqlCrearHistorial)

        val cv = ContentValues().apply {
            put(COL_USUARIO, "admin")
            put(COL_PASSWORD, cifrarSHA256("admin123"))
        }
        db.insert(TABLA_USUARIOS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_HISTORIAL")
        onCreate(db)
    }

    fun cifrarSHA256(texto: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(texto.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun registrarUsuario(usuario: String, contrasena: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USUARIO, usuario)
            put(COL_PASSWORD, cifrarSHA256(contrasena))
        }
        val resultado = db.insert(TABLA_USUARIOS, null, cv)
        return resultado != -1L
    }

    fun validarUsuario(usuario: String, contrasena: String): Boolean {
        val db = readableDatabase
        val hash = cifrarSHA256(contrasena)

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLA_USUARIOS WHERE $COL_USUARIO = ? AND $COL_PASSWORD = ?",
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
            put(COL_FECHA_HORA, fechaActual)
            put(COL_TIPO, tipo)
            put(COL_DISTANCIA, distancia)
            put(COL_ESTADO, estado)
        }
        val resultado = db.insert(TABLA_HISTORIAL, null, cv)
        return resultado != -1L
    }

    fun obtenerHistorial(): List<Dispensacion> {
        val lista = ArrayList<Dispensacion>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID_HISTORIAL, $COL_FECHA_HORA, $COL_TIPO, $COL_DISTANCIA, $COL_ESTADO FROM $TABLA_HISTORIAL ORDER BY $COL_ID_HISTORIAL DESC",
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

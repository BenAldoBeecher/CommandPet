package com.example.commandpet

data class Dispensacion(
    val id: Int,
    val fechaHora: String,
    val tipo: String,
    val distancia: Float,
    val estado: String
)

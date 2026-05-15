package com.example.waterdropapp.data.firebase.model

data class UsuarioMarket(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val fotoUrl: String = "",
    val ciudad: String = "",
    val calificacion: Double = 0.0,
    val fechaRegistro: Long = 0L
)

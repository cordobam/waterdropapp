package com.example.waterdropapp.data.firebase.model

data class Publicacion(
    val id: String = "",
    val usuarioId: String = "",
    val nombreUsuario: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",       // "planta", "semilla", "esqueje"
    val precio: Double = 0.0,
    val aceptaTrueque: Boolean = false,
    val imagenUrl: String = "",
    val ciudad: String = "",
    val barrio: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val activa: Boolean = true,
    val fechaPublicacion: Long = 0L
)

package com.example.waterdropapp.data.firebase.model

data class Vivero(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val direccion: String = "",
    val ciudad: String = "",
    val barrio: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val telefono: String = "",
    val horario: String = "",
    val especialidades: List<String> = emptyList(),
    val imagenUrl: String = "",
    val rating: Double = 0.0,
    val activo: Boolean = true,
    val suscripcionActiva: Boolean = false
)

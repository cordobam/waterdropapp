package com.example.waterdropapp.data.local.dto

data class RiegoHistorialDTO(
    val nombrePlanta: String,
    val fechaRiego: String,
    val diasDesdeUltimo: Int?,
    val alerta: Int
)
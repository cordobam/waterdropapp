package com.example.waterdropapp.data.dto

data class RiegoHistorialDTO(
    val nombrePlanta: String,
    val fechaRiego: String,
    val diasDesdeUltimo: Int?,
    val alerta: Int
)
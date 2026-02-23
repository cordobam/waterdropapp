package com.example.waterdropapp.data

data class EstadoPlantasDTO(
    val plantaId: Int,
    val nombre: String,
    val ultimoRiego: String?,
    val diasSinRegar: Int,
    val necesitaRiego: Boolean,
    val nombreGrupos: String?
)

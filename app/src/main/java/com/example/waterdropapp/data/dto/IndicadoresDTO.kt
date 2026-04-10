package com.example.waterdropapp.data.dto

data class IndicadoresDTO(
    val total: Int,
    val necesitanRiego: Int,
    val noNecesitanRiego: Int,
    val promedioDiasRiego: Double,
    val promedioTardanza: Double
)

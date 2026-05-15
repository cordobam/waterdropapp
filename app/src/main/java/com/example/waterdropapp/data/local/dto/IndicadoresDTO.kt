package com.example.waterdropapp.data.local.dto

data class IndicadoresDTO(
    val total: Int,
    val necesitanRiego: Int,
    val noNecesitanRiego: Int,
    val promedioDiasRiego: Double,
    val promedioTardanza: Double
)

package com.example.waterdropapp.data.local.dto

data class RiegoConUmbral(
    val fecha: String,
    val umbralDias: Int?
)

data class RiegosPlantaDTO(
    val plantaId: Int,
    val maxVerano: Int,
    val maxInvierno: Int,
    val riegos: List<RiegoConUmbral>
)

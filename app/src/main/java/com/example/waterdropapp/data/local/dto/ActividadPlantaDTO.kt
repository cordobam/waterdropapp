package com.example.waterdropapp.data.local.dto

import com.example.waterdropapp.domain.model.TipoActividad

data class ActividadPlantaDTO(
    val plantaId: Int,
    val nombrePlanta: String,
    val tipo: TipoActividad,
    val fecha: String,
    val nota: String?,
    val diasDesdeUltimo: Int?,
    val alerta: Int
)
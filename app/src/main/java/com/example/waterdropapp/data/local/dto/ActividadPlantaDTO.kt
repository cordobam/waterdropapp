package com.example.waterdropapp.data.local.dto

import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.TipoActividad

data class ActividadPlantaDTO(
    val plantaId: Int,
    val nombrePlanta: String,
    val tipo: TipoActividad,
    val fecha: String,
    val nota: String?,
    val diasDesdeUltimo: Int?,
    val alerta: Int,
    val umbralDias: Int,
    val estacion: Estacion?
)

package com.example.waterdropapp.data.local.model

data class Plantas(
    val planta_id: Int,
    val nombre: String,
    val dias_max_sin_riego: Int?,
    val dias_max_sin_riego_invierno: Int?,
    val activo: Int?,
    val imagen_path: String?,
    val grupo_id: Int?
)

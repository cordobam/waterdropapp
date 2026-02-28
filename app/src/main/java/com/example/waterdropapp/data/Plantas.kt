package com.example.waterdropapp.data

data class Plantas(
    val planta_id: Int,
    val nombre: String,
    val dias_max_sin_riego: Int?,
    val activo: Int?,
    val imagen_path: String?

)

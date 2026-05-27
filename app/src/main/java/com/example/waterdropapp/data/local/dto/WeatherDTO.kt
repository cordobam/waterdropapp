package com.example.waterdropapp.data.local.dto

data class WeatherDTO(
    val ciudad: String,
    val temperaturas: List<TemperaturaDiaria>
)

data class TemperaturaDiaria(
    val fecha: String,
    val min: Double?,
    val max: Double?
)

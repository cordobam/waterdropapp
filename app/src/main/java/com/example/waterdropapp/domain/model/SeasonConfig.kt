package com.example.waterdropapp.domain.model

enum class SeasonMode {
    AUTO, MANUAL
}

data class SeasonConfig(
    val modo: SeasonMode = SeasonMode.AUTO,
    val ciudad: String = "Córdoba",
    val umbralCalorMax: Double = 25.0,
    val umbralFrioMin: Double = 5.0,
    val estacionManual: Estacion? = null
)
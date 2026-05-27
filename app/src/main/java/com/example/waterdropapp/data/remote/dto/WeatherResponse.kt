package com.example.waterdropapp.data.remote.dto
import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val daily: DailyData
)

data class DailyData(
    val time: List<String>,               // fechas
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double?>,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double?>
)

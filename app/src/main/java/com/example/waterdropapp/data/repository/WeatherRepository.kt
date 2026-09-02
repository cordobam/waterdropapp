package com.example.waterdropapp.data.repository

import com.example.waterdropapp.data.local.dto.TemperaturaDiaria
import com.example.waterdropapp.data.local.dto.WeatherDTO
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.remote.api.WeatherApi

class WeatherRepository {

    private val geocodingApi = WeatherApi.createGeocodingApi()
    private val weatherApi = WeatherApi.createWeatherApi()

    suspend fun getWeeklyTemperatures(provincia: String): WeatherDTO {
        return getWeeklyTemperatures(provincia, null)
    }

    suspend fun getWeeklyTemperatures(provincia: String, weatherDb: DatabaseHelperWeather?): WeatherDTO {
        // 1. Buscar lat/lon por nombre de provincia
        val geoResult = geocodingApi.searchLocation(provincia)
        val location = geoResult.results?.firstOrNull()
            ?: throw Exception("No se encontró la provincia: $provincia")

        // 2. Obtener temperaturas de los ultimos 7 dias
        val forecast = weatherApi.getForecast(location.latitude, location.longitude)

        // 3. Mapear a WeatherDTO
        val temps = forecast.daily.time.mapIndexed { index, fecha ->
            TemperaturaDiaria(
                fecha = fecha,
                min = forecast.daily.temperatureMin.getOrNull(index),
                max = forecast.daily.temperatureMax.getOrNull(index)
            )
        }

        val dto = WeatherDTO(
            ciudad = location.name,
            temperaturas = temps
        )

        // Guardar en caché si se proporciona weatherDb
        weatherDb?.let { db ->
            dto.temperaturas.forEach { temp ->
                db.insertOrReplace(temp.fecha, temp.min, temp.max, dto.ciudad)
            }
        }

        return dto
    }
}
package com.example.waterdropapp.data.remote.api

import com.example.waterdropapp.data.remote.dto.GeocodingResponse
import com.example.waterdropapp.data.remote.dto.WeatherResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 1
    ): GeocodingResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse

    companion object {
        private const val GEO_BASE_URL = "https://geocoding-api.open-meteo.com/v1/"
        private const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/"

        fun createGeocodingApi(): WeatherApi {
            return Retrofit.Builder()
                .baseUrl(GEO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
                .create(WeatherApi::class.java)
        }

        fun createWeatherApi(): WeatherApi {
            return Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
                .create(WeatherApi::class.java)
        }
    }
}
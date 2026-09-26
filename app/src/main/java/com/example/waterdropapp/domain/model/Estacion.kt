package com.example.waterdropapp.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Month

enum class Estacion(val etiqueta: String) {
    PRIMAVERA("Primavera"),
    VERANO("Verano"),
    OTONO("Otoño"),
    INVIERNO("Invierno");

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun porMes(fecha: LocalDate): Estacion {
            val dia = fecha.dayOfMonth
            val mes = fecha.month

            // Usamos las fechas aproximadas de los cambios de estación (día 21)
            return when (mes) {
                Month.JANUARY, Month.FEBRUARY -> VERANO
                Month.MARCH -> if (dia < 21) VERANO else OTONO
                Month.APRIL, Month.MAY -> OTONO
                Month.JUNE -> if (dia < 21) OTONO else INVIERNO
                Month.JULY, Month.AUGUST -> INVIERNO
                Month.SEPTEMBER -> if (dia < 21) INVIERNO else PRIMAVERA
                Month.OCTOBER, Month.NOVEMBER -> PRIMAVERA
                Month.DECEMBER -> if (dia < 21) PRIMAVERA else VERANO
            }
        }
    }
}

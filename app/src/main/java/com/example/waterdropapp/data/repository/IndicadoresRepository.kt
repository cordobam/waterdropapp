package com.example.waterdropapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.local.dto.IndicadoresDTO
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class IndicadoresRepository(private val db: DBHelper) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIndicadores(): IndicadoresDTO {
        val lista = db.obtenerEstadoPlantas()

        val total = lista.size
        val necesitanRiego = lista.count { it.necesitaRiego }
        val noNecesitanRiego = lista.count { !it.necesitaRiego }
        val promedioDiasRiego = calcularPromedioDiasEntreRiegos()
        val promedioTardanza = calcularPromedioTardanza()

        return IndicadoresDTO(
            total = total,
            necesitanRiego = necesitanRiego,
            noNecesitanRiego = noNecesitanRiego,
            promedioDiasRiego = promedioDiasRiego,
            promedioTardanza = promedioTardanza
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularPromedioDiasEntreRiegos(): Double {
        val plantas = db.obtenerRiegosPorPlanta()

        val promedios = plantas.mapNotNull { planta ->

            val fechas = planta.fechas

            if (fechas.size < 2) return@mapNotNull null

            val diferencias = mutableListOf<Long>()

            for (i in 1 until fechas.size) {
                val dias = calcularDiasEntre(fechas[i - 1], fechas[i])
                diferencias.add(dias)
            }

            diferencias.average()
        }

        return if (promedios.isNotEmpty()) promedios.average() else 0.0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularPromedioTardanza(): Double {
        val plantas = db.obtenerRiegosPorPlanta()

        val tardanzas = plantas.mapNotNull { planta ->

            if (planta.fechas.size < 2) return@mapNotNull null

            val diferencias = mutableListOf<Long>()

            for (i in 1 until planta.fechas.size) {
                val dias = calcularDiasEntre(planta.fechas[i - 1], planta.fechas[i])
                diferencias.add(dias)
            }

            val promedioRiego = diferencias.average()

            val tardanza = (promedioRiego - planta.diasMax).coerceAtLeast(0.0)

            tardanza
        }

        return if (tardanzas.isNotEmpty()) tardanzas.average() else 0.0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularDiasEntre(f1: String, f2: String): Long {
        if (f1.isBlank() || f2.isBlank()) return 0

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val fecha1 = LocalDate.parse(f1, formatter)
        val fecha2 = LocalDate.parse(f2, formatter)

        return ChronoUnit.DAYS.between(fecha1, fecha2)
    }
}
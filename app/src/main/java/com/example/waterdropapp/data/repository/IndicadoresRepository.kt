package com.example.waterdropapp.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.dto.IndicadoresDTO
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.umbral
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.waterdropapp.data.repository.PlantaRepository
import com.example.waterdropapp.data.repository.RiegoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IndicadoresRepository(
    private val plantaRepo: PlantaRepository,
    private val riegoRepo: RiegoRepository,
    private val context: Context
) {

    private val seasonRepository: SeasonRepository? by lazy {
        val weatherDb = DatabaseHelperWeather(context)
        val weatherRepo = WeatherRepository()
        val prefs = SeasonPrefs.create(context)
        SeasonRepository(weatherDb, weatherRepo, prefs, context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun resolverEstacion(): Estacion =
        seasonRepository?.getCurrentSeason() ?: Estacion.porMes(LocalDate.now())

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getIndicadores(): IndicadoresDTO {
        val lista = withContext(Dispatchers.IO) {
            plantaRepo.obtenerEstadoPlantas()
        }

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
    suspend fun calcularPromedioDiasEntreRiegos(): Double {
        val plantas = riegoRepo.obtenerRiegosPorPlanta()

        val promedios = plantas.mapNotNull { planta ->

            val riegos = planta.riegos

            if (riegos.size < 2) return@mapNotNull null

            val diferencias = mutableListOf<Long>()

            for (i in 1 until riegos.size) {
                val dias = calcularDiasEntre(riegos[i - 1].fecha, riegos[i].fecha)
                diferencias.add(dias)
            }

            diferencias.average()
        }

        return if (promedios.isNotEmpty()) promedios.average() else 0.0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun calcularPromedioTardanza(): Double {
        val estacionActual = resolverEstacion()
        val plantas = riegoRepo.obtenerRiegosPorPlanta()

        val tardanzas = plantas.mapNotNull { planta ->

            val riegos = planta.riegos

            if (riegos.size < 2) return@mapNotNull null

            var sumaExcesos = 0.0
            var intervalos = 0

            for (i in 1 until riegos.size) {
                val dias = calcularDiasEntre(riegos[i - 1].fecha, riegos[i].fecha)
                val umbralAplicado = riegos[i - 1].umbralDias
                    ?: estacionActual.umbral(planta.maxVerano, planta.maxInvierno)

                sumaExcesos += (dias - umbralAplicado).coerceAtLeast(0L)
                intervalos++
            }

            if (intervalos == 0) null else sumaExcesos / intervalos
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
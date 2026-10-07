package com.example.waterdropapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterdropapp.R
import com.example.waterdropapp.data.local.model.DatabaseHelperWeather
import com.example.waterdropapp.data.local.prefs.SeasonPrefs
import com.example.waterdropapp.data.repository.SeasonRepository
import com.example.waterdropapp.data.repository.SeasonChangeResult
import com.example.waterdropapp.data.repository.WeatherRepository
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.SeasonMode

class SeasonCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val prefs = SeasonPrefs.create(applicationContext)

        if (prefs.load().modo == SeasonMode.MANUAL) {
            return Result.success()
        }

        if (!prefs.shouldCheckNow()) {
            return Result.success()
        }

        val weatherDb = DatabaseHelperWeather(applicationContext)
        val weatherRepo = WeatherRepository()
        val seasonRepo = SeasonRepository(weatherDb, weatherRepo, prefs, applicationContext)

        return try {
            val config = prefs.load()
            val refreshSuccess = seasonRepo.refreshWeatherCache(config.ciudad)

            if (!refreshSuccess) {
                return Result.retry()
            }

            val result = seasonRepo.checkSeasonChange()

            if (result is SeasonChangeResult.Changed) {
                mostrarNotificacionCambioEstacion(result.from, result.to)
            }

            prefs.markChecked()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarNotificacionCambioEstacion(from: com.example.waterdropapp.domain.model.Estacion?, to: com.example.waterdropapp.domain.model.Estacion) {
        val channelId = "estacion_channel"

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                applicationContext.getString(R.string.notif_channel_season),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val fromText = from?.let { applicationContext.getString(seasonRes(it)) }
            ?: applicationContext.getString(R.string.ajustes_unknown_season)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_planta)
            .setContentTitle(applicationContext.getString(R.string.notif_season_title))
            .setContentText(
                applicationContext.getString(
                    R.string.notif_season_text,
                    fromText,
                    applicationContext.getString(seasonRes(to))
                )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify("estacion_change".hashCode(), notification)
    }

    private fun seasonRes(estacion: Estacion): Int = when (estacion) {
        Estacion.PRIMAVERA -> R.string.season_primavera
        Estacion.VERANO -> R.string.season_verano
        Estacion.OTONO -> R.string.season_otono
        Estacion.INVIERNO -> R.string.season_invierno
    }
}
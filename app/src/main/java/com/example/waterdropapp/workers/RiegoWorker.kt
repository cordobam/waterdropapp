package com.example.waterdropapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.waterdropapp.R
import com.example.waterdropapp.data.local.model.DBHelper
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.repository.PlantaRepository

class RiegoWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        Log.d("RiegoWorker", "Worker ejecutado")

        val plantasRepo = PlantaRepository( DBHelper(applicationContext))
        val plantas = plantasRepo.obtenerEstadoPlantas() // con fechaUltimoRiego

        plantas.forEach { planta ->
            if (planta.necesitaRiego) {
                mostrarNotificacion(planta.nombre)
            }
        }

        return Result.success()

    }

    private fun mostrarNotificacion(nombrePlanta: String) {
        val channelId = "riego_channel"

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Riegos",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_planta)
            .setContentTitle("🌱 Hora de regar")
            .setContentText("Tenés que regar $nombrePlanta")
            .setAutoCancel(true)
            .build()

        manager.notify(nombrePlanta.hashCode(),notification)
    }
}
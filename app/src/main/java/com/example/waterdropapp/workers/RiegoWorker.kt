package com.example.waterdropapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.waterdropapp.R
import com.example.waterdropapp.data.DBHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
import android.util.Log

class RiegoWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("RiegoWorker", "Worker ejecutado")

        val db = DBHelper(applicationContext)
        val plantas = db.obtenerEstadoPlantas() // con fechaUltimoRiego


        val hoy = System.currentTimeMillis()
        val sieteDias = 7 * 24 * 60 * 60 * 1000L
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        plantas.forEach { planta ->
            val fecha = sdf.parse(planta.ultimoRiego)
            val ahora = System.currentTimeMillis()

            val dias = (ahora - fecha.time) / (1000 * 60 * 60 * 24)

            if (dias >= 7) {
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
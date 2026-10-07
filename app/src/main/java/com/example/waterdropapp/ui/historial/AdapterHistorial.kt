package com.example.waterdropapp.ui.historial

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.local.dto.ActividadPlantaDTO
import com.example.waterdropapp.domain.model.Estacion
import com.example.waterdropapp.domain.model.TipoActividad
import kotlin.collections.addAll

class AdapterHistorial : RecyclerView.Adapter<AdapterHistorial.HistorialViewHolder>() {

    private val items = mutableListOf<ActividadPlantaDTO>()

    fun submitList(lista: List<ActividadPlantaDTO>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_riego, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitulo = itemView.findViewById<TextView>(R.id.tv_titulo)
        private val tvSubTitulo = itemView.findViewById<TextView>(R.id.tv_subtitulo)
        private val tvFecha = itemView.findViewById<TextView>(R.id.tv_fecha)

        private val imgDot = itemView.findViewById<ImageView>(R.id.img_dot)
        fun bind(dto: ActividadPlantaDTO) {
            val ctx = itemView.context
            tvTitulo.text = ctx.getString(R.string.historial_title_done,
                ctx.getString(tipoRes(dto.tipo)), dto.nombrePlanta)
            tvFecha.text = ctx.getString(R.string.historial_date_label, dto.fecha)

            imgDot.setColorFilter(colorDot(dto))

            tvSubTitulo.text = when (dto.tipo) {
                TipoActividad.RIEGO -> subtituloRiego(dto)

                else -> dto.nota?.let { ctx.getString(R.string.historial_note, it) }
                    ?: ctx.getString(R.string.historial_registered)
            }
        }

        private fun colorDot(dto: ActividadPlantaDTO): Int {
            return if (dto.tipo == TipoActividad.RIEGO) {
                colorSegunAlerta(dto.alerta)
            } else {
                colorSegunTipo(dto.tipo)
            }
        }

        private fun colorSegunAlerta(alerta: Int): Int {
            return when (alerta) {
                0 -> Color.parseColor("#4CAF50")
                1 -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
        }

        private fun subtituloRiego(dto: ActividadPlantaDTO): String {
            val ctx = itemView.context
            val dias = dto.diasDesdeUltimo
                ?: return ctx.getString(R.string.historial_last_watering)
            val estacion = dto.estacion?.let {
                ctx.getString(R.string.historial_season_suffix, ctx.getString(seasonRes(it)).lowercase())
            } ?: ""
            return when (dto.alerta) {
                0 -> ctx.getString(R.string.historial_riego_ok, dias, dto.umbralDias, estacion)
                1 -> ctx.getString(R.string.historial_riego_mild, dias, dto.umbralDias, estacion)
                else -> ctx.getString(R.string.historial_riego_late, dias, dto.umbralDias, estacion)
            }
        }

        private fun tipoRes(tipo: TipoActividad): Int = when (tipo) {
            TipoActividad.RIEGO -> R.string.tipo_riego
            TipoActividad.ABONADO -> R.string.tipo_abonado
            TipoActividad.PODADO -> R.string.tipo_podado
        }

        private fun seasonRes(estacion: Estacion): Int = when (estacion) {
            Estacion.PRIMAVERA -> R.string.season_primavera
            Estacion.VERANO -> R.string.season_verano
            Estacion.OTONO -> R.string.season_otono
            Estacion.INVIERNO -> R.string.season_invierno
        }

        private fun colorSegunTipo(tipo: TipoActividad): Int {
            return when (tipo) {
                TipoActividad.RIEGO -> Color.parseColor("#4CAF50")
                TipoActividad.ABONADO -> Color.parseColor("#FF9800")
                TipoActividad.PODADO -> Color.parseColor("#9C27B0")
            }
        }
    }
}
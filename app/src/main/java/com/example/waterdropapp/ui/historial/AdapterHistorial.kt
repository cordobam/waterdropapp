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
            tvTitulo.text = "${dto.tipo.etiqueta} completado de: ${dto.nombrePlanta}"
            tvFecha.text = "Fecha: ${dto.fecha}"

            imgDot.setColorFilter(colorSegunTipo(dto.tipo))

            tvSubTitulo.text = when (dto.tipo) {
                TipoActividad.RIEGO -> dto.diasDesdeUltimo?.let {
                    "Pasaron $it días desde el riego anterior"
                } ?: "Último riego registrado"

                else -> dto.nota?.let { "Nota: $it" } ?: "Actividad registrada"
            }
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
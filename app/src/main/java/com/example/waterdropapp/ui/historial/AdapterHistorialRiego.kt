package com.example.waterdropapp.ui.historial

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.RiegoHistorialDTO

class AdapterHistorial : RecyclerView.Adapter<AdapterHistorial.HistorialViewHolder>() {

    private val items = mutableListOf<RiegoHistorialDTO>()

    fun submitList(lista: List<RiegoHistorialDTO>) {
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
        fun bind(dto: RiegoHistorialDTO) {
            tvTitulo.text = "Riego Completado de: ${dto.nombrePlanta}"
            tvFecha.text = "Fecha: ${dto.fechaRiego}"

            val color = when (dto.alerta) {
                0 -> Color.parseColor("#4CAF50") // verde
                1 -> Color.parseColor("#FF9800") // naranja
                else -> Color.parseColor("#F44336") // rojo
            }

            imgDot.setColorFilter(color)

            tvSubTitulo.text = dto.diasDesdeUltimo?.let {
                "Pasaron $it días desde el último riego"
            } ?: "Primer riego"
        }
    }
}
package com.example.waterdropapp.ui.plantas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.EstadoPlantasDTO

class AdapterPlantas(
    private val onRegarClick: (Int) -> Unit
) : RecyclerView.Adapter<AdapterPlantas.PlantaViewHolder>() {

    private val items = mutableListOf<EstadoPlantasDTO>()

    fun submitList(lista: List<EstadoPlantasDTO>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class PlantaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombre)
        private val tvDias = itemView.findViewById<TextView>(R.id.tvDias)
        private val btnRegar = itemView.findViewById<Button>(R.id.btnRegar)

        fun bind(dto: EstadoPlantasDTO) {
            tvNombre.text = dto.nombre
            tvDias.text = "Días sin regar: ${dto.diasSinRegar}"

            if (dto.necesitaRiego) {
                tvDias.setTextColor(Color.RED)
            } else {
                tvDias.setTextColor(Color.GREEN)
            }

            btnRegar.setOnClickListener {
                onRegarClick(dto.plantaId)
            }
        }
    }
}
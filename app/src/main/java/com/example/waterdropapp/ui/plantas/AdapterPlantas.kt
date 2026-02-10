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
    private val modo: Modo,
    private val onRegarClick: ((Int) -> Unit)? = null,
    private val onEditarPlanta: ((Int) -> Unit)? = null,
    private val onEliminarPlanta: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Modo {
        MOSTRAR_PLANTAS,
        ACTUALIZAR_PLANTAS
    }
    private val items = mutableListOf<EstadoPlantasDTO>()

    fun submitList(lista: List<EstadoPlantasDTO>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType) {

            1 -> {
                val view = inflater.inflate(R.layout.item_plantas, parent, false)
                PlantaViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_planta_act, parent, false)
                PlantaActViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(modo) {
             Modo.MOSTRAR_PLANTAS-> 1
            Modo.ACTUALIZAR_PLANTAS -> 2
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val plantas = items[position]

        when(holder) {
            is  PlantaViewHolder-> holder.bind(plantas)
            is  PlantaActViewHolder-> holder.bind(plantas)
        }
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
                tvDias.setTextColor(Color.parseColor("#C62828"))
            } else {
                tvDias.setTextColor(Color.parseColor("#2E7D32"))
            }

            btnRegar.setOnClickListener {
                onRegarClick?.invoke(dto.plantaId)
            }
        }
    }

    inner class PlantaActViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val nombrePlanta = itemView.findViewById<TextView>(R.id.tvNombrePlantaAct)
        private val botonActualizarPlanta = itemView.findViewById<Button>(R.id.btnActualizarPlantaAct)
        private val botonEliminarPlanta = itemView.findViewById<Button>(R.id.btnEliminarPlanta)
        fun bind(dto: EstadoPlantasDTO){
            nombrePlanta.text = dto.nombre
            botonActualizarPlanta.setOnClickListener {
                onEditarPlanta?.invoke(dto.plantaId)
            }
            botonEliminarPlanta.setOnClickListener {
                onEliminarPlanta?.invoke(dto.plantaId)
            }

        }
    }
}
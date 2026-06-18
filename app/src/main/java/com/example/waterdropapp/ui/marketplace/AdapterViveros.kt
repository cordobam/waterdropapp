package com.example.waterdropapp.ui.marketplace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Vivero
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class AdapterViveros(
    private val onViveroClick: (Vivero) -> Unit
) : RecyclerView.Adapter<AdapterViveros.ViveroVH>() {

    private val items = mutableListOf<Vivero>()

    fun submitList(lista: List<Vivero>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViveroVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vivero, parent, false)
        return ViveroVH(view)
    }

    override fun onBindViewHolder(holder: ViveroVH, position: Int) {
        holder.bind(items[position])
    }

    inner class ViveroVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgVivero = itemView.findViewById<ImageView>(R.id.imgVivero)
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreVivero)
        private val tvRating = itemView.findViewById<TextView>(R.id.tvRating)
        private val tvDistancia = itemView.findViewById<TextView>(R.id.tvDistanciaVivero)
        private val layoutEspecialidades = itemView.findViewById<LinearLayout>(R.id.layoutEspecialidades)
        private val tvEstado = itemView.findViewById<TextView>(R.id.tvEstadoVivero)
        private val btnVer = itemView.findViewById<MaterialButton>(R.id.btnVerVivero)

        fun bind(vivero: Vivero) {
            tvNombre.text = vivero.nombre

            if (vivero.rating > 0.0) {
                tvRating.text = "\u2B50 ${vivero.rating}"
                tvRating.visibility = View.VISIBLE
            } else {
                tvRating.visibility = View.GONE
            }

            tvDistancia.text = vivero.barrio

            layoutEspecialidades.removeAllViews()
            for (esp in vivero.especialidades) {
                val chip = Chip(itemView.context)
                chip.text = esp
                chip.isClickable = false
                chip.isCheckable = false
                chip.chipMinHeight = 26f
                chip.textSize = 11f
                chip.setChipBackgroundColorResource(android.R.color.transparent)
                chip.chipStrokeWidth = 0f
                val pad = 8
                chip.setPadding(pad, 0, pad, 0)
                layoutEspecialidades.addView(chip)
            }

            tvEstado.text = vivero.horario.ifEmpty { "Disponible" }

            if (vivero.imagenUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(vivero.imagenUrl)
                    .placeholder(R.drawable.ic_planta)
                    .centerCrop()
                    .into(imgVivero)
            } else {
                imgVivero.setImageResource(R.drawable.ic_planta)
            }

            btnVer.setOnClickListener {
                onViveroClick(vivero)
            }

            itemView.setOnClickListener {
                onViveroClick(vivero)
            }
        }
    }
}

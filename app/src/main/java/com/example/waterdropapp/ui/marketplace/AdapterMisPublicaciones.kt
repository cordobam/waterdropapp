package com.example.waterdropapp.ui.marketplace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Publicacion

class AdapterMisPublicaciones(
    private val onEditarClick: (Publicacion) -> Unit,
    private val onEliminarClick: (Publicacion) -> Unit
) : RecyclerView.Adapter<AdapterMisPublicaciones.MisPublicacionVH>() {

    private val items = mutableListOf<Publicacion>()

    fun submitList(lista: List<Publicacion>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MisPublicacionVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_publicacion, parent, false)
        return MisPublicacionVH(view)
    }

    override fun onBindViewHolder(holder: MisPublicacionVH, position: Int) {
        holder.bind(items[position])
    }

    inner class MisPublicacionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPublicacion = itemView.findViewById<ImageView>(R.id.imgMiPublicacion)
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreMiPublicacion)
        private val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecioMiPublicacion)
        private val tvFecha = itemView.findViewById<TextView>(R.id.tvFechaPublicacion)
        private val btnEditar = itemView.findViewById<ImageView>(R.id.btnEditarPublicacion)
        private val btnEliminar = itemView.findViewById<ImageView>(R.id.btnEliminarPublicacion)

        fun bind(publicacion: Publicacion) {
            tvNombre.text = publicacion.titulo

            tvPrecio.text = if (publicacion.precio == 0.0) "Gratis"
            else "$${publicacion.precio.toInt()}"

            if (publicacion.fechaPublicacion > 0L) {
                val ahora = System.currentTimeMillis()
                val diff = ahora - publicacion.fechaPublicacion
                val texto = when {
                    diff < 60_000 -> "Publicado hace segundos"
                    diff < 3_600_000 -> "Publicado hace ${diff / 60_000} min"
                    diff < 86_400_000 -> "Publicado hace ${diff / 3_600_000} h"
                    diff < 604_800_000 -> "Publicado hace ${diff / 86_400_000} d\u00edas"
                    else -> "Publicado hace ${diff / 604_800_000} sem"
                }
                tvFecha.text = texto
            } else {
                tvFecha.text = ""
            }

            if (publicacion.imagenUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(publicacion.imagenUrl)
                    .placeholder(R.drawable.ic_planta)
                    .centerCrop()
                    .into(imgPublicacion)
            } else {
                imgPublicacion.setImageResource(R.drawable.ic_planta)
            }

            btnEditar.setOnClickListener {
                onEditarClick(publicacion)
            }

            btnEliminar.setOnClickListener {
                onEliminarClick(publicacion)
            }
        }
    }
}
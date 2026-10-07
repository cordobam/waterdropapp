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

            tvPrecio.text = if (publicacion.precio == 0.0) itemView.context.getString(R.string.common_free)
            else "$${publicacion.precio.toInt()}"

            if (publicacion.fechaPublicacion > 0L) {
                val ahora = System.currentTimeMillis()
                val diff = ahora - publicacion.fechaPublicacion
                val ctx = itemView.context
                val texto = when {
                    diff < 60_000 -> ctx.getString(R.string.pub_age_now)
                    diff < 3_600_000 -> ctx.getString(R.string.pub_age_min, diff / 60_000)
                    diff < 86_400_000 -> ctx.getString(R.string.pub_age_hours, diff / 3_600_000)
                    diff < 604_800_000 -> ctx.getString(R.string.pub_age_days, diff / 86_400_000)
                    else -> ctx.getString(R.string.pub_age_weeks, diff / 604_800_000)
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
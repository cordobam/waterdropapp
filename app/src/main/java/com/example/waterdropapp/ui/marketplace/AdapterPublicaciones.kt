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
import com.example.waterdropapp.data.firebase.model.Vivero
import com.google.android.material.button.MaterialButton

class AdapterPublicaciones(
    private val onPublicacionClick: (Publicacion) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TIPO_PUBLICACION = 1
        const val TIPO_BANNER_VIVERO = 2
        const val CADA_CUANTOS_MOSTRAR_BANNER = 4
    }

    private val items = mutableListOf<Publicacion>()
    private val viveros = mutableListOf<Vivero>()

    fun submitList(lista: List<Publicacion>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    fun submitViveros(lista: List<Vivero>) {
        viveros.clear()
        viveros.addAll(lista)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        if (items.isEmpty()) return 0
        val banners = items.size / CADA_CUANTOS_MOSTRAR_BANNER
        return items.size + banners
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % (CADA_CUANTOS_MOSTRAR_BANNER + 1) == 0) {
            TIPO_BANNER_VIVERO
        } else {
            TIPO_PUBLICACION
        }
    }

    private fun getPublicacionIndex(position: Int): Int {
        val bannersAntes = position / (CADA_CUANTOS_MOSTRAR_BANNER + 1)
        return position - bannersAntes
    }

    private fun getViveroParaBanner(position: Int): Vivero? {
        if (viveros.isEmpty()) return null
        val bannerIndex = position / (CADA_CUANTOS_MOSTRAR_BANNER + 1)
        return viveros[bannerIndex % viveros.size]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TIPO_BANNER_VIVERO -> {
                val view = inflater.inflate(R.layout.item_banner_vivero, parent, false)
                BannerViveroVH(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_publicacion, parent, false)
                PublicacionVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PublicacionVH -> {
                val index = getPublicacionIndex(position)
                if (index < items.size) holder.bind(items[index])
            }
            is BannerViveroVH -> {
                val vivero = getViveroParaBanner(position)
                vivero?.let { holder.bind(it) }
            }
        }
    }

    inner class PublicacionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPublicacion = itemView.findViewById<ImageView>(R.id.imgPublicacion)
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombrePublicacion)
        private val tvUbicacion = itemView.findViewById<TextView>(R.id.tvUbicacion)
        private val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecio)
        private val tvCategoria = itemView.findViewById<TextView>(R.id.tvCategoria)
        private val tvTrueque = itemView.findViewById<TextView>(R.id.tvTrueque)

        fun bind(publicacion: Publicacion) {
            tvNombre.text = publicacion.titulo
            tvUbicacion.text = publicacion.barrio
            tvCategoria.text = publicacion.categoria.replaceFirstChar { it.uppercase() }

            tvPrecio.text = if (publicacion.precio == 0.0) "Gratis"
            else "$${publicacion.precio.toInt()}"

            tvTrueque.visibility = if (publicacion.aceptaTrueque) View.VISIBLE
            else View.GONE

            if (publicacion.imagenUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(publicacion.imagenUrl)
                    .placeholder(R.drawable.ic_planta)
                    .centerCrop()
                    .into(imgPublicacion)
            } else {
                imgPublicacion.setImageResource(R.drawable.ic_planta)
            }

            itemView.setOnClickListener {
                onPublicacionClick(publicacion)
            }
        }
    }

    inner class BannerViveroVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreVivero = itemView.findViewById<TextView>(R.id.tvNombreBannerVivero)
        private val tvDistancia = itemView.findViewById<TextView>(R.id.tvDistanciaBanner)
        private val tvEspecialidad = itemView.findViewById<TextView>(R.id.tvEspecialidadBanner)
        private val btnVerVivero = itemView.findViewById<MaterialButton>(R.id.btnVerBannerVivero)
        private val imgVivero = itemView.findViewById<ImageView>(R.id.imgBannerVivero)

        fun bind(vivero: Vivero) {
            tvNombreVivero.text = vivero.nombre
            tvDistancia.text = vivero.barrio
            tvEspecialidad.text = vivero.especialidades.joinToString(" · ")

            if (vivero.imagenUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(vivero.imagenUrl)
                    .placeholder(R.drawable.ic_planta)
                    .centerCrop()
                    .into(imgVivero)
            } else {
                imgVivero.setImageResource(R.drawable.ic_planta)
            }

            btnVerVivero.setOnClickListener {
                // navegás al detalle del vivero
            }
        }
    }
}
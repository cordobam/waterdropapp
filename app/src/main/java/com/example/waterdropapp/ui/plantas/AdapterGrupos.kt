package com.example.waterdropapp.ui.plantas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.EstadoGruposDTO

class AdapterGrupos(
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<AdapterGrupos.GrupoVH>() {

    private val items = mutableListOf<EstadoGruposDTO>()

    fun submitList(lista: List<EstadoGruposDTO>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grupo, parent, false)
        return GrupoVH(view)
    }

    override fun onBindViewHolder(holder: GrupoVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class GrupoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreGrupo)

        fun bind(grupo: EstadoGruposDTO) {
            tvNombre.text = grupo.nombreGrupo
            itemView.setOnClickListener {
                onClick(grupo.grupoId)
            }
        }
    }
}

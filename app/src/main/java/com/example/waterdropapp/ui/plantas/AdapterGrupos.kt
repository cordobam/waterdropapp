package com.example.waterdropapp.ui.plantas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.EstadoGruposDTO

class AdapterGrupos(
    private val onVerGrupo: (Int) -> Unit,
    private val onRegarGrupo: (Int) -> Unit
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
        private val tvCantPlantas = itemView.findViewById<TextView>(R.id.tvCantPlantas)

        private val btnRegar = itemView.findViewById<Button>(R.id.btnRegarGrupo)
        private val btnVer = itemView.findViewById<Button>(R.id.btnVerGrupo)
        fun bind(grupo: EstadoGruposDTO) {
            tvNombre.text = grupo.nombreGrupo
            tvCantPlantas.text = "Cantidad de plantas ${grupo.cantPlantasGrupo}"
            btnRegar.setOnClickListener {
                onRegarGrupo(grupo.grupoId)
            }
            btnVer.setOnClickListener {
                onVerGrupo(grupo.grupoId)
            }
        }
    }
}

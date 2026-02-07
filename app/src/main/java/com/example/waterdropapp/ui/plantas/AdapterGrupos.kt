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
    private val modo: Modo,
    private val onVerGrupo: ((Int) -> Unit)? = null,
    private val onRegarGrupo: ((Int) -> Unit)? = null,
    private val onEditarGrupo: ((Int) -> Unit)? = null,
    private val onEliminarGrupo: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class Modo {
        MOSTRAR_GRUPOS,
        ACTUALIZAR_GRUPOS
    }
    private val items = mutableListOf<EstadoGruposDTO>()

    fun submitList(lista: List<EstadoGruposDTO>) {
        items.clear()
        items.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType) {

            1 -> {
                val view = inflater.inflate(R.layout.item_grupo, parent, false)
                GrupoVH(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_grupo_act, parent, false)
                GrupoActualizarVH(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(modo) {
            Modo.MOSTRAR_GRUPOS-> 1
            Modo.ACTUALIZAR_GRUPOS -> 2
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val grupo = items[position]

        when(holder) {
            is GrupoVH -> holder.bind(grupo)
            is GrupoActualizarVH -> holder.bind(grupo)
        }
    }

    override fun getItemCount() = items.size

    inner class GrupoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreGrupo)
        private val tvCantPlantas = itemView.findViewById<TextView>(R.id.tvCantPlantas)
        private val btnRegar = itemView.findViewById<Button?>(R.id.btnRegarGrupo)
        private val btnVer = itemView.findViewById<Button?>(R.id.btnVerGrupo)


        fun bind(grupo: EstadoGruposDTO) {
            tvNombre.text = grupo.nombreGrupo
            tvCantPlantas.text = "Cantidad de plantas ${grupo.cantPlantasGrupo}"
            btnVer.setOnClickListener {
                onVerGrupo?.invoke(grupo.grupoId)
            }
            btnRegar.setOnClickListener {
                onRegarGrupo?.invoke(grupo.grupoId)
            }

        }


    }

    inner class GrupoActualizarVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreGrupoAct = itemView.findViewById<TextView>(R.id.tvNombreGrupoAct)
        private val btnEditar = itemView.findViewById<Button?>(R.id.btnActualizarGrupo)

        private val btnEliminar = itemView.findViewById<Button?>(R.id.btnEliminarGrupo)

        fun bind(grupo: EstadoGruposDTO) {

            nombreGrupoAct.text = grupo.nombreGrupo
            btnEditar.setOnClickListener {
                onEditarGrupo?.invoke(grupo.grupoId)
            }
            btnEliminar.setOnClickListener {
                onEliminarGrupo?.invoke(grupo.grupoId)
            }

        }
    }
}

package com.example.waterdropapp.ui.grupos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.dto.EstadoGruposDTO
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GruposBottomSheet(
    private val listaGrupos: List<EstadoGruposDTO>, // Tipo correcto
    private val onEditar: (Int) -> Unit,
    private val onEliminar: (Int, View) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_grupos, container, false)

        val rv = view.findViewById<RecyclerView>(R.id.rvListaGestionGrupo)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscarGrupo)

        // Usamos tu adaptador existente
        val adapter = AdapterGrupos(
            modo = AdapterGrupos.Modo.ACTUALIZAR_GRUPOS,
            onEditarGrupo = { id ->
                onEditar(id)
                dismiss() // Cerramos al editar para ver el diálogo
            },
            onEliminarGrupo = { id -> onEliminar(id, view )  }
        )

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter
        adapter.submitList(listaGrupos)

        // Lógica de búsqueda
        etBuscar.addTextChangedListener { text ->
            val filtrados = listaGrupos.filter { it.nombreGrupo.lowercase().contains(text.toString().lowercase()) }
            adapter.submitList(filtrados)
        }

        return view
    }
}
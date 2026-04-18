package com.example.waterdropapp.ui.plantas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.R
import com.example.waterdropapp.data.dto.EstadoPlantasDTO
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlantasBottomSheet(
    private val listaPlantas: List<EstadoPlantasDTO>, // Tipo correcto
    private val onEditar: (Int) -> Unit,
    private val onEliminar: (Int) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_plantas, container, false)

        val rv = view.findViewById<RecyclerView>(R.id.rvListaGestion)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscarPlanta)

        // Usamos tu adaptador existente
        val adapter = AdapterPlantas(
            modo = AdapterPlantas.Modo.ACTUALIZAR_PLANTAS,
            onEditarPlanta = { id ->
                onEditar(id)
                dismiss() // Cerramos al editar para ver el diálogo
            },
            onEliminarPlanta = { id -> onEliminar(id) }
        )

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter
        adapter.submitList(listaPlantas)

        // Lógica de búsqueda
        etBuscar.addTextChangedListener { text ->
            val filtrados = listaPlantas.filter { it.nombre.lowercase().contains(text.toString().lowercase()) }
            adapter.submitList(filtrados)
        }

        return view
    }
}
package com.example.waterdropapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.firebase.model.Vivero
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.example.waterdropapp.ui.marketplace.AdapterViveros

class ViverosFragment : Fragment(R.layout.fragment_viveros) {

    private val repository = FirestoreRepository()
    private lateinit var adapter: AdapterViveros
    private var listaCompleta = listOf<Vivero>()
    private var queryBusqueda = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etBuscar = view.findViewById<EditText>(R.id.etBuscarVivero)
        val tvCantidad = view.findViewById<TextView>(R.id.tvCantidadViveros)
        val rv = view.findViewById<RecyclerView>(R.id.rvViveros)

        adapter = AdapterViveros { vivero ->
            Toast.makeText(requireContext(), "Detalle de ${vivero.nombre} - pr\u00f3ximamente", Toast.LENGTH_SHORT).show()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                queryBusqueda = s?.toString()?.trim()?.lowercase() ?: ""
                aplicarFiltros(tvCantidad)
            }
        })

        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                queryBusqueda = etBuscar.text.toString().trim().lowercase()
                aplicarFiltros(tvCantidad)
                true
            } else false
        }

        cargarDatos(tvCantidad)
    }

    private fun cargarDatos(tvCantidad: TextView) {
        repository.getViveros(
            onSuccess = { lista ->
                listaCompleta = lista
                aplicarFiltros(tvCantidad)
            },
            onError = {
                Toast.makeText(requireContext(), "Error al cargar viveros", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun aplicarFiltros(tvCantidad: TextView) {
        var lista = listaCompleta

        if (queryBusqueda.isNotEmpty()) {
            lista = lista.filter {
                it.nombre.lowercase().contains(queryBusqueda) ||
                        it.barrio.lowercase().contains(queryBusqueda) ||
                        it.especialidades.any { esp -> esp.lowercase().contains(queryBusqueda) }
            }
        }

        adapter.submitList(lista)
        tvCantidad.text = "${lista.size} viveros encontrados"
    }
}

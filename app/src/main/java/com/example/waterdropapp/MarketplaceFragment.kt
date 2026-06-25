package com.example.waterdropapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.example.waterdropapp.ui.marketplace.AdapterPublicaciones
import com.example.waterdropapp.ui.marketplace.PublicacionFormlDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class MarketplaceFragment : Fragment(R.layout.fragment_marketplace) {

    private val repository = FirestoreRepository()
    private lateinit var adapter: AdapterPublicaciones
    private var listaCompleta = listOf<Publicacion>()
    private var filtroActual = "todos"
    private var queryBusqueda = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipTodos = view.findViewById<Chip>(R.id.chipTodos)
        val chipPlantas = view.findViewById<Chip>(R.id.chipPlantas)
        val chipSemillas = view.findViewById<Chip>(R.id.chipSemillas)
        val chipEsquejes = view.findViewById<Chip>(R.id.chipEsquejes)
        val chipTrueque = view.findViewById<Chip>(R.id.chipTrueque)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        val tvCercaTuyo = view.findViewById<TextView>(R.id.tvCercaTuyo)

        val chips = listOf(chipTodos, chipPlantas, chipSemillas, chipEsquejes, chipTrueque)

        adapter = AdapterPublicaciones { publicacion ->
            // click en publicacion
        }

        view.findViewById<RecyclerView>(R.id.rvPublicaciones).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MarketplaceFragment.adapter
        }

        for (chip in chips) {
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    for (other in chips) {
                        if (other != chip) other.isChecked = false
                    }
                    filtroActual = when (chip.id) {
                        R.id.chipPlantas -> "planta"
                        R.id.chipSemillas -> "semilla"
                        R.id.chipEsquejes -> "esqueje"
                        R.id.chipTrueque -> "trueque"
                        else -> "todos"
                    }
                    aplicarFiltros(tvCercaTuyo)
                }
            }
        }
        chipTodos.isChecked = true

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                queryBusqueda = s?.toString()?.trim()?.lowercase() ?: ""
                aplicarFiltros(tvCercaTuyo)
            }
        })

        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                queryBusqueda = etBuscar.text.toString().trim().lowercase()
                aplicarFiltros(tvCercaTuyo)
                true
            } else false
        }


        view.findViewById<MaterialButton>(R.id.btnPublicar).setOnClickListener {
            PublicacionFormlDialog(
                onSuccess = {cargarDatos(tvCercaTuyo)}
            ).show(parentFragmentManager,"PublicarPublicacion")
        }

        cargarDatos(tvCercaTuyo)
    }

    private fun cargarDatos(tvCercaTuyo: TextView) {
        repository.getPublicaciones(
            onSuccess = { lista ->
                listaCompleta = lista
                Log.d("FIRESTORE_TEST", "${lista.size} publicaciones")
                aplicarFiltros(tvCercaTuyo)
            },
            onError = {
                Toast.makeText(requireContext(), "Error al cargar", Toast.LENGTH_SHORT).show()
            }
        )

        repository.getViveros(
            onSuccess = { lista ->
                adapter.submitViveros(lista)
            },
            onError = {
                Log.e("FIRESTORE", "Error al cargar viveros para banners")
            }
        )
    }

    private fun aplicarFiltros(tvCercaTuyo: TextView) {
        var lista = listaCompleta

        when (filtroActual) {
            "planta" -> lista = lista.filter { it.categoria == "planta" }
            "semilla" -> lista = lista.filter { it.categoria == "semilla" }
            "esqueje" -> lista = lista.filter { it.categoria == "esqueje" }
            "trueque" -> lista = lista.filter { it.aceptaTrueque }
        }

        if (queryBusqueda.isNotEmpty()) {
            lista = lista.filter { it.titulo.lowercase().contains(queryBusqueda) }
        }

        adapter.submitList(lista)
        tvCercaTuyo.text = "Cerca tuyo · ${lista.size} publicaciones"
    }
}
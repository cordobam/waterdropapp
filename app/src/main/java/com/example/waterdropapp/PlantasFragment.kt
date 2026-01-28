package com.example.waterdropapp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.ui.plantas.AdapterPlantas
import java.util.Date
import java.util.Locale


class PlantasFragment : Fragment(R.layout.fragment_plantas) {

    private lateinit var db: DBHelper
    private lateinit var adapter: AdapterPlantas

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBHelper(requireContext())

        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        adapter = AdapterPlantas { plantaId ->
            db.putRiegos(plantaId, fecha)   // la fecha se maneja en DBHelper

        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPlantas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cargarPlantas()

    }

    fun cargarPlantas() {
        val lista = db.obtenerEstadoPlantas()
        adapter.submitList(lista)
    }
}
package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.ui.historial.AdapterHistorial
import com.example.waterdropapp.data.repository.PlantaRepository
import com.example.waterdropapp.data.repository.RiegoRepository


class HistorialRiegoFragment : Fragment(R.layout.fragment_historial_riego) {


    private lateinit var historialAdapter: AdapterHistorial
    private lateinit var plantaRepo: PlantaRepository
    private lateinit var riegoRepo: RiegoRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helper = DBHelper(requireContext())
        plantaRepo = PlantaRepository(helper)
        riegoRepo = RiegoRepository(helper)
        historialAdapter = AdapterHistorial()

        // carga spinner
        val spinnerPlantas = view.findViewById<Spinner>(R.id.spinnerHistorialRiego)
        val plantas = plantaRepo.getPlantas()
        val nombresPlantas = plantas.map{it.second}
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresPlantas
        )
        spinnerAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerPlantas.adapter = spinnerAdapter

        // carga recyclerview
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvHistorialRiego)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = historialAdapter


        // seleccion spinner
        spinnerPlantas.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                   position: Int,
                    id: Long
                ) {
                    val plantaSeleccionada = plantas[position]
                    val lista = riegoRepo.obtenerHistorialRiegoxPlanta(plantaSeleccionada.first)
                    val listaOrdenada = lista.sortedByDescending { it.fechaRiego }
                    historialAdapter.submitList(listaOrdenada)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

    }

}
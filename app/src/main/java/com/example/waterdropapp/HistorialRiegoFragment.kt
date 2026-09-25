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
import com.example.waterdropapp.data.repository.ActividadRepository
import com.example.waterdropapp.data.repository.PlantaRepository
import com.example.waterdropapp.domain.model.TipoActividad
import com.google.android.material.chip.ChipGroup


class HistorialRiegoFragment : Fragment(R.layout.fragment_historial_riego) {


    private lateinit var historialAdapter: AdapterHistorial
    private lateinit var plantaRepo: PlantaRepository
    private lateinit var actividadRepo: ActividadRepository
    private var filtroActividad: TipoActividad? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helper = DBHelper(requireContext())
        plantaRepo = PlantaRepository(helper, requireContext())
        actividadRepo = ActividadRepository(helper)
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

        // filtro por tipo de actividad
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFiltroHistorial)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val chipId = checkedIds.firstOrNull()

            filtroActividad = when (chipId) {
                R.id.chipHistorialRiego -> TipoActividad.RIEGO
                R.id.chipHistorialAbonado -> TipoActividad.ABONADO
                R.id.chipHistorialPodado -> TipoActividad.PODADO
                else -> null
            }

            if (spinnerPlantas.selectedItemPosition in plantas.indices) {
                cargarHistorial(plantas[spinnerPlantas.selectedItemPosition].first)
            }
        }

        // seleccion spinner
        spinnerPlantas.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                   position: Int,
                    id: Long
                ) {
                    cargarHistorial(plantas[position].first)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

    }

    private fun cargarHistorial(plantaId: Int) {
        val lista = actividadRepo.obtenerActividadesxPlanta(plantaId, filtroActividad)
        historialAdapter.submitList(lista)
    }

}
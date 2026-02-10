package com.example.waterdropapp

import android.graphics.Rect
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.ui.plantas.AdapterGrupos
import com.example.waterdropapp.ui.plantas.AdapterPlantas
import com.google.android.material.tabs.TabLayout
import java.util.Date
import java.util.Locale


class PlantasFragment : Fragment(R.layout.fragment_plantas) {

    private lateinit var db: DBHelper
    private lateinit var plantasAdapter: AdapterPlantas
    private lateinit var gruposAdapter: AdapterGrupos


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())

        val fecha: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        plantasAdapter = AdapterPlantas (
            modo = AdapterPlantas.Modo.MOSTRAR_PLANTAS,
            onRegarClick = { plantaId ->
                db.putRiegos(plantaId, fecha)

                Toast.makeText(
                    requireContext(),
                    "Planta regada con exito",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )


        // me sirve para tener dos botones y que cada uno haga algo
        gruposAdapter = AdapterGrupos(
            modo = AdapterGrupos.Modo.MOSTRAR_GRUPOS,
            onVerGrupo = {grupoId ->

                cargarPlantasPorGrupo(grupoId)
            },
            onRegarGrupo = {grupoId ->
                db.putRiegoPorGrupo(grupoId, fecha)
                Toast.makeText(requireContext(), "Grupo regado", Toast.LENGTH_SHORT).show()
            }
        )

        // recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPlantas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = plantasAdapter

        // para el espaciado
        recyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                                            parent: RecyclerView, state: RecyclerView.State) {
                    outRect.bottom = 16
                }
            }
        )

        // carga principal
        cargarPlantas()

        // tabs
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.getTabAt(0)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        recyclerView.adapter = plantasAdapter
                        cargarPlantas()
                    }
                    1 -> {
                        recyclerView.adapter = gruposAdapter
                        cargarGrupos()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


    }

    fun cargarPlantas() {
        val lista = db.obtenerEstadoPlantas()
        val listaOrdenada = lista.sortedByDescending { it.diasSinRegar }
        plantasAdapter.submitList(listaOrdenada)
    }

    private fun cargarGrupos() {
        val grupos = db.getEstadosGrupos()
        gruposAdapter.submitList(grupos)
    }

    // me sirve para seleccionar el otro tab y mostrar las plantas ahi
    private fun cargarPlantasPorGrupo(grupoId: Int) {
        //val plantas = db.obtenerEstadoPlantasPorGrupo(grupoId)
        //plantasAdapter.submitList(plantas)

        val tabLayout = view?.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout?.getTabAt(0)?.select()   // tab Plantas

        val plantas = db.obtenerEstadoPlantasPorGrupo(grupoId)
        plantasAdapter.submitList(plantas)
    }
}
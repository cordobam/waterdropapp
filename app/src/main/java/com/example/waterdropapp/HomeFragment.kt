package com.example.waterdropapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.data.repository.IndicadoresRepository


class HomeFragment : Fragment(R.layout.fragment_home) {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbHelper = DBHelper(requireContext())
        val repository = IndicadoresRepository(dbHelper)

        val indicadores = repository.getIndicadores()

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalPlantas)
        val tvTotalxRegar = view.findViewById<TextView>(R.id.tvPorRegar)
        val tvTotalxNoRegar = view.findViewById<TextView>(R.id.tvRegadasHoy)

        val tvPromedioDias = view.findViewById<TextView>(R.id.tvPromedioDias)
        val tvPromedioTardanza = view.findViewById<TextView>(R.id.tvPromedioTardanza)

        tvTotal.text = indicadores.total.toString()
        tvTotalxRegar.text = indicadores.necesitanRiego.toString()
        tvTotalxNoRegar.text = indicadores.noNecesitanRiego.toString()
        tvPromedioDias.text = String.format("%.2f", indicadores.promedioDiasRiego)
        tvPromedioTardanza.text = String.format("%.2f", indicadores.promedioTardanza)

    }


}
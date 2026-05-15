package com.example.waterdropapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.data.repository.IndicadoresRepository
import com.google.android.material.button.MaterialButton


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
        val btnMarketplace = view.findViewById<MaterialButton>(R.id.btnMarketplace)

        tvTotal.text = indicadores.total.toString()
        tvTotalxRegar.text = indicadores.necesitanRiego.toString()
        tvTotalxNoRegar.text = indicadores.noNecesitanRiego.toString()
        tvPromedioDias.text = String.format("%.2f", indicadores.promedioDiasRiego)
        tvPromedioTardanza.text = String.format("%.2f", indicadores.promedioTardanza)

        btnMarketplace.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)

            val intent = if (isLoggedIn) {
                Intent(requireContext(), MarketplaceActivity::class.java)
            } else {
                Intent(requireContext(), LoginActivity::class.java)
            }
            startActivity(intent)
        }
    }


}
package com.example.waterdropapp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.ui.plantas.AdapterHistorial
import com.example.waterdropapp.ui.plantas.AdapterPlantas
import java.util.Date
import java.util.Locale


class HistorialRiegoFragment : Fragment(R.layout.fragment_historial_riego) {

    private lateinit var db: DBHelper
    private lateinit var adapter: AdapterHistorial

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())
        adapter = AdapterHistorial()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvHistorialRiego)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cargarHistorial()

    }

    fun cargarHistorial() {
        val lista = db.obtenerHistorialRiego()
        adapter.submitList(lista)
    }
}
package com.example.waterdropapp

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.example.waterdropapp.data.DBHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class CargarRiegoFragment : Fragment(R.layout.fragment_cargar_riego) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //spinner
        val db = DBHelper(requireContext())

        super.onViewCreated(view, savedInstanceState)

        val spinnerPlantas = view.findViewById<Spinner>(R.id.spinnerPlantas)
        val plantas = db.getPlantas()
        val nombresPlantas = plantas.map { it.second }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresPlantas
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlantas.adapter = adapter


        //btn_guardar
        view.findViewById<Button>(R.id.btnRegar).setOnClickListener {
            val posicion = spinnerPlantas.selectedItemPosition

            if (posicion == AdapterView.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Seleccione una planta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val plantaSeleccionada = plantas[posicion]

            val plantaId = plantaSeleccionada.first
            val codigoPlanta = plantaSeleccionada.second

            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val values = db.putRiegos(plantaId,fecha=fecha)

            Toast.makeText(
                requireContext(),
                "Planta $codigoPlanta regada con exito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.waterdropapp.data.DBHelper

class CargarPlantasFragment : Fragment(R.layout.fragment_cargar_plantas) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //carga spinner con datos
        val db = DBHelper(requireContext())
        val spinnerGrupos = view.findViewById<Spinner>(R.id.spinnerGrupos)
        val grupos = db.getGrupos()
        val nombresGrupos = grupos.map{it.nombre}
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresGrupos
        )
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerGrupos.adapter = adapter

        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            // seleccion de spinner
            val posicion = spinnerGrupos.selectedItemPosition
            if (posicion == AdapterView.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Seleccione un grupo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val grupoSeleccionado = grupos[posicion]
            val codigoGrupo = grupoSeleccionado.grupo_id

            // plantas
            val nombre = view.findViewById<EditText>(R.id.etNombre).text.toString()
            val diasString = view.findViewById<EditText>(R.id.etDiasMax).text.toString()
            val dias = diasString.toIntOrNull() ?: 0
            //insert plantas
            val values = db.putPlantas(nombre, dias)

            // insert gruposplantas
            val valueInt: Int = values.toInt()
            val grupo_plantas = db.putGruposPlantas(valueInt,codigoGrupo)

            Toast.makeText(
                requireContext(),
                "Planta $nombre cargada con exito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
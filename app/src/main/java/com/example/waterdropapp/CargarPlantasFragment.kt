package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.waterdropapp.data.DBHelper

class CargarPlantasFragment : Fragment(R.layout.fragment_cargar_plantas) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val db = DBHelper(requireContext())

        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val nombre = view.findViewById<EditText>(R.id.etNombre).text.toString()
            val diasString = view.findViewById<EditText>(R.id.etDiasMax).text.toString()
            val dias = diasString.toIntOrNull() ?: 0

            val values = db.putPlantas(nombre, dias)
            Toast.makeText(
                requireContext(),
                "Planta $nombre cargada con exito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
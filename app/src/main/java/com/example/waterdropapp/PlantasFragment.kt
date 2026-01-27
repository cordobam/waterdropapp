package com.example.waterdropapp

import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.waterdropapp.data.DBHelper

class PlantasFragment : Fragment(R.layout.fragment_plantas) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val db = DBHelper(requireContext())

        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val nombre = view.findViewById<EditText>(R.id.etNombre).text.toString()

            val values = db.putPlantas(nombre)
            Toast.makeText(
                requireContext(),
                "Planta $nombre cargada con exito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
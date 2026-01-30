package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.waterdropapp.data.DBHelper


class CargarGrupoFragment : Fragment(R.layout.fragment_cargar_grupo) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = DBHelper(requireContext())
        view.findViewById<Button>(R.id.btnGuardarGrupo).setOnClickListener{
            val nombre = view.findViewById<EditText>(R.id.etNombreGrupo).text.toString()
            val values = db.putGrupos(nombre)
            Toast.makeText(
                requireContext(),
                "Grupo $nombre cargado con exito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
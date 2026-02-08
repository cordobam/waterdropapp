package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.ui.plantas.AdapterGrupos


class CargarGrupoFragment : Fragment(R.layout.fragment_cargar_grupo) {
    private lateinit var db: DBHelper
    private lateinit var gruposAdapterAct: AdapterGrupos

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = DBHelper(requireContext())

        view.findViewById<Button>(R.id.btnGuardarGrupo).setOnClickListener{
            val nombre = view.findViewById<EditText>(R.id.etNombreGrupo).text.toString()
            val values = db.putGrupos(nombre)
            Toast.makeText(
                requireContext(),
                "Grupo $nombre cargado con exito",
                Toast.LENGTH_SHORT
            ).show()
        }

        gruposAdapterAct = AdapterGrupos(
            modo = AdapterGrupos.Modo.ACTUALIZAR_GRUPOS,
            onEditarGrupo = { id -> editarGrupo(id) },
            onEliminarGrupo = { id -> eliminarGrupo(id) }
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvEliminarActualizarGrupo)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = gruposAdapterAct

        view.findViewById<Button>(R.id.btnVerGrupo).setOnClickListener {
            val db = DBHelper(requireContext())
            val grupos = db.getEstadosGrupos()
            gruposAdapterAct.submitList(grupos)
        }
    }

    fun editarGrupo(id:Int) {

    }

    fun eliminarGrupo(id:Int) {
        val filas = db.eliminarGrupos(id)
        if (filas > 0) {
            Toast.makeText(requireContext(), "Grupo eliminado", Toast.LENGTH_SHORT).show()

            // refrescar lista
            gruposAdapterAct.submitList(db.getEstadosGrupos())
        } else {
            Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
        }
    }
}
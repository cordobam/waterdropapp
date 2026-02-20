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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.ui.grupos.AdapterGrupos
import com.example.waterdropapp.ui.plantas.AdapterPlantas

class CargarPlantasFragment : Fragment(R.layout.fragment_cargar_plantas) {

    private lateinit var db: DBHelper
    private lateinit var plantasAdapterAct: AdapterPlantas

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //carga spinner con datos
        db = DBHelper(requireContext())
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

        plantasAdapterAct = AdapterPlantas(
            modo = AdapterPlantas.Modo.ACTUALIZAR_PLANTAS,
            onEditarPlanta = { id -> editarPlantas(id) },
            onEliminarPlanta = { id -> eliminarPlantas(id) }
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvEliminarActualizarPlantas)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = plantasAdapterAct

        view.findViewById<Button>(R.id.btnVerPlantas).setOnClickListener {
            val db = DBHelper(requireContext())
            val plantas = db.obtenerEstadoPlantas()
            plantasAdapterAct.submitList(plantas)
        }
    }

    fun editarPlantas(id:Int) {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_editar_planta,
            null
        )

        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etDias = dialogView.findViewById<EditText>(R.id.etDias)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Planta")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->

                val nombre = etNombre.text.toString()
                val diasInt = etDias.text.toString().toIntOrNull() ?: 0

                db.actualizarPlantas(id, nombre, diasInt)

                plantasAdapterAct.submitList(
                    db.obtenerEstadoPlantas()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun eliminarPlantas(id:Int) {
        val filas = db.eliminarPlantas(id)
        if (filas > 0) {
            Toast.makeText(requireContext(), "Planta eliminada", Toast.LENGTH_SHORT).show()

            // refrescar lista
            plantasAdapterAct.submitList(db.obtenerEstadoPlantas())
        } else {
            Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
        }
    }
}
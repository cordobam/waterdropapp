package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.ui.grupos.AdapterGrupos
import com.example.waterdropapp.ui.grupos.GruposBottomSheet
import com.google.android.material.snackbar.Snackbar


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

        val botonvergrupos = view.findViewById<Button>(R.id.btnVerGrupo)

        botonvergrupos.setOnClickListener {
            val gruposDTO = db.getEstadosGrupos()

            val sheet = GruposBottomSheet(
                listaGrupos = gruposDTO,
                onEditar = { id -> editarGrupo(id) },
                onEliminar = { id, vistasheet -> eliminarGrupo(id ,vistasheet) }
            )
            sheet.show(parentFragmentManager, "GruposSheet")
        }
    }

    fun editarGrupo(id:Int) {
        val input = EditText(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar grupo")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->


                val nombre = input.text.toString()
                db.actualizarGrupos(id, nombre)
                Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                gruposAdapterAct.submitList(db.getEstadosGrupos())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarGrupo(id: Int , view: View? = null) {

        val filas = db.eliminarGrupos(id)

        if (filas > 0) {

            // Refrescamos la lista primero
            //gruposAdapterAct.submitList(db.getEstadosGrupos())
            val snackbarView = view ?: requireView()
            val listaActualizada = db.getEstadosGrupos()

            gruposAdapterAct.submitList(listaActualizada)

            Snackbar.make(snackbarView, "Grupo eliminado", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {

                    db.reactivarGrupo(id)
                    val listaReactivada = db.getEstadosGrupos()
                    gruposAdapterAct.submitList(listaReactivada)
                }
                .show()

        } else {
            Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
        }
    }
}
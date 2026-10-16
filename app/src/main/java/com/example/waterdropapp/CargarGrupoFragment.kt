package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.ui.grupos.AdapterGrupos
import com.example.waterdropapp.ui.grupos.GruposBottomSheet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.waterdropapp.data.repository.GrupoRepository

class CargarGrupoFragment : Fragment(R.layout.fragment_cargar_grupo) {
    private lateinit var db: GrupoRepository
    private lateinit var gruposAdapterAct: AdapterGrupos

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = GrupoRepository(DBHelper(requireContext()))

        view.findViewById<Button>(R.id.btnGuardarGrupo).setOnClickListener{
            val nombre = view.findViewById<EditText>(R.id.etNombreGrupo).text.toString()
            val values = db.putGrupos(nombre)
            Toast.makeText(
                requireContext(),
                getString(R.string.grupos_loaded_ok, nombre),
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_grupo, null)

        val etNombreGrupo = dialogView.findViewById<EditText>(R.id.etNombreGrupoEdit)
        etNombreGrupo.setText(db.getGrupos().firstOrNull { it.grupo_id == id }?.nombre.orEmpty())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnGuardarGrupo).setOnClickListener {
            val nombre = etNombreGrupo.text.toString()
            db.actualizarGrupos(id, nombre)
            Toast.makeText(requireContext(), getString(R.string.common_saved), Toast.LENGTH_SHORT).show()
            gruposAdapterAct.submitList(db.getEstadosGrupos())
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancelarGrupo).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun eliminarGrupo(id: Int , view: View? = null) {

        val filas = db.softDeleteGrupo(id,false)

        if (filas > 0) {

            // Refrescamos la lista primero
            //gruposAdapterAct.submitList(db.getEstadosGrupos())
            val snackbarView = view ?: requireView()
            val listaActualizada = db.getEstadosGrupos()

            gruposAdapterAct.submitList(listaActualizada)

            Snackbar.make(snackbarView, getString(R.string.common_group_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.common_undo)) {

                    db.softDeleteGrupo(id , true)
                    val listaReactivada = db.getEstadosGrupos()
                    gruposAdapterAct.submitList(listaReactivada)
                }
                .show()

        } else {
            Toast.makeText(requireContext(), getString(R.string.common_delete_failed), Toast.LENGTH_SHORT).show()
        }
    }
}
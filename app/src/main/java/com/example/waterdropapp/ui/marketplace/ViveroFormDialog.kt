package com.example.waterdropapp.ui.marketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Vivero
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ViveroForrmDialog(
    private val itemAEditar: Vivero? = null,
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vivero_form_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (itemAEditar != null) cargarDatosExistente(itemAEditar)
        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dismiss() }
    }

    private fun cargarDatosExistente(usuario: Vivero) {
        view?.findViewById<EditText>(R.id.etNombre)?.setText(usuario.nombre)
        view?.findViewById<EditText>(R.id.etDescripcion)?.setText(usuario.descripcion)
        view?.findViewById<EditText>(R.id.etDireccion)?.setText(usuario.direccion)
        view?.findViewById<EditText>(R.id.etCiudad)?.setText(usuario.ciudad)
        view?.findViewById<EditText>(R.id.etBarrio)?.setText(usuario.barrio)
        view?.findViewById<EditText>(R.id.etTelefono)?.setText(usuario.telefono)
        view?.findViewById<EditText>(R.id.etHorario)?.setText(usuario.horario)
        view?.findViewById<EditText>(R.id.etEspecialidades)?.setText(usuario.especialidades.toString())
        view?.findViewById<EditText>(R.id.etImagenUrl)?.setText(usuario.imagenUrl)
    }

    private fun guardar() {
        val repo = FirestoreRepository()
        val datos = mutableMapOf(
            "nombre" to (view?.findViewById<EditText>(R.id.etNombre)?.text?.toString()?.trim() ?: ""),
            "descripcion" to (view?.findViewById<EditText>(R.id.etDescripcion)?.text?.toString()?.trim() ?: ""),
            "direccion" to (view?.findViewById<EditText>(R.id.etDireccion)?.text?.toString()?.trim() ?: ""),
            "ciudad" to (view?.findViewById<EditText>(R.id.etBarrio)?.text?.toString()?.trim() ?: ""),
            "barrio" to (view?.findViewById<EditText>(R.id.etFotoUrl)?.text?.toString()?.trim() ?: ""),
            "telefono" to (view?.findViewById<EditText>(R.id.etTelefono)?.text?.toString()?.trim() ?: ""),
            "horario" to (view?.findViewById<EditText>(R.id.etHorario)?.text?.toString()?.trim() ?: ""),
            "especialidades" to (view?.findViewById<EditText>(R.id.etEspecialidades)?.text?.toString()?.trim() ?: ""),
            "imagenUrl" to (view?.findViewById<EditText>(R.id.etImagenUrl)?.text?.toString()?.trim() ?: ""),
        )
        if (itemAEditar != null) {
            repo.updateVivero(itemAEditar.id, datos, { dismiss(); onSuccess() }, {})
        }
    }
}
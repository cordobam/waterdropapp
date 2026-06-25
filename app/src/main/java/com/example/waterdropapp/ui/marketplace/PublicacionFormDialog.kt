package com.example.waterdropapp.ui.marketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PublicacionFormlDialog(
    private val itemAEditar: Publicacion? = null,
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_publicacion_form_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (itemAEditar != null) cargarDatosExistente(itemAEditar)
        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dismiss() }
    }

    private fun cargarDatosExistente(usuario: Publicacion) {
        view?.findViewById<EditText>(R.id.etTitulo)?.setText(usuario.titulo)
        view?.findViewById<EditText>(R.id.etDescripcion)?.setText(usuario.descripcion)
        view?.findViewById<EditText>(R.id.chipGroupCategoria)?.setText(usuario.categoria)
        view?.findViewById<SwitchCompat>(R.id.swAceptaTrueque)?.isChecked = usuario.aceptaTrueque
        view?.findViewById<EditText>(R.id.etPrecio)?.setText(usuario.precio.toString())
        view?.findViewById<EditText>(R.id.etImagenUrl)?.setText(usuario.imagenUrl)
        view?.findViewById<EditText>(R.id.etCiudad)?.setText(usuario.ciudad)
        view?.findViewById<EditText>(R.id.etBarrio)?.setText(usuario.barrio)
    }

    private fun guardar() {
        val repo = FirestoreRepository()
        val datos = mutableMapOf(
            "titulo" to (view?.findViewById<EditText>(R.id.etTitulo)?.text?.toString()?.trim() ?: ""),
            "descripcion" to (view?.findViewById<EditText>(R.id.etDescripcion)?.text?.toString()?.trim() ?: ""),
            "categoria" to (view?.findViewById<EditText>(R.id.chipGroupCategoria)?.text?.toString()?.trim() ?: ""),
            "precio" to (view?.findViewById<EditText>(R.id.etPrecio)?.text?.toString()?.trim() ?: ""),
            "imagenUrl" to (view?.findViewById<EditText>(R.id.etImagenUrl)?.text?.toString()?.trim() ?: ""),
            "ciudad" to (view?.findViewById<EditText>(R.id.etCiudad)?.text?.toString()?.trim() ?: ""),
            "barrio" to (view?.findViewById<EditText>(R.id.etBarrio)?.text?.toString()?.trim() ?: "")
        )
        if (itemAEditar != null) {
            repo.updatePublicacion(itemAEditar.id, datos, { dismiss(); onSuccess() }, {})
        }
    }
}
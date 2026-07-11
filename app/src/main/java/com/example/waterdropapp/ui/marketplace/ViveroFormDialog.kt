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
import com.google.firebase.auth.FirebaseAuth

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
        val nombre = view?.findViewById<EditText>(R.id.etNombre)?.text?.toString()?.trim() ?: ""
        val descripcion = view?.findViewById<EditText>(R.id.etDescripcion)?.text?.toString()?.trim() ?: ""
        val direccion = view?.findViewById<EditText>(R.id.etDireccion)?.text?.toString()?.trim() ?: ""
        val ciudad = view?.findViewById<EditText>(R.id.etCiudad)?.text?.toString()?.trim() ?: ""
        val barrio = view?.findViewById<EditText>(R.id.etBarrio)?.text?.toString()?.trim() ?: ""
        val telefono = view?.findViewById<EditText>(R.id.etTelefono)?.text?.toString()?.trim() ?: ""
        val horario = view?.findViewById<EditText>(R.id.etHorario)?.text?.toString()?.trim() ?: ""
        val especialidades = view?.findViewById<EditText>(R.id.etEspecialidades)?.text?.toString()?.trim()
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val imagenUrl = view?.findViewById<EditText>(R.id.etImagenUrl)?.text?.toString()?.trim() ?: ""

        if (itemAEditar != null) {
            val datos = mutableMapOf(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "direccion" to direccion,
                "ciudad" to ciudad,
                "barrio" to barrio,
                "telefono" to telefono,
                "horario" to horario,
                "especialidades" to especialidades,
                "imagenUrl" to imagenUrl
            )
            repo.updateVivero(itemAEditar.id, datos, { dismiss(); onSuccess() }, {})
        } else {
            val nuevo = Vivero(
                nombre = nombre,
                descripcion = descripcion,
                direccion = direccion,
                ciudad = ciudad,
                barrio = barrio,
                telefono = telefono,
                horario = horario,
                especialidades = especialidades,
                imagenUrl = imagenUrl,
                usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                usuarioNombre = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
            )
            repo.agregarVivero(nuevo, { dismiss(); onSuccess() }, {})
        }
    }
}

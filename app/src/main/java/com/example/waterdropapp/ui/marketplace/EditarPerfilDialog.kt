package com.example.waterdropapp.ui.marketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.UsuarioMarket
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class EditarPerfilDialog(
    private val itemAEditar: UsuarioMarket? = null,
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_editar_perfil_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (itemAEditar != null) cargarDatosExistente(itemAEditar)
        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dismiss() }
    }

    private fun cargarDatosExistente(usuario: UsuarioMarket) {
        view?.findViewById<EditText>(R.id.etNombre)?.setText(usuario.nombre)
        view?.findViewById<EditText>(R.id.etFotoUrl)?.setText(usuario.fotoUrl)
        view?.findViewById<EditText>(R.id.etCiudad)?.setText(usuario.ciudad)
    }

    private fun guardar() {
        val repo = FirestoreRepository()
        val nombre = view?.findViewById<EditText>(R.id.etNombre)?.text?.toString()?.trim() ?: ""
        val fotoUrl = view?.findViewById<EditText>(R.id.etFotoUrl)?.text?.toString()?.trim() ?: ""
        val ciudad = view?.findViewById<EditText>(R.id.etCiudad)?.text?.toString()?.trim() ?: ""

        if (itemAEditar != null) {
            val datos = mutableMapOf(
                "nombre" to nombre,
                "fotoUrl" to fotoUrl,
                "ciudad" to ciudad
            )
            repo.updateUsuarioMarket(itemAEditar.id, datos, { dismiss(); onSuccess() }, {})
        } else {
            val auth = FirebaseAuth.getInstance().currentUser
            val nuevo = UsuarioMarket(
                id = auth?.uid ?: "",
                nombre = nombre,
                email = auth?.email ?: "",
                fotoUrl = fotoUrl,
                ciudad = ciudad,
                fechaRegistro = System.currentTimeMillis()
            )
            repo.crearUsuarioMarket(nuevo, { dismiss(); onSuccess() }, {})
        }
    }
}

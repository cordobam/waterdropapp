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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth

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
        val chipId = when (usuario.categoria.lowercase()) {
            "planta" -> R.id.chipCategoriaPlanta
            "semilla" -> R.id.chipCategoriaSemilla
            "esqueje" -> R.id.chipCategoriaEsqueje
            else -> null
        }
        chipId?.let { view?.findViewById<Chip>(it)?.isChecked = true }
        view?.findViewById<SwitchCompat>(R.id.swAceptaTrueque)?.isChecked = usuario.aceptaTrueque
        view?.findViewById<EditText>(R.id.etPrecio)?.setText(usuario.precio.toString())
        view?.findViewById<EditText>(R.id.etImagenUrl)?.setText(usuario.imagenUrl)
        view?.findViewById<EditText>(R.id.etCiudad)?.setText(usuario.ciudad)
        view?.findViewById<EditText>(R.id.etBarrio)?.setText(usuario.barrio)
    }

    private fun guardar() {
        val repo = FirestoreRepository()
        val titulo = view?.findViewById<EditText>(R.id.etTitulo)?.text?.toString()?.trim() ?: ""
        val descripcion = view?.findViewById<EditText>(R.id.etDescripcion)?.text?.toString()?.trim() ?: ""
        val categoria = getSelectedCategoria()
        val precio = view?.findViewById<EditText>(R.id.etPrecio)?.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
        val aceptaTrueque = view?.findViewById<SwitchCompat>(R.id.swAceptaTrueque)?.isChecked ?: false
        val imagenUrl = view?.findViewById<EditText>(R.id.etImagenUrl)?.text?.toString()?.trim() ?: ""
        val ciudad = view?.findViewById<EditText>(R.id.etCiudad)?.text?.toString()?.trim() ?: ""
        val barrio = view?.findViewById<EditText>(R.id.etBarrio)?.text?.toString()?.trim() ?: ""

        if (itemAEditar != null) {
            val datos = mutableMapOf(
                "titulo" to titulo,
                "descripcion" to descripcion,
                "categoria" to categoria,
                "precio" to precio,
                "aceptaTrueque" to aceptaTrueque,
                "imagenUrl" to imagenUrl,
                "ciudad" to ciudad,
                "barrio" to barrio
            )
            repo.updatePublicacion(itemAEditar.id, datos, { dismiss(); onSuccess() }, {})
        } else {
            val nueva = Publicacion(
                usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                nombreUsuario = FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                precio = precio,
                aceptaTrueque = aceptaTrueque,
                imagenUrl = imagenUrl,
                ciudad = ciudad,
                barrio = barrio,
                fechaPublicacion = System.currentTimeMillis()
            )
            repo.agregarPublicacion(nueva, { dismiss(); onSuccess() }, {})
        }
    }

    private fun getSelectedCategoria(): String {
        val chipGroup = view?.findViewById<ChipGroup>(R.id.chipGroupCategoria) ?: return ""
        val checkedId = chipGroup.checkedChipId
        return if (checkedId != -1) {
            view?.findViewById<Chip>(checkedId)?.text?.toString()?.lowercase() ?: ""
        } else ""
    }
}

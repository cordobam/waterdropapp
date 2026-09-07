package com.example.waterdropapp.ui.marketplace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class PublicacionFormlDialog(
    private val itemAEditar: Publicacion? = null,
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    private var imagenNuevaPath: String? = null
    private var imageViewActual: ImageView? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val path = guardarImagenInterna(it)
            imagenNuevaPath = path
            actualizarPreview(path)
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageViewActual?.let { img ->
                val path = img.tag as String
                imagenNuevaPath = path
                actualizarPreview(path)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_publicacion_form_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSeleccionarFoto = view.findViewById<MaterialButton>(R.id.btnSeleccionarFoto)
        val cardPreview = view.findViewById<MaterialCardView>(R.id.cardPreviewFoto)
        val imgPreview = view.findViewById<ImageView>(R.id.imgPreviewFoto)

        btnSeleccionarFoto.setOnClickListener {
            mostrarSelectorImagen()
        }

        // Cambiar textos según edición o creación
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        val tvTituloDialog = view.findViewById<TextView>(R.id.tvTituloDialog)
        
        if (itemAEditar != null) {
            btnGuardar.text = "Guardar cambios"
            tvTituloDialog.text = "Editar publicación"
            cargarDatosExistente(itemAEditar!!)
        } else {
            btnGuardar.text = "Publicar"
            tvTituloDialog.text = "Nueva publicación"
        }

        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dismiss() }
    }

    private fun mostrarSelectorImagen() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar foto")
            .setItems(arrayOf("Galería", "Cámara")) { _, which ->
                when (which) {
                    0 -> pickImage.launch("image/*")
                    1 -> tomarFoto()
                }
            }
            .show()
    }

    private fun tomarFoto() {
        val fileName = "publicacion_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)

        // Generas la Uri para guardar la imagen
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        imageViewActual = requireView().findViewById<ImageView>(R.id.imgPreviewFoto)
        imageViewActual?.tag = file.absolutePath

        // Le pasas directamente la uri en lugar del intent
        takePhoto.launch(uri)
    }

    private fun guardarImagenInterna(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val fileName = "publicacion_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    private fun actualizarPreview(path: String) {
        val cardPreview = requireView().findViewById<MaterialCardView>(R.id.cardPreviewFoto)
        val imgPreview = requireView().findViewById<ImageView>(R.id.imgPreviewFoto)
        val etImagenUrl = requireView().findViewById<EditText>(R.id.etImagenUrl)

        cardPreview.visibility = View.VISIBLE
        etImagenUrl.setText(path)

        Glide.with(this)
            .load(File(path))
            .centerCrop()
            .into(imgPreview)
    }

    private fun cargarDatosExistente(publicacion: Publicacion) {
        requireView().findViewById<EditText>(R.id.etTitulo)?.setText(publicacion.titulo)
        requireView().findViewById<EditText>(R.id.etDescripcion)?.setText(publicacion.descripcion)

        val chipId = when (publicacion.categoria.lowercase()) {
            "planta" -> R.id.chipCategoriaPlanta
            "semilla" -> R.id.chipCategoriaSemilla
            "esqueje" -> R.id.chipCategoriaEsqueje
            else -> null
        }
        chipId?.let { requireView().findViewById<Chip>(it)?.isChecked = true }

        requireView().findViewById<SwitchCompat>(R.id.swAceptaTrueque)?.isChecked = publicacion.aceptaTrueque
        requireView().findViewById<EditText>(R.id.etPrecio)?.setText(publicacion.precio.toString())
        requireView().findViewById<EditText>(R.id.etCiudad)?.setText(publicacion.ciudad)
        requireView().findViewById<EditText>(R.id.etBarrio)?.setText(publicacion.barrio)

        // Cargar imagen existente
        val cardPreview = requireView().findViewById<MaterialCardView>(R.id.cardPreviewFoto)
        val imgPreview = requireView().findViewById<ImageView>(R.id.imgPreviewFoto)
        val etImagenUrl = requireView().findViewById<EditText>(R.id.etImagenUrl)

        if (publicacion.imagenUrl.isNotEmpty()) {
            etImagenUrl.setText(publicacion.imagenUrl)

            if (publicacion.imagenUrl.startsWith("http")) {
                // URL remota
                cardPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(publicacion.imagenUrl)
                    .centerCrop()
                    .into(imgPreview)
            } else {
                // Archivo local
                val file = File(publicacion.imagenUrl)
                if (file.exists()) {
                    cardPreview.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(file)
                        .centerCrop()
                        .into(imgPreview)
                }
            }
        }
    }

    private fun guardar() {
        val repo = FirestoreRepository()
        val titulo = requireView().findViewById<EditText>(R.id.etTitulo)?.text?.toString()?.trim() ?: ""
        val descripcion = requireView().findViewById<EditText>(R.id.etDescripcion)?.text?.toString()?.trim() ?: ""
        val categoria = getSelectedCategoria()
        val precio = requireView().findViewById<EditText>(R.id.etPrecio)?.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
        val aceptaTrueque = requireView().findViewById<SwitchCompat>(R.id.swAceptaTrueque)?.isChecked ?: false
        val imagenUrl = requireView().findViewById<EditText>(R.id.etImagenUrl)?.text?.toString()?.trim() ?: ""
        val ciudad = requireView().findViewById<EditText>(R.id.etCiudad)?.text?.toString()?.trim() ?: ""
        val barrio = requireView().findViewById<EditText>(R.id.etBarrio)?.text?.toString()?.trim() ?: ""

        if (titulo.isEmpty()) {
            requireView().findViewById<EditText>(R.id.etTitulo)?.error = "Título requerido"
            return
        }

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
        val chipGroup = requireView().findViewById<ChipGroup>(R.id.chipGroupCategoria) ?: return ""
        val checkedId = chipGroup.checkedChipId
        return if (checkedId != -1) {
            requireView().findViewById<Chip>(checkedId)?.text?.toString()?.lowercase() ?: ""
        } else ""
    }
}
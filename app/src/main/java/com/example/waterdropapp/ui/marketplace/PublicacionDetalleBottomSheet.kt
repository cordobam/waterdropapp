package com.example.waterdropapp.ui.marketplace

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Oferta
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class PublicacionDetalleBottomSheet(
    private val publicacion: Publicacion,
    private val onOfertar: () -> Unit,
    private val onChatear: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: View? = null
    private lateinit var repository: FirestoreRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_publicacion_detalle, container, false)
        _binding = view
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = FirestoreRepository()

        cargarDatos()
        setupBotones()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatos() {
        val binding = _binding!!

        // Imagen
        val imgPublicacion = binding.findViewById<ImageView>(R.id.imgPublicacionDetalle)
        if (publicacion.imagenUrl.isNotEmpty()) {
            if (publicacion.imagenUrl.startsWith("http")) {
                Glide.with(this).load(publicacion.imagenUrl).centerCrop().into(imgPublicacion)
            } else {
                val file = File(publicacion.imagenUrl)
                if (file.exists()) {
                    Glide.with(this).load(file).centerCrop().into(imgPublicacion)
                }
            }
        }

        // Datos básicos
        binding.findViewById<TextView>(R.id.tvCategoriaDetalle).text = publicacion.categoria.uppercase()
        binding.findViewById<TextView>(R.id.tvNombreDetalle).text = publicacion.titulo

        val precio = if (publicacion.precio == 0.0) "Gratis" else "$${publicacion.precio.toInt()}"
        binding.findViewById<TextView>(R.id.tvPrecioDetalle).text = precio

        val tvTrueque = binding.findViewById<TextView>(R.id.tvTruequeDetalle)
        tvTrueque.visibility = if (publicacion.aceptaTrueque) View.VISIBLE else View.GONE

        binding.findViewById<TextView>(R.id.tvDescripcionDetalle).text = publicacion.descripcion
        binding.findViewById<TextView>(R.id.tvUbicacionDetalle).text = "${publicacion.ciudad} · ${publicacion.barrio}"

        // Vendedor
        binding.findViewById<TextView>(R.id.tvNombreVendedor).text = publicacion.nombreUsuario
        
        val imgVendedor = binding.findViewById<ImageView>(R.id.imgVendedor)
        // TODO: Cargar foto del vendedor si está disponible

        binding.findViewById<TextView>(R.id.tvCalificacionVendedor).text = "--"
        binding.findViewById<TextView>(R.id.tvTotalCalificaciones).text = "(0)"

        // Trueque
        val tvTrueque_ = binding.findViewById<TextView>(R.id.tvTruequeDetalle)
        tvTrueque_.text = if (publicacion.aceptaTrueque) "🔄 Acepta trueque" else ""
        tvTrueque_.visibility = if (publicacion.aceptaTrueque) View.VISIBLE else View.GONE

        // Botones
        val btnOfertar = binding.findViewById<MaterialButton>(R.id.btnOfertar)
        val btnChatear = binding.findViewById<MaterialButton>(R.id.btnChatear)

        btnOfertar.setOnClickListener {
            dismiss()
            onOfertar()
        }

        btnChatear.setOnClickListener {
            dismiss()
            onChatear()
        }

        // Verificar si ya hay oferta propia aceptada
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isNotEmpty()) {
            repository.getOfertasDePublicacion(publicacion.id!!, { ofertas ->
                val miOferta = ofertas.find { it.compradorId == currentUid && it.estado == com.example.waterdropapp.data.firebase.model.EstadoOferta.ACEPTADA }
                if (miOferta != null) {
                    // Ya hay oferta aceptada, mostrar estado
                    // TODO: Mostrar estado en UI
                }
            }, {})
        }
    }

    private fun setupBotones() {
        // Los listeners están en cargarDatos
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
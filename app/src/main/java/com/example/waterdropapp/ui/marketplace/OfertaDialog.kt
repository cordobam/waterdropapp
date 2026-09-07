package com.example.waterdropapp.ui.marketplace

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Oferta
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class OfertaDialog(
    private val publicacion: Publicacion,
    private val onSuccess: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var repository: FirestoreRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_oferta, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = FirestoreRepository()

        setupPublicacionInfo(view)
        setupRadioGroup(view)
        setupBotones(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPublicacionInfo(view: View) {
        val precioTexto = if (publicacion.precio == 0.0) "Gratis" else "$${publicacion.precio.toInt()}"
        view.findViewById<TextView>(R.id.tvPublicacionInfo).text = 
            "Publicación: ${publicacion.titulo} • $precioTexto"
        
        view.findViewById<TextView>(R.id.tvPrecioMaximo).text = 
            "Precio de la publicación: $precioTexto"
    }

    private fun setupRadioGroup(view: View) {
        val rgTipoOferta = view.findViewById<RadioGroup>(R.id.rgTipoOferta)
        val layoutDinero = view.findViewById<View>(R.id.layoutDinero)
        val layoutTrueque = view.findViewById<View>(R.id.layoutTrueque)

        // Configurar visibilidad inicial según trueque
        val rbDinero = view.findViewById<RadioButton>(R.id.rbDinero)
        val rbTrueque = view.findViewById<RadioButton>(R.id.rbTrueque)
        val rbDineroTrueque = view.findViewById<RadioButton>(R.id.rbDineroTrueque)

        if (!publicacion.aceptaTrueque) {
            rbTrueque.visibility = View.GONE
            rbDineroTrueque.visibility = View.GONE
            rgTipoOferta.check(R.id.rbDinero)
        }

        rgTipoOferta.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDinero -> {
                    layoutDinero.visibility = View.VISIBLE
                    layoutTrueque.visibility = View.GONE
                }
                R.id.rbTrueque -> {
                    layoutDinero.visibility = View.GONE
                    layoutTrueque.visibility = View.VISIBLE
                }
                R.id.rbDineroTrueque -> {
                    layoutDinero.visibility = View.VISIBLE
                    layoutTrueque.visibility = View.VISIBLE
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupBotones(view: View) {
        view.findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnEnviarOferta).setOnClickListener {
            validarYEnviar(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validarYEnviar(view: View) {
        val rgTipoOferta = view.findViewById<RadioGroup>(R.id.rgTipoOferta)
        val checkedId = rgTipoOferta.checkedRadioButtonId

        val tipoOferta = when (checkedId) {
            R.id.rbDinero -> com.example.waterdropapp.data.firebase.model.TipoOferta.DINERO
            R.id.rbTrueque -> com.example.waterdropapp.data.firebase.model.TipoOferta.TRUEQUE
            R.id.rbDineroTrueque -> com.example.waterdropapp.data.firebase.model.TipoOferta.DINERO_TRUEQUE
            else -> {
                Toast.makeText(requireContext(), "Selecciona un tipo de oferta", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val etMonto = view.findViewById<EditText>(R.id.etMonto)
        val etDescripcionTrueque = view.findViewById<EditText>(R.id.etDescripcionTrueque)
        val etMensaje = view.findViewById<EditText>(R.id.etMensaje)

        val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
        val descripcionTrueque = etDescripcionTrueque.text.toString().trim()
        val mensaje = etMensaje.text.toString().trim()

        // Validaciones
        when {
            tipoOferta == com.example.waterdropapp.data.firebase.model.TipoOferta.DINERO || 
            tipoOferta == com.example.waterdropapp.data.firebase.model.TipoOferta.DINERO_TRUEQUE -> {
                if (monto <= 0) {
                    etMonto.error = "Ingresa un monto válido"
                    return
                }
                if (publicacion.precio > 0 && monto > publicacion.precio) {
                    etMonto.error = "El monto no puede superar el precio de la publicación"
                    return
                }
            }
            tipoOferta == com.example.waterdropapp.data.firebase.model.TipoOferta.TRUEQUE || tipoOferta == com.example.waterdropapp.data.firebase.model.TipoOferta.DINERO_TRUEQUE -> {
                val desc = view.findViewById<EditText>(R.id.etDescripcionTrueque).text.toString().trim()
                if (desc.isEmpty()) {
                    view.findViewById<EditText>(R.id.etDescripcionTrueque).error = "Describe qué ofreces a cambio"
                    return
                }
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val oferta = Oferta(
            publicacionId = publicacion.id!!,
            compradorId = currentUser.uid,
            compradorNombre = currentUser.displayName ?: "Usuario",
            tipo = tipoOferta,
            monto = monto,
            descripcionTrueque = descripcionTrueque,
            mensaje = mensaje
        )

        val repo = FirestoreRepository()
        repo.agregarOferta(oferta, { 
            dismiss()
            onSuccess()
            Toast.makeText(requireContext(), "Oferta enviada correctamente", Toast.LENGTH_SHORT).show()
        }, { e ->
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        })
    }
}
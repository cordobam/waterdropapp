package com.example.waterdropapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.data.DBHelper
import com.example.waterdropapp.ui.grupos.AdapterGrupos
import com.example.waterdropapp.ui.plantas.AdapterPlantas
import com.google.android.material.card.MaterialCardView
import java.io.File
import com.example.waterdropapp.data.Plantas
import com.google.android.material.snackbar.Snackbar


class CargarPlantasFragment : Fragment(R.layout.fragment_cargar_plantas) {

    private lateinit var db: DBHelper
    private lateinit var plantasAdapterAct: AdapterPlantas
    private var imagenPathGuardado: String? = null
    private lateinit var imgPreview: ImageView
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var cardFoto: MaterialCardView
    private var imagenNuevaPath: String? = null
    private var imageViewActual: ImageView? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val path = guardarImagenInterna(it)
                imagenNuevaPath = path

                imageViewActual?.let { img ->
                    Glide.with(this)
                        .load(File(path))
                        .centerCrop()
                        .into(img)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //carga spinner con datos
        db = DBHelper(requireContext())
        val spinnerGrupos = view.findViewById<Spinner>(R.id.spinnerGrupos)
        val grupos = db.getGrupos()
        val nombresGrupos = grupos.map{it.nombre}
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresGrupos
        )

        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerGrupos.adapter = adapter

        // carga de foto
        layoutPlaceholder = view.findViewById<LinearLayout>(R.id.layoutPlaceholder)
        cardFoto = view.findViewById<MaterialCardView>(R.id.cardSeleccionarFoto)
        imgPreview = view.findViewById(R.id.imgPreview)

        cardFoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            // seleccion de spinner
            val posicion = spinnerGrupos.selectedItemPosition
            if (posicion == AdapterView.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Seleccione un grupo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val grupoSeleccionado = grupos[posicion]
            val codigoGrupo = grupoSeleccionado.grupo_id

            // plantas
            val nombre = view.findViewById<EditText>(R.id.etNombre).text.toString()
            val diasString = view.findViewById<EditText>(R.id.etDiasMax).text.toString()
            val dias = diasString.toIntOrNull() ?: 0
            //insert plantas
            val values = db.putPlantas(nombre, dias, imagenPathGuardado)

            // insert gruposplantas
            val valueInt: Int = values.toInt()
            val grupo_plantas = db.putGruposPlantas(valueInt,codigoGrupo)

            Toast.makeText(
                requireContext(),
                "Planta $nombre cargada con exito",
                Toast.LENGTH_SHORT
            ).show()
        }

        plantasAdapterAct = AdapterPlantas(
            modo = AdapterPlantas.Modo.ACTUALIZAR_PLANTAS,
            onEditarPlanta = { id -> editarPlantas(id) },
            onEliminarPlanta = { id -> eliminarPlantas(id) }
        )

        val rv = view.findViewById<RecyclerView>(R.id.rvEliminarActualizarPlantas)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = plantasAdapterAct

        val botonverplantas = view.findViewById<Button>(R.id.btnVerPlantas)
        val cardLista =  view.findViewById<MaterialCardView>(R.id.cardContenedorLista)

        botonverplantas.setOnClickListener {
            cardLista.visibility = View.VISIBLE
            val db = DBHelper(requireContext())
            val plantas = db.obtenerEstadoPlantas()
            plantasAdapterAct.submitList(plantas)
        }
    }

    private fun findViewById(cardContenedorLista: Int) {}

    fun editarPlantas(id:Int) {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_editar_planta,
            null
        )

        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etDias = dialogView.findViewById<EditText>(R.id.etDias)
        val spGrupos = dialogView.findViewById<Spinner>(R.id.spGrupos)
        val imgPlanta = dialogView.findViewById<ImageView>(R.id.imgPlantaEditar)
        val btnCambiarFoto = dialogView.findViewById<Button>(R.id.btnCambiarFoto)

        val plantaActual = db.getPlantasPorId(id)

        etNombre.setText(plantaActual?.nombre)
        etDias.setText(plantaActual?.dias_max_sin_riego.toString())

        imagenNuevaPath = plantaActual?.imagen_path

        // Mostrar imagen si tiene
        plantaActual?.imagen_path?.let { path ->
            Glide.with(requireContext())
                .load(File(path))
                .into(imgPlanta)
        }
        // Botón cambiar foto
        btnCambiarFoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        val grupos = db.getGrupos()
        val nombresGrupos = grupos.map{it.nombre}

        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresGrupos
        )

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGrupos.adapter = adapterSpinner

        // posicion del grupo actual
        val posicionGrupoActual = grupos.indexOfFirst {
            it.grupo_id == plantaActual?.grupo_id
        }

        if (posicionGrupoActual >= 0) {
            spGrupos.setSelection(posicionGrupoActual)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Planta")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->

                // seleccion de spinner
                val posicion = spGrupos.selectedItemPosition

                val grupoSeleccionado = grupos[posicion]
                val codigoGrupo = grupoSeleccionado.grupo_id

                val nombre = etNombre.text.toString()
                val diasInt = etDias.text.toString().toIntOrNull() ?: 0

                // insert gruposplantas
                val grupo_plantas = db.actualizarGrupoPlanta(id,codigoGrupo)

                db.actualizarPlantas(id, nombre, diasInt, imagenNuevaPath  )
                Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                plantasAdapterAct.submitList(
                    db.obtenerEstadoPlantas()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPlantas(id: Int) {

        val filas = db.eliminarPlantas(id)

        if (filas > 0) {

            // Refrescamos primero la lista
            plantasAdapterAct.submitList(db.obtenerEstadoPlantas())

            Snackbar.make(requireView(), "Planta eliminada", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {

                    db.reactivarPlanta(id)
                    plantasAdapterAct.submitList(db.obtenerEstadoPlantas())
                }
                .show()

        } else {
            Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    fun guardarImagenInterna(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val fileName = "planta_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }
}
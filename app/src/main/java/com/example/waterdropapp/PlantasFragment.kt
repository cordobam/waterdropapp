package com.example.waterdropapp

import android.graphics.Rect
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.data.local.model.DBHelper
import com.example.waterdropapp.domain.model.FiltroRiego
import com.example.waterdropapp.ui.grupos.AdapterGrupos
import com.example.waterdropapp.ui.plantas.AdapterPlantas
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.util.Date
import java.util.Locale
import com.example.waterdropapp.data.repository.PlantaRepository
import com.example.waterdropapp.data.repository.GrupoRepository
import com.example.waterdropapp.data.repository.RiegoRepository
import com.example.waterdropapp.ui.plantas.PlantasBottomSheet
import com.google.android.material.snackbar.Snackbar
import java.io.File


class PlantasFragment : Fragment(R.layout.fragment_plantas) {

    private lateinit var db: DBHelper
    private lateinit var plantasAdapter: AdapterPlantas
    private lateinit var gruposAdapter: AdapterGrupos
    private lateinit var plantaRepo: PlantaRepository
    private lateinit var grupoRepo: GrupoRepository
    private lateinit var riegoRepo: RiegoRepository
    private var imagenNuevaPath: String? = null
    private var imageViewActual: ImageView? = null
    private lateinit var imgPreview: ImageView
    private lateinit var layoutPlaceholder: LinearLayout

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val path = guardarImagenInterna(it)
                imagenNuevaPath = path

                // Cambiamos visibilidad solo para la carga inicial (cuando no hay imageViewActual)
                if (imageViewActual == null) {
                    imgPreview.visibility = View.VISIBLE
                    layoutPlaceholder.visibility = View.GONE
                }

                // Determinamos cuál es el destino
                val targetImageView = imageViewActual ?: imgPreview

                // CARGA LA IMAGEN SIEMPRE (Quitamos el imageViewActual?.let)
                Glide.with(this)
                    .load(File(path))
                    .centerCrop()
                    .into(targetImageView)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //db = DBHelper(requireContext())
        val helper = DBHelper(requireContext())
        plantaRepo = PlantaRepository(helper)
        riegoRepo = RiegoRepository(helper)
        grupoRepo = GrupoRepository(helper)

        // Dentro de tu Fragment, por ejemplo en onViewCreated:
        val fab = view.findViewById<FloatingActionButton>(R.id.fabPrincipal)

        fab.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, CargasVariasFragment())
                .addToBackStack(null)  // permite volver con el botón atrás
                .commit()
        }

        val fecha: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        plantasAdapter = AdapterPlantas (
            modo = AdapterPlantas.Modo.MOSTRAR_PLANTAS,
            onRegarClick = { plantaId ->
                riegoRepo.putRiegos(plantaId, fecha)

                cargarPlantas()

                Toast.makeText(
                    requireContext(),
                    "Planta regada con exito",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onEditarClick = {plantaId ->
                val estado = plantaRepo.obtenerEstadoPlantasxId(plantaId)
                val sheet = PlantasBottomSheet(
                    listaPlantas = if(estado != null) listOf(estado) else emptyList(),
                    onEditar = {id -> editarPlantas(id)},
                    onEliminar = {id , view -> eliminarPlantas(id, view) }
                )
                sheet.show(parentFragmentManager, "EditarPlantaSheet")
            }
        )


        // me sirve para tener dos botones y que cada uno haga algo
        gruposAdapter = AdapterGrupos(
            modo = AdapterGrupos.Modo.MOSTRAR_GRUPOS,
            onVerGrupo = {grupoId ->

                cargarPlantasPorGrupo(grupoId)
            },
            onRegarGrupo = {grupoId ->
                riegoRepo.putRiegoPorGrupo(grupoId, fecha)
                Toast.makeText(requireContext(), "Grupo regado", Toast.LENGTH_SHORT).show()
            }
        )

        // recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPlantas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = plantasAdapter

        // para hacer el filtro de la plantas

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFiltro)

        chipGroup.setOnCheckedStateChangeListener  { group, checkedIds  ->
            val chipId = checkedIds.firstOrNull()

            val FiltroRiego = when (chipId) {
                R.id.chipTodas -> FiltroRiego.TODAS
                R.id.chipProximas -> FiltroRiego.PROXIMAS
                R.id.chipVencidas -> FiltroRiego.VENCIDAS
                else -> FiltroRiego.TODAS
            }

            val lista = plantaRepo.obtenerEstadoPlantasXRiego(FiltroRiego)
            val listaOrdenada = lista.sortedByDescending { it.diasSinRegar }

            plantasAdapter.submitList(listaOrdenada)
        }

        // para el espaciado
        recyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                                            parent: RecyclerView, state: RecyclerView.State) {
                    outRect.bottom = 16
                }
            }
        )

        // carga principal
        cargarPlantas()
        chipGroup.visibility = View.VISIBLE

        // tabs
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.getTabAt(0)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        recyclerView.adapter = plantasAdapter
                        chipGroup.visibility = View.VISIBLE
                        cargarPlantas()
                    }
                    1 -> {
                        recyclerView.adapter = gruposAdapter
                        chipGroup.visibility = View.GONE
                        cargarGrupos()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarPlantas() {
        val lista = plantaRepo.obtenerEstadoPlantas()
        val listaOrdenada = lista.sortedByDescending { it.diasSinRegar }
        plantasAdapter.submitList(listaOrdenada)
    }

    private fun cargarGrupos() {
        val grupos = grupoRepo.getEstadosGrupos()
        gruposAdapter.submitList(grupos)
    }

    // me sirve para seleccionar el otro tab y mostrar las plantas ahi
    private fun cargarPlantasPorGrupo(grupoId: Int) {
        //val plantas = db.obtenerEstadoPlantasPorGrupo(grupoId)
        //plantasAdapter.submitList(plantas)

        val tabLayout = view?.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout?.getTabAt(0)?.select()   // tab Plantas

        val plantas = plantaRepo.obtenerEstadoPlantasPorGrupo(grupoId)
        plantasAdapter.submitList(plantas)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun editarPlantas(id:Int) {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_editar_planta,
            null
        )

        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etDias = dialogView.findViewById<EditText>(R.id.etDias)
        val etDiasInv = dialogView.findViewById<EditText>(R.id.etDiasInv)
        val spGrupos = dialogView.findViewById<Spinner>(R.id.spGrupos)
        val imgPlanta = dialogView.findViewById<ImageView>(R.id.imgPlantaEditar)
        val btnCambiarFoto = dialogView.findViewById<Button>(R.id.btnCambiarFoto)

        val plantaActual = plantaRepo.getPlantasPorId(id)

        etNombre.setText(plantaActual?.nombre)
        etDias.setText(plantaActual?.dias_max_sin_riego.toString())
        etDiasInv.setText(plantaActual?.dias_max_sin_riego_invierno.toString())

        imagenNuevaPath = plantaActual?.imagen_path

        // Mostrar imagen si tiene
        plantaActual?.imagen_path?.let { path ->
            Glide.with(requireContext())
                .load(File(path))
                .into(imgPlanta)
        }
        // Botón cambiar foto
        btnCambiarFoto.setOnClickListener {
            imageViewActual = imgPlanta
            pickImage.launch("image/*")
        }

        val grupos = grupoRepo.getGrupos()
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
                val diasInt_inv = etDiasInv.text.toString().toIntOrNull() ?: 0

                // insert gruposplantas
                val grupo_plantas = grupoRepo.actualizarGrupoPlanta(id,codigoGrupo)

                plantaRepo.actualizarPlantas(id, nombre, diasInt, imagenNuevaPath,diasInt_inv  )
                Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                plantasAdapter.submitList(
                    plantaRepo.obtenerEstadoPlantas()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun eliminarPlantas(id: Int, view: View? = null) {

        val filas = plantaRepo.softDeletePlanta(id, activo = false)

        if (filas > 0) {

            // Refrescamos primero la lista
            //plantasAdapterAct.submitList(db.obtenerEstadoPlantas())
            val snackbarView = view ?: requireView()
            val listaActualizada = plantaRepo.obtenerEstadoPlantas()

            plantasAdapter.submitList(listaActualizada)

            Snackbar.make(snackbarView, "Planta eliminada", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {

                    plantaRepo.softDeletePlanta(id, true)
                    //plantasAdapterAct.submitList(db.obtenerEstadoPlantas())
                    val listaReactivada = plantaRepo.obtenerEstadoPlantas()
                    plantasAdapter.submitList(listaReactivada)
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
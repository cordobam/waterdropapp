package com.example.waterdropapp.ui.plantas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.waterdropapp.R
import com.example.waterdropapp.domain.model.TipoActividad
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlantasAccionesSheet(
    private val plantaId: Int,
    private val nombrePlanta: String,
    private val onAccion: (Int, TipoActividad) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_acciones_planta, container, false)

        view.findViewById<TextView>(R.id.tvTituloAcciones).text = "Acciones de $nombrePlanta"

        view.findViewById<LinearLayout>(R.id.rowAccionRiego).setOnClickListener {
            onAccion(plantaId, TipoActividad.RIEGO)
            dismiss()
        }
        view.findViewById<LinearLayout>(R.id.rowAccionAbonado).setOnClickListener {
            onAccion(plantaId, TipoActividad.ABONADO)
            dismiss()
        }
        view.findViewById<LinearLayout>(R.id.rowAccionPodado).setOnClickListener {
            onAccion(plantaId, TipoActividad.PODADO)
            dismiss()
        }

        return view
    }
}
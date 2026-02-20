package com.example.waterdropapp.ui.plantas

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.waterdropapp.CargarGrupoFragment
import com.example.waterdropapp.CargarPlantasFragment

class CargaVariasAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CargarPlantasFragment()
            1 -> CargarGrupoFragment()
            else -> throw IllegalStateException("Posición no soportada")
        }
    }
}
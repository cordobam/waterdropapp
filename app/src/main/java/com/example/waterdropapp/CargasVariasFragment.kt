package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.waterdropapp.databinding.FragmentCargasVariasBinding
import com.example.waterdropapp.ui.plantas.CargaVariasAdapter
import com.google.android.material.tabs.TabLayoutMediator


class CargasVariasFragment : Fragment(R.layout.fragment_cargas_varias) {

    private var _binding: FragmentCargasVariasBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCargasVariasBinding.bind(view)

        // Seteamos el adaptador
        binding.viewPagerCarga.adapter = CargaVariasAdapter(this)

        // Vinculamos el TabLayout con el ViewPager2
        TabLayoutMediator(binding.tabLayoutCargas, binding.viewPagerCarga) { tab, position ->
            tab.text = when (position) {
                0 -> "Nueva Planta"
                1 -> "Nuevo Grupo"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

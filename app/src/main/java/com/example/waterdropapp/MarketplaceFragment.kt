package com.example.waterdropapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.example.waterdropapp.ui.marketplace.AdapterPublicaciones
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MarketplaceFragment : Fragment(R.layout.fragment_marketplace) {
    private val repository = FirestoreRepository()
    private lateinit var adapter: AdapterPublicaciones

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdapterPublicaciones { publicacion ->
            // click en publicacion
        }

        view.findViewById<RecyclerView>(R.id.rvPublicaciones).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MarketplaceFragment.adapter
        }

        cargarDatos()
    }

    private fun cargarDatos() {
        repository.getPublicaciones(
            onSuccess = { lista ->
                Log.d("FIRESTORE_TEST", "✅ ${lista.size} publicaciones")
                adapter.submitList(lista)
            },
            onError = {
                Toast.makeText(requireContext(), "Error al cargar", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
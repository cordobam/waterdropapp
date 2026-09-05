package com.example.waterdropapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.data.firebase.model.UsuarioMarket
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.example.waterdropapp.ui.marketplace.AdapterMisPublicaciones
import com.example.waterdropapp.ui.marketplace.EditarPerfilDialog
import com.example.waterdropapp.ui.marketplace.PublicacionFormlDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class PerfilMarketFragment : Fragment(R.layout.fragment_perfil_market) {

    private val repository = FirestoreRepository()
    private lateinit var adapter: AdapterMisPublicaciones
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var usuarioActual: UsuarioMarket? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        view.findViewById<TextView>(R.id.tvNombreUsuario).text = user?.displayName ?: "Usuario"
        view.findViewById<TextView>(R.id.tvEmailUsuario).text = user?.email ?: ""

        if (user?.photoUrl != null) {
            Glide.with(this)
                .load(user.photoUrl)
                .circleCrop()
                .into(view.findViewById<ImageView>(R.id.imgAvatar))
        }

        adapter = AdapterMisPublicaciones(
            onEditarClick = { publicacion ->
                PublicacionFormlDialog(
                    itemAEditar = publicacion,
                    onSuccess = { cargarMisPublicaciones(view) }
                ).show(parentFragmentManager, "EditarPublicacion")
            },
            onEliminarClick = { publicacion ->
                repository.deletePublicacion(
                    id = publicacion.id,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Publicaci\u00f3n eliminada", Toast.LENGTH_SHORT).show()
                        cargarMisPublicaciones(view)
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )

        view.findViewById<RecyclerView>(R.id.rvMisPublicaciones).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PerfilMarketFragment.adapter
        }

        if (userId.isNotEmpty()) {
            cargarPerfilUsuario(view)
            cargarMisPublicaciones(view)
        }

        view.findViewById<MaterialButton>(R.id.btnCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        view.findViewById<MaterialButton>(R.id.btnEditarPerfil).setOnClickListener {
            EditarPerfilDialog(
                itemAEditar = usuarioActual,
                onSuccess = {cargarPerfilUsuario(view)}
            ).show(parentFragmentManager,"EditarPerfil")
        }
    }

    private fun cargarPerfilUsuario(view: View) {
        repository.getUsuarioMarket(
            id = userId,
            onSuccess = { usuario ->
                usuarioActual = usuario
                view.findViewById<TextView>(R.id.tvNombreUsuario).text = usuario.nombre

                val calificacion = usuario.calificacion
                view.findViewById<TextView>(R.id.tvCalificacion).text =
                    if (calificacion > 0.0) String.format("%.1f", calificacion) else "\u2014"

                if (usuario.fotoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(usuario.fotoUrl)
                        .circleCrop()
                        .into(view.findViewById<ImageView>(R.id.imgAvatar))
                }
            },
            onError = {
                // Sin perfil marketplace creado aún — se queda con datos de Auth
            }
        )
    }

    private fun cargarMisPublicaciones(view: View) {
        repository.getPublicacionesDeUsuario(
            usuarioId = userId,
            onSuccess = { lista ->
                adapter.submitList(lista)
                view.findViewById<TextView>(R.id.tvTotalPublicaciones).text = "${lista.size}"
            },
            onError = {
                Toast.makeText(requireContext(), "Error al cargar publicaciones", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
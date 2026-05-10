package com.example.waterdropapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class PerfilMarketFragment : Fragment(R.layout.fragment_perfil_market) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        view.findViewById<TextView>(R.id.tvNombreUsuario).text = user?.displayName ?: "Usuario"
        view.findViewById<TextView>(R.id.tvEmailUsuario).text = user?.email ?: ""

        view.findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
    }
}
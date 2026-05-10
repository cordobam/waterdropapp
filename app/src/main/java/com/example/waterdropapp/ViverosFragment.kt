package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViverosFragment : Fragment(R.layout.fragment_viveros) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvViveros)
        rv.layoutManager = LinearLayoutManager(requireContext())
        // rv.adapter = tu adapter
    }
}
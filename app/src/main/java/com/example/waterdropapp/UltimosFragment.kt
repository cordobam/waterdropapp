package com.example.waterdropapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.waterdropapp.data.DBHelper
import java.util.Date


class UltimosFragment : Fragment(R.layout.fragment_ultimos) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val db = DBHelper(requireContext())
        val ultimo = db.getUltimosRiegos()
        val sb = StringBuilder()
        ultimo.forEach {
            sb.append("${it.name} - ${it.fecha}\n")
        }

        view.findViewById<TextView>(R.id.tvResultado).text = sb.toString()
    }
}
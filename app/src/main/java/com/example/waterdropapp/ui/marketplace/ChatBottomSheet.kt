package com.example.waterdropapp.ui.marketplace

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.R
import com.example.waterdropapp.data.firebase.model.Chat
import com.example.waterdropapp.data.firebase.model.Mensaje
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class ChatBottomSheet(
    private val chat: Chat,
    private val publicacion: Publicacion
) : BottomSheetDialogFragment() {

    private var _binding: View? = null
    private lateinit var repository: FirestoreRepository
    private var listenerRegistro: com.google.firebase.firestore.ListenerRegistration? = null
    private lateinit var adapter: MensajesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        _binding = view
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = FirestoreRepository()

        setupHeader(view)
        setupRecyclerView(view)
        setupInput(view)
        setupCerrar(view)
        listenMensajes()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupHeader(view: View) {
        val imgPublicacion = view.findViewById<ImageView>(R.id.imgChatPublicacion)
        val tvTitulo = view.findViewById<TextView>(R.id.tvChatTitulo)
        val tvCon = view.findViewById<TextView>(R.id.tvChatCon)

        // Imagen de la publicación
        if (publicacion.imagenUrl.isNotEmpty()) {
            if (publicacion.imagenUrl.startsWith("http")) {
                Glide.with(this).load(publicacion.imagenUrl).centerCrop().into(imgPublicacion)
            } else {
                val file = File(publicacion.imagenUrl)
                if (file.exists()) {
                    Glide.with(this).load(file).centerCrop().into(imgPublicacion)
                }
            }
        }

        tvTitulo.text = publicacion.titulo
        
        // Determinar con quién chateamos
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val otroNombre = chat.nombresParticipantes[currentUid]?.let { nombres ->
            chat.nombresParticipantes.entries.find { it.key != currentUid }?.value ?: "Usuario"
        } ?: "Usuario"
        view.findViewById<TextView>(R.id.tvChatCon).text = "Chat con $otroNombre"

        view.findViewById<ImageView>(R.id.btnCerrarChat).setOnClickListener {
            dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvMensajes)
        rv.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        adapter = MensajesAdapter()
        rv.adapter = adapter

        // Scroll automático
        rv.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            rv.scrollToPosition(adapter.itemCount - 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun listenMensajes() {
        listenerRegistro = repository.listenMensajes(chat.id!!) { mensajes ->
            adapter.submitList(mensajes)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupInput(view: View) {
        val etMensaje = view.findViewById<EditText>(R.id.etMensaje)
        val btnEnviar = view.findViewById<MaterialButton>(R.id.btnEnviarMensaje)

        btnEnviar.setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val mensaje = Mensaje(
                        chatId = chat.id!!,
                        remitenteId = currentUser.uid,
                        remitenteNombre = currentUser.displayName ?: "Usuario",
                        texto = texto
                    )
                    repository.enviarMensaje(chat.id!!, mensaje, {}, {})
                    etMensaje.text.clear()
                }
            }
        }
    }

    private fun setupCerrar(view: View) {
        view.findViewById<ImageView>(R.id.btnCerrarChat).setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistro?.remove()
    }

    // Adapter interno para mensajes
    private inner class MensajesAdapter : RecyclerView.Adapter<MensajesAdapter.MensajeVH>() {
        private var items = mutableListOf<Mensaje>()

        fun submitList(lista: List<Mensaje>) {
            items = lista.toMutableList()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeVH {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_mensaje, parent, false)
            return MensajeVH(view)
        }

        override fun onBindViewHolder(holder: MensajeVH, position: Int) {
            holder.bind(items[position])
        }

        inner class MensajeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvRemitente = itemView.findViewById<TextView>(R.id.tvMensajeRemitente)
            private val tvTexto = itemView.findViewById<TextView>(R.id.tvMensajeTexto)
            private val tvHora = itemView.findViewById<TextView>(R.id.tvMensajeHora)
            private val layoutContainer = itemView.findViewById<ViewGroup>(R.id.layoutMensajeContainer)

            fun bind(mensaje: Mensaje) {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val esMio = mensaje.remitenteId == currentUid

                tvRemitente.text = mensaje.remitenteNombre
                tvTexto.text = mensaje.texto

                val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                tvHora.text = formatter.format(java.util.Date(mensaje.timestamp))

                // Alineación según sea mío o del otro
                val params = layoutContainer.layoutParams as LinearLayout.LayoutParams
                params.gravity = if (esMio) android.view.Gravity.END else android.view.Gravity.START
                layoutContainer.layoutParams = params

                // Color de fondo
                layoutContainer.setBackgroundResource(
                    if (esMio) R.drawable.bg_mensaje_mio else R.drawable.bg_mensaje_otro
                )

                // Visibilidad nombre (solo para mensajes de otros)
                tvRemitente.visibility = if (esMio) View.GONE else View.VISIBLE
            }
        }
    }
}
package com.example.waterdropapp.ui.marketplace

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.waterdropapp.data.firebase.model.Chat
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.repository.FirestoreRepository
import com.example.waterdropapp.ui.marketplace.ChatBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.waterdropapp.R

class ChatsFragment : Fragment() {

    private var _binding: View? = null
    private lateinit var repository: FirestoreRepository
    private var listenerRegistro: ListenerRegistration? = null
    private lateinit var adapter: ChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        _binding = view
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = FirestoreRepository()

        val rv = view.findViewById<RecyclerView>(R.id.rvChats)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatsAdapter()
        rv.adapter = adapter

        listenChats()

        // Empty state
        view.findViewById<TextView>(R.id.tvEmptyChats).visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun listenChats() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        listenerRegistro = repository.listenChatsDeUsuario(uid) { chats ->
            if (chats.isEmpty()) {
                view?.findViewById<TextView>(R.id.tvEmptyChats)?.visibility = View.VISIBLE
                view?.findViewById<RecyclerView>(R.id.rvChats)?.visibility = View.GONE
            } else {
                view?.findViewById<TextView>(R.id.tvEmptyChats)?.visibility = View.GONE
                view?.findViewById<RecyclerView>(R.id.rvChats)?.visibility = View.VISIBLE
                adapter.submitList(chats)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistro?.remove()
    }

    // Adapter interno
    private inner class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.ChatVH>() {
        private var items = mutableListOf<Chat>()

        fun submitList(lista: List<Chat>) {
            items = lista.toMutableList()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_chat, parent, false)
            return ChatVH(view)
        }

        override fun onBindViewHolder(holder: ChatVH, position: Int) {
            holder.bind(items[position])
        }

        inner class ChatVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imgPublicacion = itemView.findViewById<ImageView>(R.id.imgChatItemPublicacion)
            private val tvTitulo = itemView.findViewById<TextView>(R.id.tvChatItemTitulo)
            private val tvUltimoMensaje = itemView.findViewById<TextView>(R.id.tvChatItemUltimoMensaje)
            private val tvHora = itemView.findViewById<TextView>(R.id.tvChatItemHora)
            private val tvNoLeidos = itemView.findViewById<TextView>(R.id.tvChatItemNoLeidos)

            fun bind(chat: Chat) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                tvTitulo.text = chat.publicacionTitulo
                tvUltimoMensaje.text = chat.ultimaMensaje
                
                // Tiempo
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvHora.text = formatter.format(java.util.Date(chat.ultimaMensajeTimestamp))

                // Badge no leídos
                val noLeidos = chat.noLeidos[uid] ?: 0
                val tvBadge = itemView.findViewById<TextView>(R.id.tvChatItemNoLeidos)
                if (noLeidos > 0) {
                    tvBadge.text = noLeidos.toString()
                    tvBadge.visibility = View.VISIBLE
                } else {
                    tvBadge.visibility = View.GONE
                }

                // Imagen
                val imgPublicacion = itemView.findViewById<ImageView>(R.id.imgChatItemPublicacion)
                if (chat.publicacionId.isNotEmpty()) {
                    // TODO: Cargar imagen de la publicación
                    // Por ahora placeholder
                }

                itemView.setOnClickListener {
                    // Abrir chat
                    val repo = FirestoreRepository()
                    repo.getPublicacionById(chat.publicacionId!!, { publicacion ->
                        val chatSheet = ChatBottomSheet(chat, publicacion)
                        chatSheet.show(parentFragmentManager, "ChatBottomSheet")
                    }, {})
                }
            }
        }
    }
}
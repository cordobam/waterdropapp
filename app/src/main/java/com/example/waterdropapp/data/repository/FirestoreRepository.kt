package com.example.waterdropapp.data.repository

import com.example.waterdropapp.data.firebase.model.Chat
import com.example.waterdropapp.data.firebase.model.Mensaje
import com.example.waterdropapp.data.firebase.model.Oferta
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.firebase.model.UsuarioMarket
import com.example.waterdropapp.data.firebase.model.Vivero
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

// FirestoreRepository.kt
class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FIRESTORE_REPO"

    // ========================================================================
    // PUBLICACIONES
    // ========================================================================

    fun getPublicaciones(
        onSuccess: (List<Publicacion>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones")
            .whereEqualTo("activa", true)
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Publicacion::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getPublicacionesDeUsuario(
        usuarioId: String,
        onSuccess: (List<Publicacion>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones")
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("activa", true)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Publicacion::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getPublicacionById(
        id: String,
        onSuccess: (Publicacion) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val publicacion = doc.toObject(Publicacion::class.java)?.copy(id = doc.id)
                if (publicacion != null) {
                    onSuccess(publicacion)
                } else {
                    onError(Exception("Publicación no encontrada"))
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun agregarPublicacion(
        publicacion: Publicacion,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones")
            .add(publicacion)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun updatePublicacion(
        id: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones").document(id)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deletePublicacion(
        id: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones").document(id)
            .update("activa", false)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getAllPublicaciones(
        onSuccess: (List<Publicacion>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("publicaciones")
            .whereEqualTo("activa", true)
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Publicacion::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    // ========================================================================
    // OFERTAS
    // ========================================================================

    fun agregarOferta(
        oferta: Oferta,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("ofertas")
            .add(oferta)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getOfertasDePublicacion(
        publicacionId: String,
        onSuccess: (List<Oferta>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("ofertas")
            .whereEqualTo("publicacionId", publicacionId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Oferta::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getOfertasDeUsuario(
        usuarioId: String,
        onSuccess: (List<Oferta>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("ofertas")
            .whereEqualTo("compradorId", usuarioId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Oferta::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun actualizarEstadoOferta(
        ofertaId: String,
        nuevoEstado: com.example.waterdropapp.data.firebase.model.EstadoOferta,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("ofertas").document(ofertaId)
            .update("estado", nuevoEstado.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // ========================================================================
    // CHATS
    // ========================================================================

    fun crearChat(
        chat: Chat,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("chats")
            .add(chat)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getChatsDeUsuario(
        usuarioId: String,
        onSuccess: (List<Chat>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("chats")
            .whereArrayContains("participantes", usuarioId)
            .orderBy("ultimaMensajeTimestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Chat::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun listenChatsDeUsuario(
        usuarioId: String,
        listener: (List<Chat>) -> Unit
    ): ListenerRegistration {
        return db.collection("chats")
            .whereArrayContains("participantes", usuarioId)
            .orderBy("ultimaMensajeTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val lista = snapshots?.documents?.map { doc ->
                    doc.toObject(Chat::class.java)!!.copy(id = doc.id)
                } ?: emptyList()
                listener(lista)
            }
    }

    fun getMensajes(
        chatId: String,
        onSuccess: (List<Mensaje>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("chats").document(chatId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Mensaje::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun listenMensajes(
        chatId: String,
        listener: (List<Mensaje>) -> Unit
    ): ListenerRegistration {
        return db.collection("chats").document(chatId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val lista = snapshots?.documents?.map { doc ->
                    doc.toObject(Mensaje::class.java)!!.copy(id = doc.id)
                } ?: emptyList()
                listener(lista)
            }
    }

    fun enviarMensaje(
        chatId: String,
        mensaje: Mensaje,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val batch = db.batch()
        
        // Agregar mensaje a subcolección
        val mensajeRef = db.collection("chats").document(chatId)
            .collection("mensajes").document()
        batch.set(mensajeRef, mensaje.copy(id = mensajeRef.id))
        
        // Actualizar chat con último mensaje
        val chatRef = db.collection("chats").document(chatId)
        val noLeidosActualizado = mutableMapOf<String, Int>()
        // Incrementar contador no leídos para el otro participante
        // Esto se haría en una transacción real, aquí simplificado
        batch.update(chatRef, mapOf(
            "ultimaMensaje" to mensaje.texto,
            "ultimaMensajeTimestamp" to mensaje.timestamp
        ))
        
        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun marcarMensajesLeidos(
        chatId: String,
        usuarioId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("chats").document(chatId)
            .update("noLeidos.$usuarioId", 0)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // ========================================================================
    // VIVEROS
    // ========================================================================

    fun getViveros(
        onSuccess: (List<Vivero>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros")
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Vivero::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getViveroById(
        id: String,
        onSuccess: (Vivero) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val vivero = doc.toObject(Vivero::class.java)?.copy(id = doc.id)
                if (vivero != null) {
                    onSuccess(vivero)
                } else {
                    onError(Exception("Vivero no encontrado"))
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun agregarVivero(
        vivero: Vivero,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros")
            .add(vivero)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun updateVivero(
        id: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros").document(id)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deleteVivero(
        id: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros").document(id)
            .update("activo", false)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getAllViveros(
        onSuccess: (List<Vivero>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros")
            .orderBy("nombre")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Vivero::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getViverosByUsuario(
        usuarioId: String,
        onSuccess: (List<Vivero>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("viveros")
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Vivero::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }

    // ========================================================================
    // USUARIO MARKET
    // ========================================================================

    fun getUsuarioMarket(
        id: String,
        onSuccess: (UsuarioMarket) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_market").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(UsuarioMarket::class.java)?.copy(id = doc.id)
                if (usuario != null) {
                    onSuccess(usuario)
                } else {
                    onError(Exception("Usuario no encontrado en marketplace"))
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun crearUsuarioMarket(
        usuario: UsuarioMarket,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_market").document(usuario.id)
            .set(usuario)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun updateUsuarioMarket(
        id: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_market").document(id)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deleteUsuarioMarket(
        id: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_market").document(id)
            .update("activo", false)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getAllUsuariosMarket(
        onSuccess: (List<UsuarioMarket>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("usuarios_market")
            .orderBy("nombre")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(UsuarioMarket::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }
}
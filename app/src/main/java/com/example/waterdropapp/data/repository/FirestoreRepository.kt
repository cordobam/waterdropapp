package com.example.waterdropapp.data.repository

import android.util.Log
import com.example.waterdropapp.data.firebase.model.Publicacion
import com.example.waterdropapp.data.firebase.model.Vivero
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// FirestoreRepository.kt
class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // PUBLICACIONES
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
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    doc.toObject(Publicacion::class.java)!!.copy(id = doc.id)
                }
                onSuccess(lista)
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

    // VIVEROS
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
}
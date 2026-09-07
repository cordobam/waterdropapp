package com.example.waterdropapp.data.firebase.model

data class Chat(
    val id: String = "",
    val publicacionId: String,
    val publicacionTitulo: String,
    val participantes: List<String>,
    val nombresParticipantes: Map<String, String>,
    val ultimaMensaje: String = "",
    val ultimaMensajeTimestamp: Long = 0,
    val noLeidos: Map<String, Int> = emptyMap(),
    val esOfertaAceptada: Boolean = false,
    val ofertaId: String? = null
)

data class Mensaje(
    val id: String = "",
    val chatId: String,
    val remitenteId: String,
    val remitenteNombre: String,
    val texto: String,
    val timestamp: Long = System.currentTimeMillis(),
    val leido: Boolean = false
)
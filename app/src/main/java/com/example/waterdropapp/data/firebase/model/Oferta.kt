package com.example.waterdropapp.data.firebase.model

data class Oferta(
    val id: String = "",
    val publicacionId: String,
    val compradorId: String,
    val compradorNombre: String,
    val tipo: TipoOferta,
    val monto: Double = 0.0,
    val descripcionTrueque: String = "",
    val mensaje: String = "",
    val estado: EstadoOferta = EstadoOferta.PENDIENTE,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TipoOferta {
    DINERO, TRUEQUE, DINERO_TRUEQUE
}

enum class EstadoOferta {
    PENDIENTE, ACEPTADA, RECHAZADA, EXPIRADA
}
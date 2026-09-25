package com.example.waterdropapp.domain.model

enum class TipoActividad(val tag: String, val etiqueta: String) {
    RIEGO("RIEGO", "Riego"),
    ABONADO("ABONADO", "Abonado"),
    PODADO("PODADO", "Podado");

    companion object {
        fun fromDB(valor: String): TipoActividad {
            return entries.firstOrNull { it.tag == valor } ?: RIEGO
        }
    }
}
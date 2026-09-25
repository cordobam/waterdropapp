package com.example.waterdropapp.domain.model

enum class TipoActividad(val tag: String) {
    RIEGO("RIEGO"),
    ABONADO("ABONADO"),
    PODADO("PODADO");

    companion object {
        fun fromDB(valor: String): TipoActividad {
            return entries.firstOrNull { it.tag == valor } ?: RIEGO
        }
    }
}
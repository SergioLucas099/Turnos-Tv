package com.example.turnostv.model

import kotlinx.serialization.Serializable

@Serializable
data class TextoGuardado(
    val _id: String? = null,
    val texto: String,
    val activo: Boolean = false
)
package com.example.turnostv.model

import kotlinx.serialization.Serializable

@Serializable
data class Multimedia(
    val _id: String? = null,
    val tipo: String, // VIDEO, IMAGEN
    val url: String,
    val activo: Boolean = true,
    val sonido: Boolean = false,
    val nombre: String
)
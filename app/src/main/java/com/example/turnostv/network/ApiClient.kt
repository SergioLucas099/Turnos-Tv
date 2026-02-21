package com.example.turnostv.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

object ApiClient {

    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    const val BASE_URL = "http://192.168.2.118:8080"
}

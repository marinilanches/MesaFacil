package com.example.mesafacil.data.models

import java.io.Serializable

data class Adicional(
    val id: String = "",
    val nome: String = "",
    val valor: Double = 0.0,
    val disponivel: Boolean = true,
    val createdAt: Long = 0L
) : Serializable
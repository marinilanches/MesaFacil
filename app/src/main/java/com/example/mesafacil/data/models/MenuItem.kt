package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class MenuItem(
    @DocumentId
    val id: String = "",
    val nome: String = "",
    val categoria: String = "", // Lanches, Bebidas, etc
    val descricao: String = "",
    val valor: Double = 0.0,
    val disponivel: Boolean = true,
    val imagem: String = "",
    val adicionaisDisponiveis: List<String> = emptyList(), // IDs dos adicionais
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

data class Adicional(
    @DocumentId
    val id: String = "",
    val nome: String = "",
    val valor: Double = 0.0,
    val disponivel: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Mesa(
    @DocumentId
    val id: String = "",
    val numero: Int = 0,
    val status: MesaStatus = MesaStatus.LIVRE,
    val quantidadePessoas: Int = 0,
    val valorTotal: Double = 0.0,
    val garcomId: String = "",
    val garcomNome: String = "",
    val mesasUnidas: List<String> = emptyList(),
    val mesaPrincipal: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

enum class MesaStatus {
    LIVRE,
    OCUPADA,
    RESERVADA
}
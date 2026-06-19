package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Mesa(
    val id: String = "",
    val numero: Int = 0,
    val status: MesaStatus = MesaStatus.LIVRE,
    val quantidadePessoas: Int = 0,
    val valorTotal: Double = 0.0,
    val mesasUnidas: List<String> = emptyList()
) : Serializable

enum class MesaStatus {
    LIVRE,
    OCUPADA,
    RESERVADA
}
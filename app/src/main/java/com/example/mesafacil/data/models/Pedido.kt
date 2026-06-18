package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Pedido(
    @DocumentId
    val id: String = "",
    val mesaId: String = "",
    val numeroMesa: Int = 0,
    val itens: List<ItemPedido> = emptyList(),
    val status: StatusPedido = StatusPedido.NOVO,
    val valorTotal: Double = 0.0,
    val observacoes: String = "",
    val garcomId: String = "",
    val garcomNome: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

data class ItemPedido(
    val id: String = "",
    val nome: String = "",
    val categoria: String = "",
    val quantidade: Int = 1,
    val valorUnitario: Double = 0.0,
    val adicionais: List<String> = emptyList(),
    val valorAdicionalUnitario: Double = 0.0,
    val observacoes: String = ""
) : Serializable {

    fun valorTotal(): Double {
        val base = valorUnitario * quantidade
        val adicionaisTotal = valorAdicionalUnitario * quantidade
        return base + adicionaisTotal
    }
}
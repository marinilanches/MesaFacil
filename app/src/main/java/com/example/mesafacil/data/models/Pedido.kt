package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Pedido(
    @DocumentId
    val id: String = "",
    val mesaId: String = "",
    val numeroMesa: Int = 0,
    val itens: List<ItemPedido> = emptyList(),
    val status: PedidoStatus = PedidoStatus.NOVO,
    val valorTotal: Double = 0.0,
    val observacoes: String = "",
    val garcomId: String = "",
    val garcomNome: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

enum class PedidoStatus {
    NOVO,        // Novo pedido
    EM_PREPARO,  // Sendo preparado
    PRONTO,      // Pronto para entregar
    ENTREGUE,    // Entregue ao cliente
    CANCELADO    // Cancelado
}

data class ItemPedido(
    val id: String = "",
    val nome: String = "",
    val categoria: String = "",
    val quantidade: Int = 1,
    val valorUnitario: Double = 0.0,
    val adicionais: List<Adicional> = emptyList(),
    val observacoes: String = ""
) : Serializable {
    fun valorTotal(): Double {
        val valorAdicionais = adicionais.sumOf { it.valor }
        return (valorUnitario + valorAdicionais) * quantidade
    }
}

data class Adicional(
    val id: String = "",
    val nome: String = "",
    val valor: Double = 0.0
) : Serializable

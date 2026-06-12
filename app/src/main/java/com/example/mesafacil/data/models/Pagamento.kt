package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Pagamento(
    @DocumentId
    val id: String = "",
    val mesaId: String = "",
    val numeroMesa: Int = 0,
    val valorTotal: Double = 0.0,
    val quantidadePessoas: Int = 0,
    val quantidadePagantes: Int = 0,
    val pagamentos: List<PagamentoParcial> = emptyList(),
    val formaPagamento: FormaPagamento = FormaPagamento.DINHEIRO,
    val status: PagamentoStatus = PagamentoStatus.PENDENTE,
    val troco: Double = 0.0,
    val garcomId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
) : Serializable {
    fun valorPago(): Double = pagamentos.sumOf { it.valor }
    fun valorRestante(): Double = valorTotal - valorPago()
    fun valorPorPessoa(): Double = if (quantidadePagantes > 0) valorTotal / quantidadePagantes else 0.0
}

data class PagamentoParcial(
    val valor: Double = 0.0,
    val formaPagamento: FormaPagamento = FormaPagamento.DINHEIRO,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

enum class FormaPagamento {
    DINHEIRO,
    PIX,
    CARTAO_DEBITO,
    CARTAO_CREDITO
}

enum class PagamentoStatus {
    PENDENTE,
    PARCIAL,
    COMPLETO,
    CANCELADO
}

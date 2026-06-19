package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.FormaPagamento
import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.data.models.PagamentoParcial
import com.example.mesafacil.data.models.PagamentoStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class PagamentoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pagamentos")

    // Criar novo pagamento
    suspend fun createPagamento(
        mesaId: String,
        numeroMesa: Int,
        valorTotal: Double,
        quantidadePessoas: Int,
        quantidadePagantes: Int,
        garcomId: String
    ): Result<Pagamento> = try {
        val pagamento = Pagamento(
            mesaId = mesaId,
            numeroMesa = numeroMesa,
            valorTotal = valorTotal,
            quantidadePessoas = quantidadePessoas,
            quantidadePagantes = quantidadePagantes,
            status = PagamentoStatus.PENDENTE,
            garcomId = garcomId,
            createdAt = System.currentTimeMillis()
        )

        println("PAGAMENTO QUE VAI PARA FIRESTORE = $pagamento")
        val doc = collection.add(pagamento).await()
        Result.success(pagamento.copy(id = doc.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Obter pagamento por mesa
    suspend fun getPagamentoByMesa(mesaId: String): Pagamento? = try {

        val snapshot = collection
            .whereEqualTo("mesaId", mesaId)
            .get()
            .await()

        println("DOCUMENTOS ENCONTRADOS = ${snapshot.documents.size}")

        val pagamentos = snapshot.documents
            .mapNotNull { it.toObject(Pagamento::class.java) }

        println("PAGAMENTOS ENCONTRADOS = $pagamentos")

        pagamentos.firstOrNull {
            it.status == PagamentoStatus.PENDENTE ||
                    it.status == PagamentoStatus.PARCIAL
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    // Obter pagamento por ID
    suspend fun getPagamentoById(id: String): Pagamento? = try {
        collection.document(id).get().await().toObject(Pagamento::class.java)
    } catch (e: Exception) {
        null
    }

    // Adicionar pagamento parcial
    suspend fun adicionarPagamentoParcial(
        pagamentoId: String,
        valor: Double,
        formaPagamento: FormaPagamento
    ): Result<Unit> = try {
        val pagamento = getPagamentoById(pagamentoId) ?: throw Exception("Pagamento não encontrado")
        val novoPagamento = PagamentoParcial(valor = valor, formaPagamento = formaPagamento)
        val pagamentosParciais = pagamento.pagamentos + novoPagamento
        val valorPago = pagamentosParciais.sumOf { it.valor }
        
        val novoStatus = when {
            valorPago >= pagamento.valorTotal -> PagamentoStatus.COMPLETO
            valorPago > 0 -> PagamentoStatus.PARCIAL
            else -> PagamentoStatus.PENDENTE
        }

        val troco = if (valorPago > pagamento.valorTotal) valorPago - pagamento.valorTotal else 0.0

        val pagamentoAtualizado = pagamento.copy(
            pagamentos = pagamentosParciais,
            status = novoStatus,
            troco = troco
        )

        updatePagamento(pagamentoAtualizado)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Adicionar troco
    suspend fun adicionarTroco(
        pagamentoId: String,
        valor: Double,
        formaPagamento: FormaPagamento
    ): Result<Unit> = try {
        val pagamento = getPagamentoById(pagamentoId) ?: throw Exception("Pagamento não encontrado")
        val novoPagamento = PagamentoParcial(valor = valor, formaPagamento = formaPagamento)
        val pagamentosParciais = pagamento.pagamentos + novoPagamento
        val valorPago = pagamentosParciais.sumOf { it.valor }
        val troco = valorPago - pagamento.valorTotal

        val pagamentoAtualizado = pagamento.copy(
            pagamentos = pagamentosParciais,
            status = PagamentoStatus.COMPLETO,
            troco = if (troco > 0) troco else 0.0,
            closedAt = System.currentTimeMillis()
        )

        updatePagamento(pagamentoAtualizado)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Atualizar pagamento
    suspend fun updatePagamentoTotal(
        pagamentoId: String,
        valorTotal: Double
    ) {
        FirebaseFirestore.getInstance()
            .collection("pagamentos")
            .document(pagamentoId)
            .update("valorTotal", valorTotal)
            .await()
    }

    suspend fun updatePagamento(pagamento: Pagamento): Result<Unit> {
        return try {
            collection
                .document(pagamento.id)
                .set(pagamento, com.google.firebase.firestore.SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancelar pagamento
    suspend fun cancelarPagamento(pagamentoId: String): Result<Unit> = try {
        val pagamento = getPagamentoById(pagamentoId) ?: throw Exception("Pagamento não encontrado")
        val pagamentoAtualizado = pagamento.copy(status = PagamentoStatus.CANCELADO)
        updatePagamento(pagamentoAtualizado)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Deletar pagamento
    suspend fun deletePagamento(pagamentoId: String): Result<Unit> = try {
        collection.document(pagamentoId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

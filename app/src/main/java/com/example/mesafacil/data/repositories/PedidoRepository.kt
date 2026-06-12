package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.ItemPedido
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.PedidoStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class PedidoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pedidos")

    // Listar pedidos da mesa em tempo real
    fun getPedidosByMesa(mesaId: String): Flow<List<Pedido>> = flow {
        try {
            collection
                .whereEqualTo("mesaId", mesaId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val pedidos = snapshot?.toObjects(Pedido::class.java) ?: emptyList()
                    try {
                        emit(pedidos.sortedByDescending { it.createdAt })
                    } catch (e: Exception) {
                        // Emit error
                    }
                }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Obter pedido por ID
    suspend fun getPedidoById(id: String): Pedido? = try {
        collection.document(id).get().await().toObject(Pedido::class.java)
    } catch (e: Exception) {
        null
    }

    // Criar novo pedido
    suspend fun createPedido(
        mesaId: String,
        numeroMesa: Int,
        itens: List<ItemPedido>,
        observacoes: String,
        garcomId: String,
        garcomNome: String
    ): Result<Pedido> = try {
        val valorTotal = itens.sumOf { it.valorTotal() }
        val pedido = Pedido(
            mesaId = mesaId,
            numeroMesa = numeroMesa,
            itens = itens,
            status = PedidoStatus.NOVO,
            valorTotal = valorTotal,
            observacoes = observacoes,
            garcomId = garcomId,
            garcomNome = garcomNome,
            createdAt = System.currentTimeMillis()
        )
        val doc = collection.add(pedido).await()
        Result.success(pedido.copy(id = doc.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Atualizar pedido
    suspend fun updatePedido(pedido: Pedido): Result<Unit> = try {
        collection.document(pedido.id).set(
            pedido.copy(updatedAt = System.currentTimeMillis())
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Atualizar status do pedido
    suspend fun updatePedidoStatus(pedidoId: String, status: PedidoStatus): Result<Unit> = try {
        collection.document(pedidoId).update(
            "status", status,
            "updatedAt", System.currentTimeMillis()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Adicionar item ao pedido
    suspend fun adicionarItem(pedidoId: String, item: ItemPedido): Result<Unit> = try {
        val pedido = getPedidoById(pedidoId) ?: throw Exception("Pedido não encontrado")
        val novosPedidos = pedido.itens + item
        val novoValorTotal = novosPedidos.sumOf { it.valorTotal() }
        val pedidoAtualizado = pedido.copy(
            itens = novosPedidos,
            valorTotal = novoValorTotal
        )
        updatePedido(pedidoAtualizado)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Remover item do pedido
    suspend fun removerItem(pedidoId: String, itemId: String): Result<Unit> = try {
        val pedido = getPedidoById(pedidoId) ?: throw Exception("Pedido não encontrado")
        val novosPedidos = pedido.itens.filter { it.id != itemId }
        val novoValorTotal = novosPedidos.sumOf { it.valorTotal() }
        val pedidoAtualizado = pedido.copy(
            itens = novosPedidos,
            valorTotal = novoValorTotal
        )
        updatePedido(pedidoAtualizado)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Deletar pedido
    suspend fun deletePedido(pedidoId: String): Result<Unit> = try {
        collection.document(pedidoId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

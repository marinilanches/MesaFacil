package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.PedidoStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PedidoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("pedidos")

    suspend fun createPedido(pedido: Pedido) {
        collection.add(pedido).await()
    }

    suspend fun salvarPedido(pedido: Pedido) {
        createPedido(pedido)
    }

    // READ
    fun getPedidosByMesa(mesaId: String): Flow<List<Pedido>> = callbackFlow {
        val listener = collection
            .whereEqualTo("mesaId", mesaId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pedidos =
                    snapshot?.toObjects(Pedido::class.java)
                        ?: emptyList()

                trySend(pedidos)
            }

        awaitClose {
            listener.remove()
        }
    }

    // UPDATE
    suspend fun updatePedidoStatus(
        pedidoId: String,
        status: PedidoStatus
    ) {
        collection.document(pedidoId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    // DELETE
    suspend fun deletePedido(pedidoId: String) {
        collection.document(pedidoId)
            .delete()
            .await()
    }
}
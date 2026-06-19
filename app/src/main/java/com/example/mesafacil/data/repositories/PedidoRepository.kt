package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.StatusPedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class PedidoRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pedidos")

    suspend fun createPedido(pedido: Pedido) {
        collection.add(pedido).await()
    }

    suspend fun updatePedidoStatus(pedidoId: String, status: StatusPedido) {
        collection.document(pedidoId)
            .update("status", status.name)
            .await()
    }

    suspend fun deletePedido(pedidoId: String) {
        collection.document(pedidoId).delete().await()
    }

    suspend fun getPedidosByMesaOnce(mesaId: String): List<Pedido> {
        return try {
            val snapshot = firestore
                .collection("pedidos")
                .whereEqualTo("mesaId", mesaId)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(Pedido::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPedidosByMesa(mesaId: String): Flow<List<Pedido>> = callbackFlow {

        val listener = collection
            .whereEqualTo("mesaId", mesaId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pedidos = snapshot?.toObjects(Pedido::class.java) ?: emptyList()
                trySend(pedidos)
            }

        awaitClose { listener.remove() }
    }
}
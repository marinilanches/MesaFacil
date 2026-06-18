package com.example.mesafacil.data.repositories

import android.util.Log
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.StatusPedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PedidoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("pedidos")

    suspend fun createPedido(pedido: Pedido): String {
        val docRef = collection.document()
        val pedidoComId = pedido.copy(id = docRef.id)

        docRef.set(pedidoComId).await()

        return docRef.id

        Log.d("PEDIDO", "Criando pedido...")
        Log.d("PEDIDO", "ID gerado: ${docRef.id}")
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

                val pedidos = snapshot
                    ?.toObjects(Pedido::class.java)
                    ?: emptyList()

                trySend(pedidos)
            }

        awaitClose { listener.remove() }
    }

    // UPDATE
    suspend fun updatePedidoStatus(
        pedidoId: String,
        status: StatusPedido
    ) {
        collection.document(pedidoId)
            .update(
                mapOf(
                    "status" to status.name,
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
package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.MesaStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MesaRepository {

    private val collection = FirebaseFirestore.getInstance().collection("mesas")

    suspend fun atualizarMesa(
        mesaId: String,
        status: String,
        pessoas: Int
    ) {
        collection.document(mesaId).update(
            mapOf(
                "status" to MesaStatus.OCUPADA.name,
                "quantidadePessoas" to pessoas
            )
        ).await()
    }

    suspend fun abrirMesa(
        mesaId: String,
        pessoas: Int,
        garcomId: String,
        garcomNome: String
    ) {
        collection.document(mesaId).update(
            mapOf(
                "status" to MesaStatus.OCUPADA.name,
                "quantidadePessoas" to pessoas,
                "garcomId" to garcomId,
                "garcomNome" to garcomNome
            )
        ).await()
    }

    suspend fun fecharMesa(mesaId: String) {
        collection.document(mesaId).update(
            mapOf(
                "status" to "LIVRE",
                "quantidadePessoas" to 0,
                "valorTotal" to 0.0
            )
        ).await()
    }

    suspend fun unirMesas(
        mesasIds: List<String>,
        garcomId: String,
        garcomNome: String
    ) {
        val batch = FirebaseFirestore.getInstance().batch()

        mesasIds.forEach { id ->
            val ref = collection.document(id)

            batch.update(
                ref,
                mapOf(
                    "status" to MesaStatus.OCUPADA.name,
                    "garcomId" to garcomId,
                    "garcomNome" to garcomNome
                )
            )
        }

        batch.commit().await()
    }

    fun getAllMesas(): Flow<List<Mesa>> = callbackFlow {

        val listener = collection
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val mesas = snapshot
                    ?.toObjects(Mesa::class.java)
                    ?: emptyList()

                trySend(mesas)
            }

        awaitClose { listener.remove() }
    }
}
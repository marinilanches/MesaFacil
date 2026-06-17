package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.MesaStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MesaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("mesas")

    fun getAllMesas(): Flow<List<Mesa>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val mesas = snapshot?.toObjects(Mesa::class.java) ?: emptyList()
            trySend(mesas)
        }

        awaitClose { listener.remove() }
    }

    suspend fun abrirMesa(id: String, pessoas: Int) {
        collection.document(id).update(
            mapOf(
                "status" to MesaStatus.OCUPADA,
                "quantidadePessoas" to pessoas
            )
        ).await()
    }

    suspend fun fecharMesa(id: String) {
        collection.document(id).update(
            mapOf(
                "status" to MesaStatus.LIVRE,
                "quantidadePessoas" to 0,
                "valorTotal" to 0.0
            )
        ).await()
    }

    suspend fun unirMesas(
        mesaIds: List<String>,
        garcomId: String,
        garcomNome: String
    ) {
        if (mesaIds.isEmpty()) return

        val batch = db.batch()

        mesaIds.forEach { mesaId ->
            val docRef = collection.document(mesaId)

            batch.update(
                docRef,
                mapOf(
                    "status" to MesaStatus.OCUPADA,
                    "garcomId" to garcomId,
                    "garcomNome" to garcomNome,
                    "mesaPrincipal" to mesaIds.first(),
                    "mesasUnidas" to mesaIds
                )
            )
        }

        batch.commit().await()
    }
}
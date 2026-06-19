package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.MesaStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MesaRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("mesas")

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
                "quantidadePessoas" to 0
            )
        ).await()
    }

    suspend fun liberarGrupoMesa(mesa: Mesa) {
        val batch = firestore.batch()

        val ids = mesa.mesasUnidas + mesa.id

        ids.forEach { id ->
            val ref = collection.document(id)

            batch.update(
                ref,
                mapOf(
                    "status" to MesaStatus.LIVRE.name,
                    "quantidadePessoas" to 0,
                    "garcomId" to "",
                    "garcomNome" to "",
                    "mesasUnidas" to emptyList<String>()
                )
            )
        }

        batch.commit().await()
    }

    suspend fun getMesaById(mesaId: String): Mesa? {
        return try {
            collection.document(mesaId)
                .get()
                .await()
                .toObject(Mesa::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun unirMesas(
        mesasIds: List<String>,
        garcomId: String,
        garcomNome: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val grupoId = UUID.randomUUID().toString()

        val batch = db.batch()

        mesasIds.forEach { mesaId ->

            val ref = db.collection("mesas").document(mesaId)

            val updates = mapOf(
                "status" to "OCUPADA",
                "grupoId" to grupoId,
                "garcomId" to garcomId,
                "garcomNome" to garcomNome
            )

            batch.set(ref, updates, SetOptions.merge())
        }

        batch.commit().await()
    }

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
}
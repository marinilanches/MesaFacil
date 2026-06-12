package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.MesaStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class MesaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("mesas")

    // Listar todas as mesas em tempo real
    fun getAllMesas(): Flow<List<Mesa>> = flow {
        try {
            collection.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val mesas = snapshot?.toObjects(Mesa::class.java) ?: emptyList()
                try {
                    emit(mesas.sortedBy { it.numero })
                } catch (e: Exception) {
                    // Emit error
                }
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Obter mesa por ID
    suspend fun getMesaById(id: String): Mesa? = try {
        collection.document(id).get().await().toObject(Mesa::class.java)
    } catch (e: Exception) {
        null
    }

    // Criar nova mesa
    suspend fun createMesa(numero: Int): Result<Mesa> = try {
        val mesa = Mesa(
            numero = numero,
            status = MesaStatus.LIVRE,
            createdAt = System.currentTimeMillis()
        )
        val doc = collection.add(mesa).await()
        val createdMesa = mesa.copy(id = doc.id)
        Result.success(createdMesa)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Atualizar mesa
    suspend fun updateMesa(mesa: Mesa): Result<Unit> = try {
        collection.document(mesa.id).set(mesa.copy(updatedAt = System.currentTimeMillis())).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Abrir mesa
    suspend fun abrirMesa(mesaId: String, quantidadePessoas: Int, garcomId: String, garcomNome: String): Result<Unit> = try {
        val mesa = getMesaById(mesaId) ?: throw Exception("Mesa não encontrada")
        val updatedMesa = mesa.copy(
            status = MesaStatus.OCUPADA,
            quantidadePessoas = quantidadePessoas,
            garcomId = garcomId,
            garcomNome = garcomNome
        )
        updateMesa(updatedMesa)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Fechar mesa
    suspend fun fecharMesa(mesaId: String): Result<Unit> = try {
        val mesa = getMesaById(mesaId) ?: throw Exception("Mesa não encontrada")
        val updatedMesa = mesa.copy(
            status = MesaStatus.LIVRE,
            quantidadePessoas = 0,
            valorTotal = 0.0,
            garcomId = "",
            garcomNome = "",
            mesasUnidas = emptyList()
        )
        updateMesa(updatedMesa)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Unir mesas
    suspend fun unirMesas(mesaIds: List<String>, garcomId: String, garcomNome: String): Result<Mesa> = try {
        // Buscar todas as mesas
        val mesas = mesaIds.mapNotNull { getMesaById(it) }
        if (mesas.isEmpty()) throw Exception("Nenhuma mesa encontrada")

        // Calcular totais
        val totalPessoas = mesas.sumOf { it.quantidadePessoas }
        val totalValor = mesas.sumOf { it.valorTotal }
        val numerosUnidos = mesas.map { it.numero }

        // Usar a primeira mesa como principal
        val mesaPrincipal = mesas[0].copy(
            status = MesaStatus.OCUPADA,
            quantidadePessoas = totalPessoas,
            valorTotal = totalValor,
            garcomId = garcomId,
            garcomNome = garcomNome,
            mesasUnidas = numerosUnidos
        )

        // Atualizar mesa principal
        updateMesa(mesaPrincipal)

        // Fechar as outras mesas
        for (i in 1 until mesas.size) {
            fecharMesa(mesas[i].id)
        }

        Result.success(mesaPrincipal)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Deletar mesa
    suspend fun deleteMesa(mesaId: String): Result<Unit> = try {
        collection.document(mesaId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

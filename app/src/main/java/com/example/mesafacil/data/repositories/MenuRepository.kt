package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Adicional
import com.example.mesafacil.data.models.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

class MenuRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val menuCollection = firestore.collection("menu")
    private val adicionaisCollection = firestore.collection("adicionais")

    // Listar itens do menu por categoria
    suspend fun getMenu(): List<MenuItem> {
        return menuCollection.get().await().toObjects(MenuItem::class.java)
    }

    // Listar todos os adicionais
    fun getAllAdicionais(): Flow<List<Adicional>> = callbackFlow {
        val listener = adicionaisCollection
            .whereEqualTo("disponivel", true)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val adicionais = snapshot?.documents?.mapNotNull {
                    it.toObject(Adicional::class.java)
                } ?: emptyList()

                trySend(adicionais.sortedBy { it.nome })
            }

        awaitClose {
            listener.remove()
        }
    }

    // Obter item do menu por ID
    suspend fun getMenuItemById(id: String): MenuItem? = try {
        menuCollection.document(id).get().await().toObject(MenuItem::class.java)
    } catch (e: Exception) {
        null
    }

    // Obter adicional por ID
    suspend fun getAdicionalById(id: String): Adicional? = try {
        adicionaisCollection.document(id).get().await().toObject(Adicional::class.java)
    } catch (e: Exception) {
        null
    }
}

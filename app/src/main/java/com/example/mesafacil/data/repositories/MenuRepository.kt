package com.example.mesafacil.data.repositories

import com.example.mesafacil.data.models.Adicional
import com.example.mesafacil.data.models.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class MenuRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val menuCollection = firestore.collection("menu")
    private val adicionaisCollection = firestore.collection("adicionais")

    // Listar itens do menu por categoria
    fun getMenuByCategoria(categoria: String): Flow<List<MenuItem>> = flow {
        try {
            menuCollection
                .whereEqualTo("categoria", categoria)
                .whereEqualTo("disponivel", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val items = snapshot?.toObjects(MenuItem::class.java) ?: emptyList()
                    try {
                        emit(items.sortedBy { it.nome })
                    } catch (e: Exception) {
                        // Emit error
                    }
                }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Listar todos os adicionais
    fun getAllAdicionais(): Flow<List<Adicional>> = flow {
        try {
            adicionaisCollection
                .whereEqualTo("disponivel", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val adicionais = snapshot?.toObjects(Adicional::class.java) ?: emptyList()
                    try {
                        emit(adicionais.sortedBy { it.nome })
                    } catch (e: Exception) {
                        // Emit error
                    }
                }
        } catch (e: Exception) {
            emit(emptyList())
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

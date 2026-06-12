package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "waiter", // waiter, admin
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

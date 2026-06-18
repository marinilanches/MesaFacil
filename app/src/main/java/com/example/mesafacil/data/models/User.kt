package com.example.mesafacil.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp
import java.io.Serializable

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "waiter",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) : Serializable
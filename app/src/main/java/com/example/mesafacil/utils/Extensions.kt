package com.example.mesafacil.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Long.formatarData(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale("pt", "BR"))
    return sdf.format(Date(this))
}

fun Long.formatarHora(): String = formatarData("HH:mm")

fun Double.formatarMoeda(): String = "R$ ${String.format("%.2f", this)}"

fun String.isEmailValid(): Boolean {
    return this.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

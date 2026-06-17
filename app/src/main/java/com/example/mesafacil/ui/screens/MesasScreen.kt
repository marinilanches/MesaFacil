package com.example.mesafacil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.MesaStatus

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MesasScreen(
    mesas: List<Mesa>,
    onMesaClick: (Mesa) -> Unit,
    onAbrirMesa: (mesa: Mesa, pessoas: Int) -> Unit,
    onUnirMesas: (List<Mesa>) -> Unit,
    onLogout: () -> Unit
) {
    var selectedMesas by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var showAbrirMesaDialog by remember { mutableStateOf(false) }
    var mesaSelecionada by remember { mutableStateOf<Mesa?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🍽️ Mesas") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onUnirMesas(selectedMesas) },
                    modifier = Modifier.weight(1f),
                    enabled = selectedMesas.size >= 2
                ) {
                    Icon(Icons.Filled.CallMerge, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unir (${selectedMesas.size})")
                }

                if (selectedMesas.isNotEmpty()) {
                    Button(
                        onClick = { selectedMesas = emptyList() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpar")
                    }
                }
            }

            // Mesas Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mesas) { mesa ->
                    MesaCard(
                        mesa = mesa,
                        isSelected = selectedMesas.contains(mesa),
                        onCardClick = {
                            if (mesa.status == MesaStatus.LIVRE) {
                                mesaSelecionada = mesa
                                showAbrirMesaDialog = true
                            } else {
                                onMesaClick(mesa)
                            }
                        },
                        onLongClick = {
                            if (mesa.status == MesaStatus.LIVRE) {
                                selectedMesas = if (selectedMesas.contains(mesa)) {
                                    selectedMesas - mesa
                                } else {
                                    selectedMesas + mesa
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Abrir Mesa Dialog
    if (showAbrirMesaDialog && mesaSelecionada != null) {
        AbrirMesaDialog(
            mesa = mesaSelecionada!!,
            onDismiss = { showAbrirMesaDialog = false },
            onConfirm = { pessoas ->
                onAbrirMesa(mesaSelecionada!!, pessoas)
                showAbrirMesaDialog = false
                mesaSelecionada = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MesaCard(
    mesa: Mesa,
    isSelected: Boolean = false,
    onCardClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.tertiaryContainer
        mesa.status == MesaStatus.LIVRE -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val statusText = when (mesa.status) {
        MesaStatus.LIVRE -> "🟢 Livre"
        MesaStatus.OCUPADA -> "🔴 Ocupada"
        MesaStatus.RESERVADA -> "🟡 Reservada"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onCardClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mesa ${mesa.numero}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {

                if (mesa.quantidadePessoas > 0) {
                    Text(
                        text = "👥 ${mesa.quantidadePessoas}",
                        fontSize = 14.sp
                    )
                }

                if (mesa.valorTotal > 0) {
                    Text(
                        text = "R$ ${String.format("%.2f", mesa.valorTotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbrirMesaDialog(
    mesa: Mesa,
    onDismiss: () -> Unit,
    onConfirm: (pessoas: Int) -> Unit
) {
    var pessoas by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abrir Mesa ${mesa.numero}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quantas pessoas?")
                OutlinedTextField(
                    value = pessoas,
                    onValueChange = { pessoas = it.filter { c -> c.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pessoas.toIntOrNull() ?: 2) }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

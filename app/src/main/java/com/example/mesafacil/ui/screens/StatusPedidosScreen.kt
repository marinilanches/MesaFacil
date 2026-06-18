package com.example.mesafacil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.StatusPedido
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPedidosScreen(
    numeroMesa: Int,
    pedidos: List<Pedido>,
    onUpdateStatus: (pedidoId: String, status: StatusPedido) -> Unit,
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedidos - Mesa $numeroMesa") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pedidos) { pedido ->
                PedidoCard(
                    pedido = pedido,
                    onStatusChange = { onUpdateStatus(pedido.id, it) }
                )
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    onStatusChange: (StatusPedido) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido ${pedido.id.take(8)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date(pedido.createdAt)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "R$ ${String.format("%.2f", pedido.valorTotal)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider()

            // Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                pedido.itens.forEach { item ->
                    Text(
                        text = "${item.quantidade}x ${item.nome}",
                        fontSize = 13.sp
                    )
                    if (item.adicionais.isNotEmpty()) {
                        Text(
                            text = item.adicionais.joinToString(", "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (pedido.observacoes.isNotEmpty()) {
                Divider()
                Text(
                    text = "Obs: ${pedido.observacoes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Status Buttons
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusPedido.values().forEach { status ->
                    Button(
                        onClick = { onStatusChange(status) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pedido.status == status)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            when (status) {
                                StatusPedido.NOVO -> "Novo"
                                StatusPedido.EM_PREPARO -> "Prep"
                                StatusPedido.PRONTO -> "Pronto"
                                StatusPedido.ENTREGUE -> "Entregue"
                                StatusPedido.CANCELADO -> "Cancel"
                            },
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
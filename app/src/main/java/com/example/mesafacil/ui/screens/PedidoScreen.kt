package com.example.mesafacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesafacil.data.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(
    mesa: Mesa,
    pedidos: List<Pedido>,
    menuItems: List<MenuItem>,
    adicionais: List<Adicional>,
    onAdicionarItem: (ItemPedido) -> Unit,
    onEnviarPedido: (List<ItemPedido>, String) -> Unit,
    onVoltar: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Lanches") }
    var itensSelecionados by remember { mutableStateOf(listOf<ItemPedido>()) }
    var observacoes by remember { mutableStateOf("") }
    var itemSelecionado by remember { mutableStateOf<MenuItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesa ${mesa.numero}") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // MENU
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {

                val categories = listOf("Lanches", "Bebidas", "Sobremesas")

                LazyColumn {
                    items(categories) { cat ->
                        Button(
                            onClick = { selectedCategory = cat },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat)
                        }
                    }
                }

                LazyColumn {
                    items(menuItems.filter { it.categoria == selectedCategory }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .clickable {
                                    itemSelecionado = item
                                    showDialog = true
                                }
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text(item.nome, fontWeight = FontWeight.Bold)
                                Text("R$ ${item.valor}")
                            }
                        }
                    }
                }
            }

            // CARRINHO
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {

                Text("Carrinho", fontWeight = FontWeight.Bold)

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(itensSelecionados) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(item.nome)
                                    Text("Qtd: ${item.quantidade}")
                                }

                                Text("R$ ${item.valorUnitario * item.quantidade}")
                            }
                        }
                    }
                }

                Text(
                    "Total: R$ ${
                        itensSelecionados.sumOf { it.valorUnitario * it.quantidade }
                    }",
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Observações") }
                )

                Row {
                    Button(
                        onClick = { itensSelecionados = emptyList() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpar")
                    }

                    Button(
                        onClick = {
                            onEnviarPedido(itensSelecionados, observacoes)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = itensSelecionados.isNotEmpty()
                    ) {
                        Text("Enviar")
                    }
                }
            }
        }
    }

    // DIALOG
    if (showDialog && itemSelecionado != null) {

        var qtd by remember { mutableStateOf(1) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(itemSelecionado!!.nome) },
            text = {
                Column {
                    Text("Quantidade")

                    Row {
                        Button(onClick = { if (qtd > 1) qtd-- }) { Text("-") }
                        Text("$qtd", modifier = Modifier.padding(16.dp))
                        Button(onClick = { qtd++ }) { Text("+") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val item = ItemPedido(
                        id = itemSelecionado!!.id,
                        nome = itemSelecionado!!.nome,
                        categoria = itemSelecionado!!.categoria,
                        quantidade = qtd,
                        valorUnitario = itemSelecionado!!.valor
                    )

                    itensSelecionados = itensSelecionados + item
                    showDialog = false
                }) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.nome,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "R$ ${String.format("%.2f", item.valor)}",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ItemCarrinho(
    item: ItemPedido,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.quantidade}x ${item.nome}",
                    fontWeight = FontWeight.Bold
                )
                if (item.adicionais.isNotEmpty()) {
                    Text(
                        text = item.adicionais.joinToString(", "),
                        fontSize = 12.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "R$ ${String.format("%.2f", item.valorTotal())}",
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remover")
                }
            }
        }
    }
}

@Composable
fun AdicionaisDialog(
    item: MenuItem,
    adicionais: List<Adicional>,
    onDismiss: () -> Unit,
    onConfirm: (adicionais: List<Adicional>, observacao: String) -> Unit
) {
    var selectedAdicionais by remember { mutableStateOf<List<Adicional>>(emptyList()) }
    var observacao by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.nome) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionais")
                LazyColumn {
                    items(adicionais) { adicional ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedAdicionais.contains(adicional),
                                onCheckedChange = {
                                    selectedAdicionais = if (it) {
                                        selectedAdicionais + adicional
                                    } else {
                                        selectedAdicionais - adicional
                                    }
                                }
                            )
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(adicional.nome)
                                Text(
                                    text = "R$ ${String.format("%.2f", adicional.valor)}",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Observações")
                OutlinedTextField(
                    value = observacao,
                    onValueChange = { observacao = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedAdicionais, observacao) }
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

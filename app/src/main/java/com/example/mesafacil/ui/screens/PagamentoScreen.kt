package com.example.mesafacil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesafacil.data.models.FormaPagamento
import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.data.models.PagamentoParcial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagamentoScreen(
    pagamento: Pagamento,
    onAdicionarPagamento: (valor: Double, forma: FormaPagamento) -> Unit,
    onFecharMesa: () -> Unit,
    onVoltar: () -> Unit
) {
    var valor by remember { mutableStateOf("") }
    var formaSelecionada by remember { mutableStateOf(FormaPagamento.DINHEIRO) }
    var showTrocoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagamento - Mesa ${pagamento.numeroMesa}") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
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
            // Total
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL DA CONTA", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = "R$ ${String.format("%.2f", pagamento.valorTotal)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${pagamento.quantidadePessoas} pessoas | Valor/pessoa: R$ ${String.format("%.2f", pagamento.valorPorPessoa())}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pago:")
                        Text(
                            text = "R$ ${String.format("%.2f", pagamento.valorPago())}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Restante:")
                        Text(
                            text = "R$ ${String.format("%.2f", pagamento.valorRestante())}",
                            fontWeight = FontWeight.Bold,
                            color = if (pagamento.valorRestante() <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    if (pagamento.troco > 0) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Troco:")
                            Text(
                                text = "R$ ${String.format("%.2f", pagamento.troco)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Histórico de pagamentos
            if (pagamento.pagamentos.isNotEmpty()) {
                Text(
                    text = "Histórico de Pagamentos",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(pagamento.pagamentos) { p ->
                        PagamentoParcialCard(p)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Adicionar pagamento
            if (pagamento.valorRestante() > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    // Forma de pagamento
                    Text("Forma de Pagamento:")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FormaPagamento.values().forEach { forma ->
                            Button(
                                onClick = { formaSelecionada = forma },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (formaSelecionada == forma)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = when (forma) {
                                        FormaPagamento.DINHEIRO -> "💰"
                                        FormaPagamento.PIX -> "📱"
                                        FormaPagamento.CARTAO_DEBITO -> "💳"
                                        FormaPagamento.CARTAO_CREDITO -> "💳"
                                    },
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Valor
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Valor (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pagamento.valorRestante() > 0) {
                    Button(
                        onClick = {
                            val valorDouble = valor.toDoubleOrNull() ?: 0.0
                            if (valorDouble > 0) {
                                if (valorDouble > pagamento.valorRestante()) {
                                    showTrocoDialog = true
                                } else {
                                    onAdicionarPagamento(valorDouble, formaSelecionada)
                                    valor = ""
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = valor.toDoubleOrNull() ?: 0.0 > 0
                    ) {
                        Text("Adicionar")
                    }
                }

                Button(
                    onClick = onFecharMesa,
                    modifier = Modifier.weight(1f),
                    enabled = pagamento.valorRestante() <= 0
                ) {
                    Text("Fechar Mesa")
                }
            }
        }
    }

    // Dialog de Troco
    if (showTrocoDialog) {
        AlertDialog(
            onDismissRequest = { showTrocoDialog = false },
            title = { Text("Registrar Troco") },
            text = {
                val valorDigitado = valor.toDoubleOrNull() ?: 0.0
                val troco = valorDigitado - pagamento.valorRestante()
                Column {
                    Text("Valor recebido: R$ ${String.format("%.2f", valorDigitado)}")
                    Text("Valor a pagar: R$ ${String.format("%.2f", pagamento.valorRestante())}")
                    Text(
                        text = "Troco: R$ ${String.format("%.2f", troco)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valorDouble = valor.toDoubleOrNull() ?: 0.0
                        onAdicionarPagamento(valorDouble, formaSelecionada)
                        valor = ""
                        showTrocoDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrocoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PagamentoParcialCard(pagamento: PagamentoParcial) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (pagamento.formaPagamento) {
                        FormaPagamento.DINHEIRO -> "💰 Dinheiro"
                        FormaPagamento.PIX -> "📱 PIX"
                        FormaPagamento.CARTAO_DEBITO -> "💳 Débito"
                        FormaPagamento.CARTAO_CREDITO -> "💳 Crédito"
                    },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm").format(pagamento.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "R$ ${String.format("%.2f", pagamento.valor)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

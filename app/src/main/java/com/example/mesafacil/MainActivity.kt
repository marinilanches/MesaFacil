package com.example.mesafacil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.mesafacil.data.models.Adicional
import com.example.mesafacil.data.models.ItemPedido
import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.MenuItem
import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.FormaPagamento
import com.example.mesafacil.ui.screens.*
import com.example.mesafacil.ui.theme.MesaFacilTheme
import com.example.mesafacil.ui.viewmodels.*
import com.example.mesafacil.utils.FirebaseInitializer
import com.example.mesafacil.utils.LoggerUtil
import com.example.mesafacil.utils.PrinterManager
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(this).get(AuthViewModel::class.java)
    }

    private val mesaViewModel: MesaViewModel by lazy {
        ViewModelProvider(this).get(MesaViewModel::class.java)
    }

    private val pedidoViewModel: PedidoViewModel by lazy {
        ViewModelProvider(this).get(PedidoViewModel::class.java)
    }

    private val pagamentoViewModel: PagamentoViewModel by lazy {
        ViewModelProvider(this).get(PagamentoViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Firebase
        FirebaseInitializer.initialize(this)
        LoggerUtil.i("MainActivity criada")

        setContent {
            MesaFacilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(
                        authViewModel = authViewModel,
                        mesaViewModel = mesaViewModel,
                        pedidoViewModel = pedidoViewModel,
                        pagamentoViewModel = pagamentoViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    authViewModel: AuthViewModel,
    mesaViewModel: MesaViewModel,
    pedidoViewModel: PedidoViewModel,
    pagamentoViewModel: PagamentoViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var selectedMesa by remember { mutableStateOf<Mesa?>(null) }
    var selectedPagamento by remember { mutableStateOf<Pagamento?>(null) }

    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val mesas by mesaViewModel.mesas.collectAsState()
    val pedidos by pedidoViewModel.pedidos.collectAsState()
    val pagamento by pagamentoViewModel.pagamento.collectAsState()

    // Mock data para menu (em produção, viria do Firebase)
    val menuItems = remember {
        listOf(
            MenuItem(
                id = "1",
                nome = "X-Burger",
                categoria = "Lanches",
                valor = 25.00,
                disponivel = true
            ),
            MenuItem(
                id = "2",
                nome = "X-Tudo",
                categoria = "Lanches",
                valor = 35.00,
                disponivel = true
            ),
            MenuItem(
                id = "3",
                nome = "Coca-Cola",
                categoria = "Bebidas",
                valor = 8.00,
                disponivel = true
            ),
            MenuItem(
                id = "4",
                nome = "Suco Natural",
                categoria = "Bebidas",
                valor = 12.00,
                disponivel = true
            )
        )
    }

    val adicionais = remember {
        listOf(
            Adicional(
                id = "1",
                nome = "Bacon",
                valor = 5.00,
                disponivel = true
            ),
            Adicional(
                id = "2",
                nome = "Cheddar",
                valor = 3.00,
                disponivel = true
            ),
            Adicional(
                id = "3",
                nome = "Ovo",
                valor = 2.00,
                disponivel = true
            )
        )
    }

    when (authState) {
        is AuthState.Success -> {
            if (currentUser != null) {
                when (currentScreen) {
                    Screen.Login -> {
                        currentScreen = Screen.Mesas
                    }

                    Screen.Mesas -> {
                        MesasScreen(
                            mesas = mesas,
                            onMesaClick = { mesa ->
                                selectedMesa = mesa
                                pedidoViewModel.loadPedidosByMesa(mesa.id)
                                currentScreen = Screen.Opcoes
                            },
                            onAbrirMesa = { mesa, pessoas ->
                                mesaViewModel.abrirMesa(
                                    mesa.id,
                                    pessoas,
                                    currentUser!!.id,
                                    currentUser!!.name
                                )
                            },
                            onUnirMesas = { selectedMesas ->
                                if (selectedMesas.isNotEmpty()) {
                                    mesaViewModel.unirMesas(
                                        selectedMesas.map { it.id },
                                        currentUser!!.id,
                                        currentUser!!.name
                                    )
                                }
                            },
                            onLogout = {
                                authViewModel.logout()
                                currentScreen = Screen.Login
                            }
                        )
                    }

                    Screen.Opcoes -> {
                        if (selectedMesa != null) {
                            OpcoesMesaScreen(
                                mesa = selectedMesa!!,
                                onNovoPedido = {
                                    currentScreen = Screen.Pedido
                                },
                                onVerPedidos = {
                                    currentScreen = Screen.Status
                                },
                                onPagamento = {
                                    pagamentoViewModel.carregarPagamento(selectedMesa!!.id)
                                    currentScreen = Screen.Pagamento
                                },
                                onVoltar = {
                                    currentScreen = Screen.Mesas
                                    selectedMesa = null
                                }
                            )
                        }
                    }

                    Screen.Pedido -> {
                        if (selectedMesa != null) {
                            PedidoScreen(
                                mesa = selectedMesa!!,
                                pedidos = pedidos,
                                menuItems = menuItems,
                                adicionais = adicionais,
                                onAdicionarItem = { /* Item adicionado no UI */ },
                                onEnviarPedido = { itens, observacoes ->
                                    pedidoViewModel.criarPedido(
                                        selectedMesa!!.id,
                                        selectedMesa!!.numero,
                                        itens,
                                        observacoes,
                                        currentUser!!.id,
                                        currentUser!!.name
                                    )
                                    // Imprimir comanda
                                    if (itens.isNotEmpty()) {
                                        val pedido = Pedido(
                                            mesaId = selectedMesa!!.id,
                                            numeroMesa = selectedMesa!!.numero,
                                            itens = itens,
                                            observacoes = observacoes
                                        )
                                        PrinterManager.printComanda(pedido)
                                    }
                                    currentScreen = Screen.Opcoes
                                },
                                onVoltar = {
                                    currentScreen = Screen.Opcoes
                                }
                            )
                        }
                    }

                    Screen.Status -> {
                        if (selectedMesa != null) {
                            StatusPedidosScreen(
                                numeroMesa = selectedMesa!!.numero,
                                pedidos = pedidos,
                                onUpdateStatus = { pedidoId, status ->
                                    pedidoViewModel.atualizarStatusPedido(pedidoId, status)
                                },
                                onVoltar = {
                                    currentScreen = Screen.Opcoes
                                }
                            )
                        }
                    }

                    Screen.Pagamento -> {
                        if (selectedMesa != null && pagamento != null) {
                            PagamentoScreen(
                                pagamento = pagamento!!,
                                onAdicionarPagamento = { valor, forma ->
                                    pagamentoViewModel.adicionarPagamento(valor, forma)
                                },
                                onFecharMesa = {
                                    // Imprimir recibo
                                    PrinterManager.printRecibo(pagamento!!)
                                    // Fechar mesa
                                    mesaViewModel.fecharMesa(selectedMesa!!.id)
                                    currentScreen = Screen.Mesas
                                    selectedMesa = null
                                    selectedPagamento = null
                                },
                                onVoltar = {
                                    currentScreen = Screen.Opcoes
                                }
                            )
                        }
                    }
                }
            }
        }

        else -> {
            LoginScreen(
                onLoginSuccess = { currentScreen = Screen.Mesas },
                onLogin = { email, password ->
                    LoggerUtil.d("Tentando login com: $email")
                    authViewModel.login(email, password)
                },
                isLoading = authState is AuthState.Loading,
                errorMessage = (authState as? AuthState.Error)?.message
            )
        }
    }
}

/**
 * Tela intermediária para escolher ação na mesa
 */
@Composable
fun OpcoesMesaScreen(
    mesa: Mesa,
    onNovoPedido: () -> Unit,
    onVerPedidos: () -> Unit,
    onPagamento: () -> Unit,
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesa ${mesa.numero}") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mesa ${mesa.numero}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${mesa.quantidadePessoas} pessoas | R$ ${String.format("%.2f", mesa.valorTotal)}",
                        fontSize = 14.sp
                    )
                }
            }

            Button(
                onClick = onNovoPedido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Novo Pedido", fontSize = 16.sp)
            }

            Button(
                onClick = onVerPedidos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Pedidos", fontSize = 16.sp)
            }

            Button(
                onClick = onPagamento,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pagamento", fontSize = 16.sp)
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Mesas : Screen()
    object Opcoes : Screen()
    object Pedido : Screen()
    object Status : Screen()
    object Pagamento : Screen()
}

// Imports necessários
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

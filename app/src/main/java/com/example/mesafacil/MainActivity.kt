package com.example.mesafacil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mesafacil.data.models.*
import com.example.mesafacil.ui.screens.*
import com.example.mesafacil.ui.theme.MesaFacilTheme
import com.example.mesafacil.ui.viewmodels.*
import com.example.mesafacil.utils.FirebaseInitializer
import com.example.mesafacil.utils.LoggerUtil
import com.example.mesafacil.utils.PrinterManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val authViewModel by lazy {
        ViewModelProvider(this)[AuthViewModel::class.java]
    }

    private val mesaViewModel by lazy {
        ViewModelProvider(this)[MesaViewModel::class.java]
    }

    private val pedidoViewModel by lazy {
        ViewModelProvider(this)[PedidoViewModel::class.java]
    }

    private val pagamentoViewModel by lazy {
        ViewModelProvider(this)[PagamentoViewModel::class.java]
    }

    private val collection =FirebaseFirestore.getInstance().collection("mesas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        criarMesasIniciais()

        FirebaseFirestore.getInstance()
            .collection("teste")
            .add(mapOf("mensagem" to "Firebase OK"))

        FirebaseInitializer.initialize(this)
        LoggerUtil.i("MainActivity iniciada")

        setContent {
            MesaFacilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppContent(
                        authViewModel,
                        mesaViewModel,
                        pedidoViewModel,
                        pagamentoViewModel
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpcoesMesaScreen(
    mesa: Mesa,
    onNovoPedido: () -> Unit,
    onVerPedidos: () -> Unit,
    onPagamento: () -> Unit,
    onLiberarMesa: () -> Unit,
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesa ${mesa.id}") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Mesa ${mesa.id}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${mesa.quantidadePessoas} pessoas | R$ ${mesa.valorTotal}")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = onNovoPedido, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Novo Pedido")
            }

            Button(onClick = onVerPedidos, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Visibility, null)
                Spacer(Modifier.width(8.dp))
                Text("Ver Pedidos")
            }

            Button(onClick = onPagamento, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Payment, null)
                Spacer(Modifier.width(8.dp))
                Text("Pagamento")
            }

            Button(
                onClick = onLiberarMesa,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Liberar Mesa")
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

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val mesas by mesaViewModel.mesas.collectAsStateWithLifecycle()
    val pedidos by pedidoViewModel.pedidos.collectAsStateWithLifecycle()
    val pagamento by pagamentoViewModel.pagamento.collectAsStateWithLifecycle()

    val menuItems = remember {
        listOf(
            MenuItem("1", "X-Burger", "Lanches", valor = 25.0),
            MenuItem("2", "X-Tudo", "Lanches", valor = 35.0),
            MenuItem("3", "Coca-Cola", "Bebidas", valor = 8.0),
            MenuItem("4", "Suco", "Bebidas", valor = 12.0)
        )
    }

    val adicionais = remember {
        listOf(
            Adicional("1", "Bacon", 5.0),
            Adicional("2", "Cheddar", 3.0),
            Adicional("3", "Ovo", 2.0)
        )
    }

    BackHandler {

        when (currentScreen) {

            Screen.Login -> {
                // deixa o Android fechar o app
            }

            Screen.Mesas -> {
                currentScreen = Screen.Login
            }

            Screen.Opcoes -> {
                currentScreen = Screen.Mesas
                selectedMesa = null
            }

            Screen.Pedido -> {
                currentScreen = Screen.Opcoes
            }

            Screen.Status -> {
                currentScreen = Screen.Opcoes
            }

            Screen.Pagamento -> {
                currentScreen = Screen.Opcoes
            }
        }
    }

    when (authState) {

        is AuthState.Success -> {
            if (currentUser != null) {
                when (currentScreen) {

                    Screen.Login -> currentScreen = Screen.Mesas

                    Screen.Mesas -> MesasScreen(
                        mesas = mesas,
                        onMesaClick = {
                            selectedMesa = it
                            pedidoViewModel.loadPedidosByMesa(it.id)
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
                        onUnirMesas = { mesasSelecionadas ->
                            mesaViewModel.unirMesas(
                                mesasSelecionadas.map { it.id },
                                currentUser!!.id,
                                currentUser!!.name
                            )
                        },
                        onLogout = {
                            authViewModel.logout()
                            currentScreen = Screen.Login
                        }
                    )

                    Screen.Opcoes -> selectedMesa?.let { mesa ->
                        OpcoesMesaScreen(
                            mesa = mesa,
                            onNovoPedido = { currentScreen = Screen.Pedido },
                            onVerPedidos = { currentScreen = Screen.Status },
                            onPagamento = {
                                pagamentoViewModel.carregarPagamento(mesa.id)
                                currentScreen = Screen.Pagamento
                            },
                            onLiberarMesa = {
                                mesaViewModel.fecharMesa(mesa.id)
                                selectedMesa = null
                                currentScreen = Screen.Mesas
                            },
                            onVoltar = {
                                currentScreen = Screen.Mesas
                                selectedMesa = null
                            }
                        )
                    }

                    Screen.Pedido -> selectedMesa?.let { mesa ->
                        PedidoScreen(
                            mesa = mesa,
                            pedidos = pedidos,
                            menuItems = menuItems,
                            adicionais = adicionais,
                            onAdicionarItem = {},
                            onEnviarPedido = { itens, obs ->

                                println("ENVIANDO PEDIDO") // 🔥 AQUI

                                pedidoViewModel.criarPedido(
                                    mesaId = mesa.id,
                                    numeroMesa = mesa.numero,
                                    itens = itens,
                                    observacoes = obs,
                                    garcomId = currentUser!!.id,
                                    garcomNome = currentUser!!.name
                                )

                                currentScreen = Screen.Opcoes
                                println("PEDIDO ENVIADO PARA MESA: ${mesa.id}")
                            },
                            onVoltar = { currentScreen = Screen.Opcoes }
                        )
                    }

                    Screen.Status -> selectedMesa?.let {
                        StatusPedidosScreen(
                            numeroMesa = it.numero,
                            pedidos = pedidos,
                            onUpdateStatus = { id, status ->
                                pedidoViewModel.atualizarStatusPedido(id, status)
                            },
                            onVoltar = { currentScreen = Screen.Opcoes }
                        )
                    }

                    Screen.Pagamento -> selectedMesa?.let { mesa ->
                        pagamento?.let { pag ->
                            PagamentoScreen(
                                pagamento = pag,
                                onAdicionarPagamento = { v, f ->
                                    pagamentoViewModel.adicionarPagamento(v, f)
                                },
                                onFecharMesa = {
                                    PrinterManager.printRecibo(pag)
                                    mesaViewModel.liberarGrupoMesa(mesa)

                                    selectedMesa = null
                                    currentScreen = Screen.Mesas
                                },
                                onLiberarMesa = {
                                    mesaViewModel.liberarGrupoMesa(mesa)

                                    selectedMesa = null
                                    currentScreen = Screen.Mesas
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

        else -> LoginScreen(
            onLoginSuccess = { currentScreen = Screen.Mesas },
            onLogin = { email, pass -> authViewModel.login(email, pass) },
            isLoading = authState is AuthState.Loading,
            errorMessage = (authState as? AuthState.Error)?.message
        )
    }
}

fun criarMesasIniciais() {
    val db = FirebaseFirestore.getInstance()
    val collection = db.collection("mesas")

    val mesas = listOf(1, 2, 3, 4)

    collection.get().addOnSuccessListener { snapshot ->

        // 🔥 se já existe mesa, não cria de novo
        if (!snapshot.isEmpty) return@addOnSuccessListener

        mesas.forEach { numero ->

            val mesa = mapOf(
                "id" to numero.toString(),
                "numero" to numero,
                "status" to "LIVRE",
                "quantidadePessoas" to 0,
                "valorTotal" to 0.0
            )

            collection.document(numero.toString())
                .set(mesa)
        }
    }
}
package com.example.mesafacil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.ui.screens.*
import com.example.mesafacil.ui.viewmodels.*

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    mesaViewModel: MesaViewModel = viewModel(),
    pedidoViewModel: PedidoViewModel = viewModel(),
    pagamentoViewModel: PagamentoViewModel = viewModel(),
    currentScreen: String = "login",
    onNavigate: (String) -> Unit = {},
    selectedMesa: Mesa? = null
) {
    when (currentScreen) {
        "login" -> {
            LoginScreen(
                onLoginSuccess = { onNavigate("mesas") },
                onLogin = { email, password -> authViewModel.login(email, password) },
                isLoading = false,
                errorMessage = null
            )
        }
        "mesas" -> {
            // TODO: Implementar navegação para mesas
        }
        "pedidos" -> {
            if (selectedMesa != null) {
                // TODO: Implementar navegação para pedidos
            }
        }
        "pagamento" -> {
            if (selectedMesa != null) {
                // TODO: Implementar navegação para pagamento
            }
        }
    }
}

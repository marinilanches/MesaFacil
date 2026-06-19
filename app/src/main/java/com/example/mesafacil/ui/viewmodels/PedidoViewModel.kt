package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.ItemPedido
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.StatusPedido
import com.example.mesafacil.data.repositories.MesaRepository
import com.example.mesafacil.data.repositories.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PedidoViewModel : ViewModel() {

    private val pedidoRepository = PedidoRepository()

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var pedidosJob: kotlinx.coroutines.Job? = null

    fun loadPedidosByMesa(mesaId: String) {

        pedidosJob?.cancel()

        pedidosJob = viewModelScope.launch {
            pedidoRepository.getPedidosByMesa(mesaId)
                .collect { lista ->
                    _pedidos.value = lista
                }
        }
    }

    fun criarPedido(
        mesaId: String,
        numeroMesa: Int,
        itens: List<ItemPedido>,
        observacoes: String,
        garcomId: String,
        garcomNome: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val total = itens.sumOf { it.quantidade * it.valorUnitario }

                val totalMesa = _pedidos.map { lista ->
                    lista.sumOf { it.valorTotal }
                }.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    0.0
                )

                val pedido = Pedido(
                    mesaId = mesaId,
                    numeroMesa = numeroMesa,
                    itens = itens,
                    observacoes = observacoes,
                    garcomId = garcomId,
                    garcomNome = garcomNome,
                    valorTotal = total,
                    status = StatusPedido.NOVO
                )

                pedidoRepository.createPedido(pedido)

            } catch (e: Exception) {
                _error.value = e.message
            }

            _loading.value = false
        }
    }

    fun calcularTotal(pedidos: List<Pedido>): Double {
        return pedidos.sumOf { pedido ->
            pedido.itens.sumOf { it.quantidade * it.valorUnitario }
        }
    }

    fun atualizarStatusPedido(pedidoId: String, status: StatusPedido) {
        viewModelScope.launch {
            try {
                pedidoRepository.updatePedidoStatus(pedidoId, status)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ✅ COLOCA AQUI (FINAL DA CLASSE)
    suspend fun calcularTotalMesa(mesaId: String): Double {
        val pedidos = pedidoRepository.getPedidosByMesaOnce(mesaId)
        return pedidos.sumOf { it.valorTotal }
    }
}
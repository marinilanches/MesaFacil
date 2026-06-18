package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.ItemPedido
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.StatusPedido
import com.example.mesafacil.data.repositories.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
                .collectLatest { lista ->
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

    fun atualizarStatusPedido(
        pedidoId: String,
        status: StatusPedido
    ) {
        viewModelScope.launch {
            _error.value = null

            try {
                pedidoRepository.updatePedidoStatus(pedidoId, status)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deletarPedido(pedidoId: String) {
        viewModelScope.launch {
            _error.value = null

            try {
                pedidoRepository.deletePedido(pedidoId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

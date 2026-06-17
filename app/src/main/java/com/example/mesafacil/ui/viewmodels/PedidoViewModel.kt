package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.ItemPedido
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.models.PedidoStatus
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

    fun loadPedidosByMesa(mesaId: String) {
        viewModelScope.launch {
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
            val pedido = Pedido(
                mesaId = mesaId,
                numeroMesa = numeroMesa,
                itens = itens,
                observacoes = observacoes,
                garcomId = garcomId,
                garcomNome = garcomNome,
                valorTotal = itens.sumOf { it.valorTotal() }
            )

            try {
                pedidoRepository.createPedido(pedido)
            } catch (e: Exception) {
                _error.value = e.message
            }
            _loading.value = false
        }
    }

    fun atualizarStatusPedido(
        pedidoId: String,
        status: PedidoStatus
    ) {
        viewModelScope.launch {
            try {
                pedidoRepository.updatePedidoStatus(
                    pedidoId,
                    status
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deletarPedido(pedidoId: String) {
        viewModelScope.launch {
            try {
                pedidoRepository.deletePedido(pedidoId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

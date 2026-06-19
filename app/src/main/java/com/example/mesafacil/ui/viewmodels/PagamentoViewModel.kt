package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.FormaPagamento
import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.data.models.PagamentoStatus
import com.example.mesafacil.data.models.Pedido
import com.example.mesafacil.data.repositories.PagamentoRepository
import com.example.mesafacil.data.repositories.MesaRepository
import com.example.mesafacil.data.repositories.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PagamentoViewModel : ViewModel() {

    private val pagamentoRepository = PagamentoRepository()
    private val pedidoRepository = PedidoRepository()

    private val _pagamento = MutableStateFlow<Pagamento?>(null)
    private val mesaRepository = MesaRepository()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    val pagamento: StateFlow<Pagamento?> = _pagamento

    fun carregarOuCriarPagamento(
        mesaId: String,
        numeroMesa: Int,
        quantidadePessoas: Int,
        garcomId: String
    ) {
        viewModelScope.launch {

            val pedidos = pedidoRepository.getPedidosByMesaOnce(mesaId)
            val valorTotal = pedidos.sumOf { it.valorTotal }

            val existente = pagamentoRepository.getPagamentoByMesa(mesaId)

            if (existente != null) {

                val atualizado = existente.copy(
                    valorTotal = valorTotal
                )

                pagamentoRepository.updatePagamentoTotal(existente.id, valorTotal)

                _pagamento.value = atualizado

            } else {

                criarPagamento(
                    mesaId = mesaId,
                    numeroMesa = numeroMesa,
                    valorTotal = valorTotal,
                    quantidadePessoas = quantidadePessoas,
                    quantidadePagantes = quantidadePessoas,
                    garcomId = garcomId
                )
            }
        }
    }

    fun criarPagamento(
        mesaId: String,
        numeroMesa: Int,
        valorTotal: Double,
        quantidadePessoas: Int,
        quantidadePagantes: Int,
        garcomId: String
    ) {
        viewModelScope.launch {
            val result = pagamentoRepository.createPagamento(
                mesaId,
                numeroMesa,
                valorTotal,
                quantidadePessoas,
                quantidadePagantes,
                garcomId
            )

            result.onSuccess {
                _pagamento.value = it
            }
        }
    }

    fun adicionarPagamento(valor: Double, forma: FormaPagamento) {
        viewModelScope.launch {

            val pagamentoAtual = _pagamento.value ?: return@launch

            val result = pagamentoRepository.adicionarPagamentoParcial(
                pagamentoAtual.id,
                valor,
                forma
            )

            result.onSuccess {
                val atualizado = pagamentoRepository.getPagamentoById(pagamentoAtual.id)
                _pagamento.value = atualizado

                if (atualizado != null) {
                    val pago = atualizado.valorPago()
                    val total = atualizado.valorTotal

                    if (pago >= total) {
                        mesaRepository.fecharMesa(atualizado.mesaId)
                    }
                }
            }

            result.onFailure {
                _error.value = it.message
            }
        }
    }

    fun observarTotalMesa(pedidoFlow: Flow<List<Pedido>>) {
        viewModelScope.launch {
            pedidoFlow.collect { listaPedidos ->

                val pagamentoAtual = _pagamento.value ?: return@collect

                val total = listaPedidos.sumOf { it.valorTotal }

                val atualizado = pagamentoAtual.copy(valorTotal = total)

                pagamentoRepository.updatePagamento(atualizado)

                _pagamento.value = atualizado
            }
        }
    }
}
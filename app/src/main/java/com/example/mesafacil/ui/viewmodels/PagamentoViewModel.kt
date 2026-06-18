package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.FormaPagamento
import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.data.repositories.PagamentoRepository
import com.example.mesafacil.data.repositories.MesaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PagamentoViewModel : ViewModel() {

    private val pagamentoRepository = PagamentoRepository()

    private val _pagamento = MutableStateFlow<Pagamento?>(null)
    val pagamento: StateFlow<Pagamento?> = _pagamento

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val mesaRepository = MesaRepository()

    fun criarPagamento(
        mesaId: String,
        numeroMesa: Int,
        valorTotal: Double,
        quantidadePessoas: Int,
        quantidadePagantes: Int,
        garcomId: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = pagamentoRepository.createPagamento(
                mesaId = mesaId,
                numeroMesa = numeroMesa,
                valorTotal = 0.0,
                quantidadePessoas = quantidadePessoas,
                quantidadePagantes = quantidadePessoas,
                garcomId = garcomId
            )

            result.onSuccess { pagamento ->
                _pagamento.value = pagamento
            }

            result.onFailure {
                _error.value = it.message
            }

            _loading.value = false
        }
    }

    fun carregarPagamento(mesaId: String) {
        viewModelScope.launch {
            _error.value = null

            val pagamento = pagamentoRepository.getPagamentoByMesa(mesaId)
            _pagamento.value = pagamento
        }
    }

    fun adicionarPagamento(valor: Double, forma: FormaPagamento) {
        viewModelScope.launch {

            val pagamentoAtual = _pagamento.value ?: return@launch

            _loading.value = true
            _error.value = null

            val result = pagamentoRepository.adicionarPagamentoParcial(
                pagamentoAtual.id,
                valor,
                forma
            )

            result.onSuccess {

                val atualizado = pagamentoRepository.getPagamentoById(pagamentoAtual.id)
                _pagamento.value = atualizado

                // 🔥 AQUI É O LUGAR CERTO
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

            _loading.value = false
        }
    }
}

package com.example.mesafacil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesafacil.data.models.Mesa
import com.example.mesafacil.data.repositories.MesaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MesaViewModel : ViewModel() {
    private val mesaRepository = MesaRepository()

    private val _mesas = MutableStateFlow<List<Mesa>>(emptyList())
    val mesas: StateFlow<List<Mesa>> = _mesas

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadMesas()
    }

    private fun loadMesas() {
        viewModelScope.launch {
            mesaRepository.getAllMesas().collectLatest { mesas ->
                _mesas.value = mesas
            }
        }
    }

    fun abrirMesa(
        mesaId: String,
        quantidadePessoas: Int,
        garcomId: String,
        garcomNome: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                mesaRepository.abrirMesa(
                    mesaId,
                    quantidadePessoas,
                    garcomId,
                    garcomNome
                )
            } catch (e: Exception) {
                _error.value = e.message
            }

            _loading.value = false
        }
    }

    fun fecharMesa(mesaId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                mesaRepository.fecharMesa(mesaId)
            } catch (e: Exception) {
                _error.value = e.message
            }

            _loading.value = false
        }
    }

    fun unirMesas(
        mesaIds: List<String>,
        garcomId: String,
        garcomNome: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                mesaRepository.unirMesas(
                    mesaIds,
                    garcomId,
                    garcomNome
                )
            } catch (e: Exception) {
                _error.value = e.message
            }

            _loading.value = false
        }
    }

    fun getAllMesas() {
        viewModelScope.launch {
            mesaRepository.getAllMesas()
        }
    }

    fun liberarMesa(mesaId: String) {
        viewModelScope.launch {
            mesaRepository.liberarMesa(mesaId)
        }
    }

    fun liberarGrupoMesa(mesa: Mesa) {
        viewModelScope.launch {
            mesaRepository.liberarGrupoMesa(mesa)
        }
    }
}
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

    private val repository = MesaRepository()

    private val _mesas = MutableStateFlow<List<Mesa>>(emptyList())
    val mesas: StateFlow<List<Mesa>> = _mesas

    init {
        viewModelScope.launch {
            repository.getAllMesas().collect {
                _mesas.value = it
            }
        }
    }

    fun abrirMesa(mesaId: String, pessoas: Int, garcomId: String, garcomNome: String) {
        viewModelScope.launch {
            repository.abrirMesa(mesaId, pessoas, garcomId, garcomNome)
        }
    }

    fun fecharMesa(mesaId: String) {
        viewModelScope.launch {
            repository.fecharMesa(mesaId)
        }
    }

    fun liberarMesa(mesa: Mesa) {
        viewModelScope.launch {
            repository.liberarGrupoMesa(mesa)
        }
    }

    fun liberarGrupoMesa(mesa: Mesa) {
        viewModelScope.launch {
            repository.liberarGrupoMesa(mesa)
        }
    }

    fun unirMesas(
        mesasIds: List<String>,
        garcomId: String,
        garcomNome: String
    ) {
        viewModelScope.launch {
            repository.unirMesas(mesasIds, garcomId, garcomNome)
        }
    }
}
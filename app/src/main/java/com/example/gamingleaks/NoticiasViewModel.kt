package com.example.gamingleaks

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoticiasViewModel : ViewModel() {
    var listaNoticias = mutableStateListOf<Noticia>()
        private set

    init {
        cargarTodas()
    }

    fun cargarTodas() {
        viewModelScope.launch {
            try {
                val respuesta = RetrofitClient.api.obtenerTodas()
                listaNoticias.clear()
                listaNoticias.addAll(respuesta)
            } catch (e: Exception) {
                println("Error conexión: ${e.message}")
            }
        }
    }

    fun filtrarPor(categoria: String) {
        viewModelScope.launch {
            try {
                val respuesta = RetrofitClient.api.filtrar(categoria)
                listaNoticias.clear()
                listaNoticias.addAll(respuesta)
            } catch (e: Exception) {
                println("Error filtrando: ${e.message}")
            }
        }
    }
}
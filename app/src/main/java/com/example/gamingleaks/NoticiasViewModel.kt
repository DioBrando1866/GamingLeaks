package com.example.gamingleaks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoticiasViewModel : ViewModel() {

    var listaNoticias by mutableStateOf<List<Noticia>>(emptyList())
    var listaFavoritos by mutableStateOf<List<Noticia>>(emptyList())

    private val USER_ID = 1

    init {
        cargarTodas()
        cargarFavoritos()
    }


    fun cargarTodas() {
        viewModelScope.launch {
            try {
                listaNoticias = RetrofitClient.api.obtenerTodas()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filtrarPor(categoria: String) {
        viewModelScope.launch {
            try {
                listaNoticias = RetrofitClient.api.filtrar(categoria)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun cargarFavoritos() {
        viewModelScope.launch {
            try {
                listaFavoritos = RetrofitClient.api.obtenerFavoritos(USER_ID)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFavorito(noticia: Noticia) {
        viewModelScope.launch {
            try {
                listaFavoritos = RetrofitClient.api.toggleFavorito(USER_ID, noticia.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun esFavorita(noticia: Noticia): Boolean {
        return listaFavoritos.any { it.id == noticia.id }
    }
}
package com.example.gamingleaks

data class Autor(
    val id: Int,
    val nombre: String,
    val twitter: String,
    val fotoUrl: String
)

data class Juego(
    val id: Int,
    val titulo: String,
    val genero: String,
    val desarrolladora: String
)

data class Noticia(
    val id: Int,
    val titulo: String,
    val cuerpo: String,
    val categoria: String,
    val imagenUrl: String,
    val fecha: String,
    val autor: Autor?,
    val juego: Juego?
)
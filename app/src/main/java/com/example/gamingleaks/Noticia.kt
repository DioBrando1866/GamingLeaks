package com.example.gamingleaks

data class Noticia(
    val id: Int,
    val titulo: String,
    val cuerpo: String,
    val categoria: String,
    val imagenUrl: String,
    val fecha: String
)
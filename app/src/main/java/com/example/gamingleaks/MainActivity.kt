package com.example.gamingleaks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PantallaPrincipal()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: NoticiasViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Leaks & Drama") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- BOTONES DE FILTRO ---
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { viewModel.cargarTodas() }) { Text("TODO") }
                Button(
                    onClick = { viewModel.filtrarPor("LEAK") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("LEAKS") }
                Button(
                    onClick = { viewModel.filtrarPor("CONTROVERSIA") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) { Text("DRAMA") }
            }

            LazyColumn {
                items(viewModel.listaNoticias) { noticia ->
                    ItemNoticia(noticia)
                }
            }
        }
    }
}

@Composable
fun ItemNoticia(noticia: Noticia) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Imagen
            AsyncImage(
                model = noticia.imagenUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Etiqueta de color según categoría
                val colorEtiqueta = if (noticia.categoria == "LEAK") Color.Red else Color(0xFFFF9800)

                Text(
                    text = noticia.categoria,
                    color = colorEtiqueta,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = noticia.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = noticia.cuerpo,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3 // Cortar texto si es muy largo
                )
            }
        }
    }
}
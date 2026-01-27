package com.example.gamingleaks

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.gamingleaks.ui.theme.DarkBackground
import com.example.gamingleaks.ui.theme.DarkSurface
import com.example.gamingleaks.ui.theme.GamingPurple
import com.example.gamingleaks.ui.theme.TextGray
import com.example.gamingleaks.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = DarkBackground,
                    surface = DarkSurface,
                    primary = GamingPurple,
                    onBackground = TextWhite,
                    onSurface = TextWhite
                )
            ) {
                // LLAMAMOS AL GESTOR DE NAVEGACIÓN PRINCIPAL
                NavegacionRoot()
            }
        }
    }
}

// --- GESTOR: SPLASH VS APP ---
@Composable
fun NavegacionRoot() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen {
            showSplash = false // Cuando acabe el splash, cambiamos a la app
        }
    } else {
        MainApp()
    }
}

// --- PANTALLA SPLASH SCREEN ---
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Estado para la animación de escala
    val scale = remember { Animatable(0f) }

    // Efecto que se ejecuta al iniciar la pantalla
    LaunchedEffect(key1 = true) {
        // 1. Animamos el logo (efecto rebote)
        scale.animateTo(
            targetValue = 1.2f, // Crece un poco más del tamaño original
            animationSpec = tween(
                durationMillis = 800,
                easing = { OvershootInterpolator(4f).getInterpolation(it) }
            )
        )
        // 2. Esperamos un poco (tiempo total del splash: 2 segundos)
        delay(1200L)

        // 3. Avisamos que hemos terminado
        onTimeout()
    }

    // Diseño visual del Splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icono animado
            Icon(
                imageVector = Icons.Default.PlayArrow, // Icono "Gamer"
                contentDescription = "Logo",
                tint = GamingPurple,
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Texto
            Text(
                text = "GAMING LEAKS",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

// --- TU APP PRINCIPAL (MainApp) ---
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val items = listOf("Inicio", "Favoritos", "Ajustes")
    val icons = listOf(Icons.Default.Home, Icons.Default.Favorite, Icons.Default.Settings)

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1F1F1F),
                contentColor = GamingPurple
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = currentRoute == item,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GamingPurple,
                            selectedTextColor = GamingPurple,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray,
                            indicatorColor = GamingPurple.copy(alpha = 0.2f)
                        ),
                        onClick = {
                            navController.navigate(item) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Inicio",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Inicio") { PantallaPrincipal() }
            composable("Favoritos") {
                Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
                    Text("Favoritos", color = TextWhite)
                }
            }
            composable("Ajustes") {
                Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
                    Text("Ajustes", color = TextWhite)
                }
            }
        }
    }
}

// --- PANTALLA PRINCIPAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: NoticiasViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("GAMING LEAKS", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = DarkBackground,
                titleContentColor = GamingPurple,
                actionIconContentColor = TextWhite
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterButton(text = "TODO", color = GamingPurple, onClick = { viewModel.cargarTodas() })
            FilterButton(text = "LEAKS", color = Color(0xFFCF6679), onClick = { viewModel.filtrarPor("LEAK") })
            FilterButton(text = "DRAMA", color = Color(0xFFFFB74D), onClick = { viewModel.filtrarPor("CONTROVERSIA") })
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(viewModel.listaNoticias) { noticia ->
                ItemNoticia(noticia)
            }
        }
    }
}

// --- BOTÓN FILTRO ---
@Composable
fun FilterButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, color),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.height(36.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold)
    }
}

// --- ITEM NOTICIA ---
@Composable
fun ItemNoticia(noticia: Noticia) {
    var isLiked by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val colorCorazon by animateColorAsState(targetValue = if (isLiked) Color(0xFFCF6679) else TextGray, label = "ColorLike")
    val sizeCorazon by animateFloatAsState(targetValue = if (isLiked) 1.2f else 1.0f, label = "SizeLike")

    val infiniteTransition = rememberInfiniteTransition(label = "Infinite")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "Alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { offsetX = 0f },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { change, dragAmount -> change.consume(); offsetX += dragAmount }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = noticia.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )

                if (noticia.categoria == "LEAK") {
                    Surface(
                        color = Color.Red.copy(alpha = 0.85f * alphaAnim),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🔴 LIVE LEAK",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(noticia.categoria) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = if (noticia.categoria == "LEAK") Color(0xFFCF6679) else Color(0xFFFFB74D)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (noticia.categoria == "LEAK") Color(0xFFCF6679) else Color(0xFFFFB74D)
                        )
                    )

                    IconToggleButton(checked = isLiked, onCheckedChange = { isLiked = !isLiked }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = colorCorazon,
                            modifier = Modifier.scale(sizeCorazon)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = noticia.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = noticia.cuerpo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    maxLines = 3
                )
            }
        }
    }
}
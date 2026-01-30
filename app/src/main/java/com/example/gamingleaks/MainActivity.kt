package com.example.gamingleaks

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.gamingleaks.ui.theme.DarkBackground
import com.example.gamingleaks.ui.theme.DarkSurface
import com.example.gamingleaks.ui.theme.GamingPurple
import com.example.gamingleaks.ui.theme.TextGray
import com.example.gamingleaks.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                val viewModel: NoticiasViewModel = viewModel()
                NavegacionRoot(viewModel)
            }
        }
    }
}

@Composable
fun NavegacionRoot(viewModel: NoticiasViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen { showSplash = false }
    } else {
        MainApp(viewModel)
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 800,
                easing = { OvershootInterpolator(4f).getInterpolation(it) }
            )
        )
        delay(1200L)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Logo",
                tint = GamingPurple,
                modifier = Modifier.size(100.dp).scale(scale.value)
            )
            Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun MainApp(viewModel: NoticiasViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("Inicio", "Favoritos", "Ajustes")

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Inicio",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Inicio") {
                PantallaPrincipal(viewModel, navController)
            }

            composable(
                route = "Detalle/{noticiaId}",
                arguments = listOf(navArgument("noticiaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val noticiaId = backStackEntry.arguments?.getInt("noticiaId") ?: 0
                val noticiaSeleccionada = viewModel.listaNoticias.find { it.id == noticiaId }

                if (noticiaSeleccionada != null) {
                    DetalleNoticiaScreen(noticiaSeleccionada, navController)
                }
            }

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

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf("Inicio", "Favoritos", "Ajustes")
    val icons = listOf(Icons.Default.Home, Icons.Default.Favorite, Icons.Default.Settings)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: NoticiasViewModel, navController: NavController) {

    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("TODO") }
    var juegoIdSeleccionado by remember { mutableStateOf<Int?>(null) }
    var autorIdSeleccionado by remember { mutableStateOf<Int?>(null) }

    var mostrarFiltros by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    val juegosDisponibles = remember(viewModel.listaNoticias) {
        viewModel.listaNoticias.mapNotNull { it.juego }.distinctBy { it.id }
    }
    val autoresDisponibles = remember(viewModel.listaNoticias) {
        viewModel.listaNoticias.mapNotNull { it.autor }.distinctBy { it.id }
    }

    val noticiasParaMostrar = viewModel.listaNoticias.filter { noticia ->
        val coincideTexto = if (textoBusqueda.isBlank()) true else {
            noticia.titulo.contains(textoBusqueda, ignoreCase = true) ||
                    noticia.cuerpo.contains(textoBusqueda, ignoreCase = true)
        }
        val coincideCategoria = if (categoriaSeleccionada == "TODO") true else noticia.categoria == categoriaSeleccionada
        val coincideJuego = if (juegoIdSeleccionado == null) true else noticia.juego?.id == juegoIdSeleccionado
        val coincideAutor = if (autorIdSeleccionado == null) true else noticia.autor?.id == autorIdSeleccionado

        coincideTexto && coincideCategoria && coincideJuego && coincideAutor
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("GAMING LEAKS", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = DarkBackground,
                titleContentColor = GamingPurple,
                actionIconContentColor = TextWhite
            )
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                mostrarFiltros = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        mostrarFiltros = true
                    }
                },
            placeholder = { Text("Buscar noticia...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = GamingPurple) },
            trailingIcon = {
                if (mostrarFiltros) {
                    IconButton(onClick = {
                        textoBusqueda = ""
                        juegoIdSeleccionado = null
                        autorIdSeleccionado = null
                        categoriaSeleccionada = "TODO"
                        mostrarFiltros = false
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar Filtros", tint = TextGray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GamingPurple,
                unfocusedBorderColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = GamingPurple,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        AnimatedVisibility(
            visible = mostrarFiltros,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = categoriaSeleccionada == "TODO",
                        onClick = { categoriaSeleccionada = "TODO" },
                        label = { Text("Todo") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GamingPurple)
                    )
                    FilterChip(
                        selected = categoriaSeleccionada == "LEAK",
                        onClick = { categoriaSeleccionada = "LEAK" },
                        label = { Text("Leaks") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFCF6679))
                    )
                    FilterChip(
                        selected = categoriaSeleccionada == "CONTROVERSIA",
                        onClick = { categoriaSeleccionada = "CONTROVERSIA" },
                        label = { Text("Drama") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFB74D))
                    )
                }

                if (juegosDisponibles.isNotEmpty()) {
                    Text("Filtrar por Juego:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(juegosDisponibles) { juego ->
                            FilterChip(
                                selected = juegoIdSeleccionado == juego.id,
                                onClick = {
                                    juegoIdSeleccionado = if (juegoIdSeleccionado == juego.id) null else juego.id
                                },
                                label = { Text(juego.titulo) },
                                leadingIcon = { Text("🎮") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GamingPurple, labelColor = TextWhite)
                            )
                        }
                    }
                }

                if (autoresDisponibles.isNotEmpty()) {
                    Text("Filtrar por Insider:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(autoresDisponibles) { autor ->
                            FilterChip(
                                selected = autorIdSeleccionado == autor.id,
                                onClick = {
                                    autorIdSeleccionado = if (autorIdSeleccionado == autor.id) null else autor.id
                                },
                                label = { Text(autor.nombre) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GamingPurple, labelColor = TextWhite)
                            )
                        }
                    }
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.cargarTodas()
                    delay(1500)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (noticiasParaMostrar.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                            Text("No se encontraron noticias", color = TextGray)
                        }
                    }
                } else {
                    items(noticiasParaMostrar) { noticia ->
                        ItemNoticia(noticia) {
                            navController.navigate("Detalle/${noticia.id}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleNoticiaScreen(noticia: Noticia, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volver", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = noticia.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DarkBackground)
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                if (noticia.autor != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = noticia.autor.fotoUrl,
                            contentDescription = "Autor",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, GamingPurple, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = noticia.autor.nombre,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = noticia.autor.twitter,
                                color = GamingPurple,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF2C2C2C))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (noticia.juego != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎮", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${noticia.juego.titulo} - ${noticia.juego.desarrolladora}",
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = noticia.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = noticia.cuerpo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun ItemNoticia(noticia: Noticia, onClick: () -> Unit) {
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
            }
            .clickable { onClick() },
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
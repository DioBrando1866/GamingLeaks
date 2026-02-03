package com.example.gamingleaks

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.gamingleaks.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AjustesUsuario(
    val escalaTexto: Float = 1.0f,
    val altoContraste: Boolean = false,
    val mostrarImagenes: Boolean = true,
    val reducirMovimiento: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var ajustes by remember { mutableStateOf(AjustesUsuario()) }

            val colorPrimario = if (ajustes.altoContraste) Color.Yellow else GamingPurple
            val colorFondo = if (ajustes.altoContraste) Color.Black else DarkBackground
            val colorSuperficie = if (ajustes.altoContraste) Color(0xFF121212) else DarkSurface
            val colorTexto = if (ajustes.altoContraste) Color.White else TextWhite

            val currentDensity = LocalDensity.current
            val fontScaledDensity = remember(currentDensity, ajustes.escalaTexto) {
                Density(currentDensity.density, currentDensity.fontScale * ajustes.escalaTexto)
            }

            CompositionLocalProvider(LocalDensity provides fontScaledDensity) {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        background = colorFondo,
                        surface = colorSuperficie,
                        primary = colorPrimario,
                        onBackground = colorTexto,
                        onSurface = colorTexto
                    )
                ) {
                    val viewModel: NoticiasViewModel = viewModel()
                    NavegacionRoot(viewModel, ajustes) { nuevosAjustes ->
                        ajustes = nuevosAjustes
                    }
                }
            }
        }
    }
}

@Composable
fun NavegacionRoot(
    viewModel: NoticiasViewModel,
    ajustes: AjustesUsuario,
    onAjustesChanged: (AjustesUsuario) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(ajustes) { showSplash = false }
    } else {
        MainApp(viewModel, ajustes, onAjustesChanged)
    }
}

@Composable
fun SplashScreen(ajustes: AjustesUsuario, onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        if (ajustes.reducirMovimiento) {
            scale.snapTo(1.2f)
        } else {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = { OvershootInterpolator(4f).getInterpolation(it) }
                )
            )
        }
        delay(1200L)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Logotipo de la aplicación",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp).scale(scale.value)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "GAMING LEAKS",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun MainApp(
    viewModel: NoticiasViewModel,
    ajustes: AjustesUsuario,
    onAjustesChanged: (AjustesUsuario) -> Unit
) {
    val navController = rememberNavController()
    val tabs = listOf("Inicio", "Favoritos", "Ajustes")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Tabs"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute == "Tabs") {
                NavigationBar(
                    containerColor = Color(0xFF1F1F1F),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val icons = listOf(Icons.Default.Home, Icons.Default.Favorite, Icons.Default.Settings)
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = "Ir a $item") },
                            label = { Text(item) },
                            selected = pagerState.currentPage == index,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Tabs",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Tabs") {
                HorizontalPager(state = pagerState) { page ->
                    when (tabs[page]) {
                        "Inicio" -> PantallaPrincipal(viewModel, navController, ajustes)
                        "Favoritos" -> PantallaFavoritos(viewModel, navController, ajustes)
                        "Ajustes" -> PantallaAjustes(ajustes, onAjustesChanged)
                    }
                }
            }

            composable(
                route = "Detalle/{noticiaId}",
                arguments = listOf(navArgument("noticiaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val noticiaId = backStackEntry.arguments?.getInt("noticiaId") ?: 0
                val noticiaSeleccionada = viewModel.listaNoticias.find { it.id == noticiaId }

                if (noticiaSeleccionada != null) {
                    DetalleNoticiaScreen(noticiaSeleccionada, navController, ajustes)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: NoticiasViewModel, navController: NavController, ajustes: AjustesUsuario) {

    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("TODO") }
    var juegoIdSeleccionado by remember { mutableStateOf<Int?>(null) }
    var autorIdSeleccionado by remember { mutableStateOf<Int?>(null) }

    var mostrarFiltros by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val juegosDisponibles = remember(viewModel.listaNoticias) { viewModel.listaNoticias.mapNotNull { it.juego }.distinctBy { it.id } }

    val noticiasParaMostrar = viewModel.listaNoticias.filter { noticia ->
        val coincideTexto = if (textoBusqueda.isBlank()) true else {
            noticia.titulo.contains(textoBusqueda, ignoreCase = true) || noticia.cuerpo.contains(textoBusqueda, ignoreCase = true)
        }
        val coincideCategoria = if (categoriaSeleccionada == "TODO") true else noticia.categoria == categoriaSeleccionada
        val coincideJuego = if (juegoIdSeleccionado == null) true else noticia.juego?.id == juegoIdSeleccionado
        val coincideAutor = if (autorIdSeleccionado == null) true else noticia.autor?.id == autorIdSeleccionado
        coincideTexto && coincideCategoria && coincideJuego && coincideAutor
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text("GAMING LEAKS", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it; mostrarFiltros = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).onFocusChanged { if (it.isFocused) mostrarFiltros = true },
            placeholder = { Text("Buscar noticia...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (mostrarFiltros) {
                    IconButton(onClick = { textoBusqueda = ""; juegoIdSeleccionado = null; autorIdSeleccionado = null; categoriaSeleccionada = "TODO"; mostrarFiltros = false; focusManager.clearFocus() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar Filtros", tint = TextGray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TextGray,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = categoriaSeleccionada == "TODO", onClick = { categoriaSeleccionada = "TODO" }, label = { Text("Todo") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary))
                    FilterChip(selected = categoriaSeleccionada == "LEAK", onClick = { categoriaSeleccionada = "LEAK" }, label = { Text("Leaks") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if(ajustes.altoContraste) Color.Red else Color(0xFFCF6679)))
                    FilterChip(selected = categoriaSeleccionada == "CONTROVERSIA", onClick = { categoriaSeleccionada = "CONTROVERSIA" }, label = { Text("Drama") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if(ajustes.altoContraste) Color.Magenta else Color(0xFFFFB74D)))
                }

                if (juegosDisponibles.isNotEmpty()) {
                    Text("Filtrar por Juego:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(juegosDisponibles) { juego ->
                            FilterChip(
                                selected = juegoIdSeleccionado == juego.id,
                                onClick = { juegoIdSeleccionado = if (juegoIdSeleccionado == juego.id) null else juego.id },
                                label = { Text(juego.titulo) },
                                leadingIcon = { Text("🎮") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, labelColor = MaterialTheme.colorScheme.onBackground)
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
                    viewModel.cargarFavoritos()
                    delay(1500)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), modifier = Modifier.fillMaxSize()) {
                if (noticiasParaMostrar.isEmpty()) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) { Text("No se encontraron noticias", color = TextGray) } }
                } else {
                    items(noticiasParaMostrar) { noticia ->
                        ItemNoticia(
                            noticia = noticia,
                            isFav = viewModel.esFavorita(noticia),
                            onFavClick = { viewModel.toggleFavorito(noticia) },
                            ajustes = ajustes,
                            onClick = { navController.navigate("Detalle/${noticia.id}") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFavoritos(viewModel: NoticiasViewModel, navController: NavController, ajustes: AjustesUsuario) {
    LaunchedEffect(Unit) {
        viewModel.cargarFavoritos()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text("MIS FAVORITOS", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )

        if (viewModel.listaFavoritos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes noticias favoritas", color = TextGray)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(viewModel.listaFavoritos) { noticia ->
                    ItemNoticia(
                        noticia = noticia,
                        isFav = true,
                        onFavClick = { viewModel.toggleFavorito(noticia) },
                        ajustes = ajustes,
                        onClick = { navController.navigate("Detalle/${noticia.id}") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(
    ajustes: AjustesUsuario,
    onAjustesChanged: (AjustesUsuario) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Accesibilidad y Diseño", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )

        SectionTitle("Visualización")
        SettingItemSlider(
            title = "Tamaño del Texto",
            description = "Ajusta el tamaño para mejorar la lectura",
            value = ajustes.escalaTexto,
            valueRange = 0.8f..1.5f,
            onValueChange = { onAjustesChanged(ajustes.copy(escalaTexto = it)) }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        SettingItemSwitch(
            title = "Alto Contraste",
            description = "Colores vivos y fondo negro puro",
            checked = ajustes.altoContraste,
            icon = Icons.Default.Info,
            onCheckedChange = { onAjustesChanged(ajustes.copy(altoContraste = it)) }
        )

        SectionTitle("Contenido")
        SettingItemSwitch(
            title = "Mostrar Imágenes",
            description = "Desactiva para modo solo lectura o ahorro de datos",
            checked = ajustes.mostrarImagenes,
            icon = Icons.Default.Home,
            onCheckedChange = { onAjustesChanged(ajustes.copy(mostrarImagenes = it)) }
        )
        SettingItemSwitch(
            title = "Reducir Movimiento",
            description = "Elimina animaciones innecesarias",
            checked = ajustes.reducirMovimiento,
            icon = Icons.Default.PlayArrow,
            onCheckedChange = { onAjustesChanged(ajustes.copy(reducirMovimiento = it)) }
        )

        Spacer(modifier = Modifier.height(30.dp))
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vista Previa", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Así se verá el texto de las noticias con tu configuración actual.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun ItemNoticia(
    noticia: Noticia,
    isFav: Boolean,
    onFavClick: () -> Unit,
    ajustes: AjustesUsuario,
    onClick: () -> Unit
) {
    val colorCorazon = if (isFav) Color(0xFFCF6679) else TextGray
    val sizeCorazon = if (isFav && !ajustes.reducirMovimiento) 1.2f else 1.0f

    val infiniteTransition = rememberInfiniteTransition(label = "Infinite")
    val alphaAnim by if (!ajustes.reducirMovimiento) {
        infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 0.2f,
            animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse), label = "Alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(onClickLabel = "Ver detalles de ${noticia.titulo}") { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            if (ajustes.mostrarImagenes) {
                Box {
                    AsyncImage(
                        model = noticia.imagenUrl,
                        contentDescription = "Imagen de noticia: ${noticia.titulo}",
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    if (noticia.categoria == "LEAK") {
                        Surface(color = Color.Red.copy(alpha = 0.85f * alphaAnim), shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.padding(12.dp)) {
                            Text("LIVE LEAK", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(if (noticia.categoria == "LEAK") Color.Red else Color.Gray))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(noticia.categoria) },
                        colors = SuggestionChipDefaults.suggestionChipColors(labelColor = if (noticia.categoria == "LEAK") Color(0xFFCF6679) else Color(0xFFFFB74D))
                    )
                    IconToggleButton(checked = isFav, onCheckedChange = { onFavClick() }) {
                        Icon(imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = if (isFav) "Quitar me gusta" else "Dar me gusta", tint = colorCorazon, modifier = Modifier.scale(sizeCorazon))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = noticia.titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = noticia.cuerpo, style = MaterialTheme.typography.bodyMedium, color = TextGray, maxLines = 3)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleNoticiaScreen(noticia: Noticia, navController: NavController, ajustes: AjustesUsuario) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volver", fontSize = 16.sp) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás", tint = MaterialTheme.colorScheme.onBackground) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = MaterialTheme.colorScheme.onBackground, navigationIconContentColor = MaterialTheme.colorScheme.onBackground)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            if (ajustes.mostrarImagenes) {
                Box {
                    AsyncImage(model = noticia.imagenUrl, contentDescription = "Imagen detallada de ${noticia.titulo}", modifier = Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Crop)
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter).background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
                }
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (noticia.autor != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (ajustes.mostrarImagenes) {
                            AsyncImage(model = noticia.autor.fotoUrl, contentDescription = "Foto de perfil de ${noticia.autor.nombre}", modifier = Modifier.size(50.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape), contentScale = ContentScale.Crop)
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(text = noticia.autor.nombre, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = noticia.autor.twitter, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(color = Color(0xFF2C2C2C)); Spacer(modifier = Modifier.height(16.dp))
                }
                if (noticia.juego != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${noticia.juego.titulo} - ${noticia.juego.desarrolladora}", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(text = noticia.titulo, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = noticia.cuerpo, style = MaterialTheme.typography.bodyLarge, color = TextGray, lineHeight = 28.sp)
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable fun SectionTitle(text: String) {
    Text(text = text, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp), fontSize = 18.sp)
}

@Composable fun SettingItemSwitch(title: String, description: String, checked: Boolean, icon: ImageVector, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(text = description, fontSize = 14.sp, color = TextGray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
    }
}

@Composable fun SettingItemSlider(title: String, description: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = description, fontSize = 14.sp, color = TextGray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("A", fontSize = 12.sp, color = TextGray)
            Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, modifier = Modifier.weight(1f).padding(horizontal = 10.dp), colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary))
            Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
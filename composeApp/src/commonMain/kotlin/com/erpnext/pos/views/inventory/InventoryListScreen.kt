package com.erpnext.pos.views.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.utils.formatDoubleToString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    state: InventoryState,
    actions: InventoryAction
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = remember { listOf("Todos", "Pollo", "Papas", "Bebidas", "Postres") }

    LaunchedEffect(Unit) {
        actions.fetchAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(
                        onClick = actions.onRefresh,
                        enabled = state != InventoryState.Loading
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            "Actualizar Inventario",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.OnlinePrediction,
                            "Online Prediction",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            when (state) {
                is InventoryState.Success -> {
                    if (state.items.collectAsLazyPagingItems().itemCount > 0)
                        ExtendedFloatingActionButton(
                            onClick = actions.onPrint,
                            icon = { Icon(Icons.Filled.Print, "Imprimir lista") },
                            text = { Text("Imprimir") }
                        )
                }

                else -> {}
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            InventoryFilters(
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                categories = categories,
                onQueryChange = actions.onSearchQueryChanged,
                onCategoryChange = actions.onCategorySelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (state) {
                InventoryState.Loading -> {
                    FullScreenLoadingIndicator()
                }

                is InventoryState.Error -> {
                    FullScreenErrorMessage(
                        errorMessage = state.message,
                        onRetry = actions.onRefresh
                    )
                }

                InventoryState.Empty -> { // Estado explícito de "completamente vacío" desde el ViewModel
                    EmptyStateMessage(
                        message = "El inventario está completamente vacío.",
                        icon = Icons.Filled.SearchOff // O un icono más genérico
                    )
                }

                is InventoryState.Success -> {
                    if (state.items.collectAsLazyPagingItems().itemCount > 0) {
                        EmptyStateMessage(
                            message = "No se encontraron productos que coincidan con tu selección.",
                            icon = Icons.Filled.SearchOff
                        )
                    } else {
                        InventoryListContent(
                            paddingValue = paddingValues,
                            items = state.items.collectAsLazyPagingItems(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryFilters(
    searchQuery: String,
    selectedCategory: String,
    categories: List<String>,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (categories.isNotEmpty()) { // Solo mostrar si hay categorías
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category) },
                        shape = MaterialTheme.shapes.small
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        SearchTextField(
            searchQuery = searchQuery,
            onSearchQueryChange = onQueryChange,
            placeholderText = "Buscar por nombre o código..."
        )
    }
}

@Composable
fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar...",
    onSearchAction: (() -> Unit)? = null // Acción opcional al presionar "buscar" en el teclado
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Un poco de padding vertical para que respire
        placeholder = { Text(placeholderText, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Icono de búsqueda",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Un color sutil para el icono
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = {
                    onSearchQueryChange("") // Borra la búsqueda
                    keyboardController?.show() // Opcional: vuelve a mostrar el teclado si se desea
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Borrar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = if (onSearchAction != null) ImeAction.Search else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (onSearchAction != null) {
                    onSearchAction()
                    keyboardController?.hide()
                } else {
                    keyboardController?.hide()
                }
            },
            onDone = {
                keyboardController?.hide()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors( // Colores para que se vea más "Material You"
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium // Bordes redondeados consistentes con M3
    )
}

@Composable
private fun InventoryListContent(
    paddingValue: PaddingValues,
    items: LazyPagingItems<ItemBO>,
    modifier: Modifier = Modifier
) {
    /*LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {*/
    when (items.loadState.refresh) {
        LoadState.Loading -> {
            Box(
                modifier = Modifier
                    .padding(paddingValue)
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                CircularProgressIndicator()
            }
        }

        is LoadState.Error -> {
            Box(
                modifier = Modifier
                    .padding(paddingValue)
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Text(text = "Error")
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = paddingValue
            ) {
                items(
                    count = items.itemCount,
                    key = { index -> items[index]?.itemCode ?: index },
                    contentType = items.itemContentType { "ItemBO" },
                ) { index ->
                    val products = items[index] ?: return@items
                    ProductRowItem(products)
                }
            }
        }
    }
    //}
}

@Composable
fun ProductRowItem(product: ItemBO) {
    val formattedPrice = remember(product.price) {
        formatDoubleToString(product.price, 2)
    }
    val availableQty = remember(product.actualQty) {
        formatDoubleToString(product.actualQty, 0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    product.name, // Manejar nulos
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Disp: $availableQty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Cód: ${product.itemCode}", // Manejar nulos
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "C$$formattedPrice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- Componentes de Estado de Pantalla (Loading, Error, Empty) ---
@Composable
private fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenErrorMessage(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.CloudOff,
                "Error",
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview()
@Composable
fun InventoryScreenSuccessPreview() {
    val items = listOf(
        ItemBO(
            "Palitos de pollo", "AVPLCJ400",
            "Palitos de pollo congelados, cada de 400gr", "1234567890", "",
            100.456, 10.0, 0.0, false, false, "UND"
        )
    )
    val flow: Flow<PagingData<ItemBO>> = flowOf(PagingData.from(items))
    MaterialTheme {
        InventoryScreen(

            state = InventoryState.Success(flow),
            actions = InventoryAction()
        )
    }
}

@Composable
@Preview
fun InventoryScreenSuccessNoResultsPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Success(flowOf(PagingData.from(listOf()))),
            actions = InventoryAction()
        )
    }
}


@Preview()
@Composable
fun InventoryScreenLoadingPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Loading,
            actions = InventoryAction()
        )
    }
}

@Preview()
@Composable
fun InventoryScreenErrorPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Error("Esto es un error de prueba"),
            actions = InventoryAction()
        )
    }
}

@Preview()
@Composable
fun InventoryScreenEmptyStatePreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Empty,
            actions = InventoryAction()
        )
    }
}

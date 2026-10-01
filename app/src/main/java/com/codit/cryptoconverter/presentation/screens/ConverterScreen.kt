package com.codit.cryptoconverter.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codit.cryptoconverter.domain.model.Currency
import com.codit.cryptoconverter.domain.model.FavoritePair
import com.codit.cryptoconverter.presentation.viewmodel.ConverterViewModel

@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    onNavigateToMarket: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    var pickerSlot by remember { mutableStateOf<Int?>(null) }
    var showFavorites by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.isRefreshing) {
            LinearProgressIndicator(
                progress = { state.refreshProgress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Syncing prices… ${state.refreshProgress}%",
                style = MaterialTheme.typography.labelSmall
            )
        }

        // From / To cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrencyCard(
                modifier = Modifier.weight(1f),
                label = "From",
                code = state.fromCurrency,
                name = state.currencies.find { it.code == state.fromCurrency }?.name
                    ?: state.fromCurrency,
                value = state.inputExpression.ifEmpty { "0" },
                onClick = { pickerSlot = 1 }
            )
            IconButton(onClick = { viewModel.onSwap() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Swap")
            }
            CurrencyCard(
                modifier = Modifier.weight(1f),
                label = "To",
                code = state.toCurrency,
                name = state.currencies.find { it.code == state.toCurrency }?.name
                    ?: state.toCurrency,
                value = state.outputText.ifEmpty { "—" },
                onClick = { pickerSlot = 2 }
            )
        }

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.onToggleFavorite() }) {
                Icon(
                    if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (state.isFavorite) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(onClick = { showFavorites = true }) {
                Text("Favs (${state.favoritesCount})")
            }
            TextButton(onClick = onNavigateToMarket) {
                Text("Market")
            }
            if (state.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
        }

        // Calculator pad
        CalculatorPad(
            onDigit = { viewModel.onDigit(it) },
            onOperator = { viewModel.onOperator(it) },
            onEquals = { viewModel.onEquals() },
            onClear = { viewModel.onClear() },
            onBackspace = { viewModel.onBackspace() },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }

    pickerSlot?.let { slot ->
        CurrencyPickerDialog(
            currencies = state.currencies,
            onDismiss = { pickerSlot = null },
            onSelect = { code ->
                viewModel.onSelectCurrency(slot, code)
                pickerSlot = null
            }
        )
    }

    if (showFavorites) {
        FavoritesDialog(
            favorites = favorites,
            onDismiss = { showFavorites = false },
            onSelect = { pair ->
                viewModel.onApplyFavorite(pair.fromCurrency, pair.toCurrency)
                showFavorites = false
            },
            onDelete = { viewModel.onRemoveFavorite(it.fromCurrency, it.toCurrency) }
        )
    }
}

@Composable
private fun CurrencyCard(
    modifier: Modifier = Modifier,
    label: String,
    code: String,
    name: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text("$name ($code)", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CalculatorPad(
    modifier: Modifier = Modifier,
    onDigit: (Char) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit
) {
    val buttons = listOf("7", "8", "9", "÷", "4", "5", "6", "*", "1", "2", "3", "-", "0", ".", "=", "+")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onClear, modifier = Modifier.weight(1f)) { Text("C") }
            Button(onClick = onBackspace, modifier = Modifier.weight(1f)) { Text("⌫") }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(buttons) { label ->
                Button(
                    onClick = {
                        when (label) {
                            "=" -> onEquals()
                            "+", "-", "*", "÷" -> onOperator(label)
                            else -> onDigit(label.first())
                        }
                    }
                ) {
                    Text(label, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun CurrencyPickerDialog(
    currencies: List<Currency>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, currencies) {
        if (query.isBlank()) currencies
        else currencies.filter {
            it.code.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true)
        }.take(200)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Select currency") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(360.dp)) {
                    items(filtered) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(c.code) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${c.name} (${c.code})", modifier = Modifier.weight(1f))
                            Text(c.type.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun FavoritesDialog(
    favorites: List<FavoritePair>,
    onDismiss: () -> Unit,
    onSelect: (FavoritePair) -> Unit,
    onDelete: (FavoritePair) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Favorite pairs") },
        text = {
            if (favorites.isEmpty()) {
                Text("No favorites yet. Tap the heart to save the current pair.")
            } else {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(favorites) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${pair.fromCurrency} → ${pair.toCurrency}",
                                modifier = Modifier.weight(1f).clickable { onSelect(pair) }
                            )
                            IconButton(onClick = { onDelete(pair) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    )
}

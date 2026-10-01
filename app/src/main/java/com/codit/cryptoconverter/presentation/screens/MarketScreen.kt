package com.codit.cryptoconverter.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codit.cryptoconverter.presentation.viewmodel.MarketViewModel

@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChange(it) },
                label = { Text("Search coin") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
            if (state.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            } else {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
        }

        if (state.isRefreshing) {
            LinearProgressIndicator(
                progress = { state.refreshProgress / 100f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
        }

        Text(
            "Prices in ${state.defaultCurrency} • ${state.visiblePrices.size} coins",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        if (state.isEmpty && !state.isRefreshing) {
            Text(
                "No cached prices yet. Tap refresh to download.",
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.visiblePrices, key = { it.coinCode }) { coin ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coin.coinCode, fontWeight = FontWeight.Bold)
                            Text(coin.coinName, style = MaterialTheme.typography.bodySmall)
                        }
                        val price = coin.prices[state.defaultCurrency]
                        Text(
                            price?.toString() ?: "Not available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

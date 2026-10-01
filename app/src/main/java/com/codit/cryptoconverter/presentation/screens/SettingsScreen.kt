package com.codit.cryptoconverter.presentation.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.codit.cryptoconverter.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth().clickable { showCurrencyPicker = true }) {
            Column(Modifier.padding(14.dp)) {
                Text("Default market currency", style = MaterialTheme.typography.labelSmall)
                Text(state.defaultCurrency, style = MaterialTheme.typography.titleMedium)
                Text("Tap to change", style = MaterialTheme.typography.bodySmall)
            }
        }

        SettingsRow("Rate app", "Open Play Store listing") {
            openPlayStore(context, context.packageName)
        }
        SettingsRow("Share app", "Send Play Store link") {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${context.packageName}")
            }
            context.startActivity(Intent.createChooser(send, "Share app"))
        }
        SettingsRow("Contact us", "codit.apps@gmail.com") {
            val mail = Intent(Intent.ACTION_SENDTO, "mailto:codit.apps@gmail.com".toUri())
            context.startActivity(Intent.createChooser(mail, "Contact us"))
        }
        SettingsRow("Credits", "Icons, APIs") { showCurrencyPicker = false; showCredits = true }
    }

    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            confirmButton = {},
            title = { Text("Default currency") },
            text = {
                LazyColumn {
                    items(state.fiatCurrencies) { c ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultCurrency(c.code)
                                    showCurrencyPicker = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text("${c.name} (${c.code})")
                        }
                    }
                }
            }
        )
    }

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            confirmButton = {
                TextButton(onClick = { showCredits = false }) { Text("Close") }
            },
            title = { Text("Credits") },
            text = { Text("Icons: https://www.flaticon.com\nAPIs:\nhttps://www.cryptocompare.com\nhttps://www.blockcypher.com") }
        )
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun openPlayStore(context: android.content.Context, packageName: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    } catch (_: Exception) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
        )
    }
}

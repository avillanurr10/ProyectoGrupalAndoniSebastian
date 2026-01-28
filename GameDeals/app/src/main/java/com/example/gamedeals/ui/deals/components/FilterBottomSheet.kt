package com.example.gamedeals.ui.deals.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    maxPrice: Float,
    onMaxPriceChange: (Float) -> Unit,
    minDiscount: Float,
    onMinDiscountChange: (Float) -> Unit,
    minMetacritic: Float,
    onMinMetacriticChange: (Float) -> Unit,
    availableStores: Map<String, String>,
    selectedStores: Set<String>,
    onStoresChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtros Avanzados",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar")
                }
            }

            // Precio máximo
            FilterSection(
                title = "Precio máximo",
                valueText = "$${maxPrice.toInt()}",
                value = maxPrice,
                onValueChange = onMaxPriceChange,
                valueRange = 0f..60f,
                steps = 11
            )

            // Descuento mínimo
            FilterSection(
                title = "Descuento mínimo",
                valueText = "${minDiscount.toInt()}%",
                value = minDiscount,
                onValueChange = onMinDiscountChange,
                valueRange = 0f..90f,
                steps = 8
            )

            // Metacritic Score
            FilterSection(
                title = "Puntuación Metacritic",
                valueText = "${minMetacritic.toInt()}+",
                value = minMetacritic,
                onValueChange = onMinMetacriticChange,
                valueRange = 0f..100f,
                steps = 9
            )

            // Tiendas
            Column {
                Text(
                    "Tiendas (${selectedStores.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(availableStores.toList()) { (storeId, storeName) ->
                        FilterChip(
                            selected = storeId in selectedStores,
                            onClick = {
                                onStoresChange(
                                    if (storeId in selectedStores) selectedStores - storeId else selectedStores + storeId
                                )
                            },
                            label = { Text(storeName, fontSize = 13.sp) }
                        )
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Mostrar Resultados", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    valueText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

package com.example.gamedeals.ui.deals.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterDialog(
    maxPrice: Float,
    onMaxPriceChange: (Float) -> Unit,
    minDiscount: Float,
    onMinDiscountChange: (Float) -> Unit,
    availableStores: Map<String, String>,
    selectedStores: Set<String>,
    onStoresChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Filtros Avanzados", 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Precio máximo
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Precio máximo",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "$${maxPrice.toInt()}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = maxPrice,
                        onValueChange = onMaxPriceChange,
                        valueRange = 0f..60f,
                        steps = 11
                    )
                }

                Divider()

                // Descuento mínimo
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Descuento mínimo",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${minDiscount.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = minDiscount,
                        onValueChange = onMinDiscountChange,
                        valueRange = 0f..90f,
                        steps = 8
                    )
                }

                Divider()

                // Tiendas
                Column {
                    Text(
                        "Tiendas (${selectedStores.size} seleccionadas)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableStores.toList()) { (storeId, storeName) ->
                            FilterChip(
                                selected = storeId in selectedStores,
                                onClick = {
                                    onStoresChange(
                                        if (storeId in selectedStores) {
                                            selectedStores - storeId
                                        } else {
                                            selectedStores + storeId
                                        }
                                    )
                                },
                                label = { Text(storeName, fontSize = 13.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Aplicar Filtros")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onClearAll()
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Limpiar Todo")
            }
        }
    )
}

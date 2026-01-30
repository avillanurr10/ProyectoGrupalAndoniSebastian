package com.example.gamedeals.ui.deals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamedeals.R

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Anterior
        IconButton(
            onClick = onPrev,
            enabled = currentPage > 0,
            modifier = Modifier
                .background(
                    if (currentPage > 0) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                    CircleShape
                )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = stringResource(R.string.prev),
                tint = if (currentPage > 0) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Números de página (ventana deslizante)
        val startPage = (currentPage - 2).coerceAtLeast(0)
        val endPage = (startPage + 4).coerceAtMost(totalPages)

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in startPage..endPage) {
                val isSelected = i == currentPage
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onPageSelected(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (i + 1).toString(),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botón Siguiente
        IconButton(
            onClick = onNext,
            enabled = currentPage < totalPages,
            modifier = Modifier
                .background(
                    if (currentPage < totalPages) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                    CircleShape
                )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, 
                contentDescription = stringResource(R.string.next),
                tint = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }
    }
}

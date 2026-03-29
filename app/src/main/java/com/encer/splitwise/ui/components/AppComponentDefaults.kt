@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.encer.splitwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.encer.splitwise.ui.formatting.GroupedNumberVisualTransformation

val amountVisualTransformation = GroupedNumberVisualTransformation()

@Composable
fun appBlendOverBackground(color: Color, alpha: Float): Color =
    color.copy(alpha = alpha).compositeOver(MaterialTheme.colorScheme.background)

@Composable
fun appTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = appBlendOverBackground(MaterialTheme.colorScheme.surface, alpha = 0.96f),
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.primary
)

@Composable
fun appCardColors() = CardDefaults.elevatedCardColors(
    containerColor = appBlendOverBackground(MaterialTheme.colorScheme.surface, alpha = 0.94f),
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun appPlainCardColors() = CardDefaults.cardColors(
    containerColor = appBlendOverBackground(MaterialTheme.colorScheme.surface, alpha = 0.96f),
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun appFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.06f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun appFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
    labelColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
    selectedTrailingIconColor = MaterialTheme.colorScheme.primary,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
)

@Composable
fun appAssistChipColors() = AssistChipDefaults.assistChipColors(
    containerColor = appBlendOverBackground(MaterialTheme.colorScheme.surface, alpha = 0.16f),
    labelColor = MaterialTheme.colorScheme.onSurface,
    leadingIconContentColor = MaterialTheme.colorScheme.primary,
    trailingIconContentColor = MaterialTheme.colorScheme.primary
)

@Composable
fun appPrimaryButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun appOutlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun appSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

@Composable
fun appHeroAccentSurface(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (scheme.background.luminance() > 0.5f) {
        appBlendOverBackground(scheme.primary, alpha = 0.06f)
    } else {
        appBlendOverBackground(scheme.primary, alpha = 0.08f)
    }
}

@Composable
fun appHeroIconContainerColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (scheme.background.luminance() > 0.5f) {
        scheme.surface.copy(alpha = 0.82f).compositeOver(appHeroAccentSurface())
    } else {
        scheme.surface.copy(alpha = 0.18f).compositeOver(appHeroAccentSurface())
    }
}

fun appBackgroundBrush(isDark: Boolean): Brush =
    Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color(0xFF0D1719), Color(0xFF112226), Color(0xFF142A2F))
        } else {
            listOf(Color(0xFFF9FBF2), Color(0xFFF0F6F6), Color(0xFFFFF8EF))
        }
    )

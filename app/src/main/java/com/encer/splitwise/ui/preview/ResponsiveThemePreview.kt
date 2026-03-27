package com.encer.splitwise.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone-FA-Light",
    group = "responsive",
    widthDp = 411,
    heightDp = 891,
    showBackground = true,
)
@Preview(
    name = "Phone-FA-Dark",
    group = "responsive",
    widthDp = 411,
    heightDp = 891,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "Tablet-EN-Light",
    group = "responsive",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
)
annotation class ResponsiveThemePreview

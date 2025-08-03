package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppTextContent(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    titleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall,
    titleFontWeight: FontWeight = FontWeight.Medium,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    spacerHeight: androidx.compose.ui.unit.Dp = 0.dp
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = titleStyle,
            fontWeight = titleFontWeight,
            color = titleColor
        )

        Spacer(modifier = Modifier.height(spacerHeight))

        Text(
            text = content,
            style = contentStyle,
            color = contentColor
        )
    }
}
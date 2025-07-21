package com.example.attendancetaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enum defining the visual style of the button
 */
enum class AppIconButtonStyle {
    ROUNDED_ICON_ONLY,          // Rounded background with icon only
    ROUNDED_ICON_TEXT,          // Rounded background with icon and text
    ROUNDED_TEXT_ONLY,          // Rounded background with text only
    NO_BACKGROUND_TEXT_ONLY,    // No background with text only
    NO_BACKGROUND_ICON_ONLY,    // No background with icon only
    NO_BACKGROUND_ICON_TEXT     // No background with icon and text
}

/**
 * A versatile icon button component that supports multiple visual styles
 *
 * @param style The visual style of the button
 * @param onClick Callback when the button is clicked
 * @param modifier Modifier for styling the button
 * @param icon Optional icon to display
 * @param text Optional text to display
 * @param enabled Whether the button is enabled
 * @param backgroundColor Background color for rounded styles (null uses theme default)
 * @param contentColor Color for icon and text (null uses theme default)
 * @param iconSize Size of the icon
 * @param fontSize Size of the text
 * @param cornerRadius Corner radius for rounded styles
 * @param contentDescription Content description for accessibility
 * @param horizontalPadding Horizontal padding inside the button
 * @param verticalPadding Vertical padding inside the button
 * @param iconTextSpacing Spacing between icon and text when both are present
 */
@Composable
fun AppIconButton(
    style: AppIconButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    enabled: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    iconSize: Dp = 20.dp,
    fontSize: TextUnit = 14.sp,
    cornerRadius: Dp = 99.dp,
    contentDescription: String? = null,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 8.dp,
    iconTextSpacing: Dp = 6.dp
) {
    // Validate that required content is provided for the selected style
    require(
        when (style) {
            AppIconButtonStyle.ROUNDED_ICON_ONLY,
            AppIconButtonStyle.NO_BACKGROUND_ICON_ONLY -> icon != null
            AppIconButtonStyle.ROUNDED_TEXT_ONLY,
            AppIconButtonStyle.NO_BACKGROUND_TEXT_ONLY -> text != null
            AppIconButtonStyle.ROUNDED_ICON_TEXT,
            AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT -> icon != null && text != null
        }
    ) { "Required content (icon and/or text) must be provided for the selected style" }

    val hasBackground = when (style) {
        AppIconButtonStyle.ROUNDED_ICON_ONLY,
        AppIconButtonStyle.ROUNDED_ICON_TEXT,
        AppIconButtonStyle.ROUNDED_TEXT_ONLY -> true
        AppIconButtonStyle.NO_BACKGROUND_TEXT_ONLY,
        AppIconButtonStyle.NO_BACKGROUND_ICON_ONLY,
        AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT -> false
    }

    val hasIcon = when (style) {
        AppIconButtonStyle.ROUNDED_ICON_ONLY,
        AppIconButtonStyle.ROUNDED_ICON_TEXT,
        AppIconButtonStyle.NO_BACKGROUND_ICON_ONLY,
        AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT -> true
        AppIconButtonStyle.ROUNDED_TEXT_ONLY,
        AppIconButtonStyle.NO_BACKGROUND_TEXT_ONLY -> false
    }

    val hasText = when (style) {
        AppIconButtonStyle.ROUNDED_TEXT_ONLY,
        AppIconButtonStyle.ROUNDED_ICON_TEXT,
        AppIconButtonStyle.NO_BACKGROUND_TEXT_ONLY,
        AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT -> true
        AppIconButtonStyle.ROUNDED_ICON_ONLY,
        AppIconButtonStyle.NO_BACKGROUND_ICON_ONLY -> false
    }

    // Theme-based defaults
    val defaultBackgroundColor = if (hasBackground) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val defaultContentColor = if (hasBackground) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val actualBackgroundColor = backgroundColor ?: defaultBackgroundColor
    val actualContentColor = contentColor ?: defaultContentColor

    // Alpha for disabled state
    val alpha = if (enabled) 1f else 0.6f

    val baseModifier = modifier
        .clickable(
            enabled = enabled,
            role = Role.Button,
            onClickLabel = contentDescription
        ) { onClick() }

    val contentModifier = if (hasBackground) {
        baseModifier
            .background(
                color = actualBackgroundColor.copy(alpha = alpha),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    } else {
        baseModifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
    }

    // For icon-only buttons, ensure minimum touch target
    val finalModifier = if (!hasText) {
        contentModifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
    } else {
        contentModifier
    }

    when {
        hasIcon && hasText -> {
            // Icon and text layout
            Row(
                modifier = finalModifier,
                horizontalArrangement = Arrangement.spacedBy(iconTextSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon!!,
                    contentDescription = null, // Content description is on the whole button
                    modifier = Modifier.size(iconSize),
                    tint = actualContentColor.copy(alpha = alpha)
                )
                Text(
                    text = text!!,
                    color = actualContentColor.copy(alpha = alpha),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        hasIcon -> {
            // Icon only layout
            Box(
                modifier = finalModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(iconSize),
                    tint = actualContentColor.copy(alpha = alpha)
                )
            }
        }
        hasText -> {
            // Text only layout
            Box(
                modifier = finalModifier,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text!!,
                    color = actualContentColor.copy(alpha = alpha),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
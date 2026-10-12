package com.goldenboot.goldenbootfinancial.core.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import goldenbootfinancial.core.generated.resources.Res
import goldenbootfinancial.core.generated.resources.search_placeholder

/**
 * Compact inline search field that appears in the TopAppBar title area.
 *
 * Auto-focuses when first composed. Dismisses when focus is lost (e.g., tap outside).
 * Submits on keyboard search action.
 *
 * @param query Current search text.
 * @param onQueryChange Called when the user types.
 * @param onDismiss Called when the field loses focus (user tapped outside).
 */
@Composable
fun InlineSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    val placeholderText = stringResource(Res.string.search_placeholder)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var hasFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
        }),
        modifier = modifier
            .height(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                if (state.isFocused) {
                    hasFocused = true
                } else if (hasFocused) {
                    onDismiss()
                }
            },
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholderText,
                        style = textStyle.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview
@Composable
private fun InlineSearchFieldEmptyPreview() {
    MaterialTheme {
        InlineSearchField(
            query = "",
            onQueryChange = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun InlineSearchFieldWithTextPreview() {
    MaterialTheme {
        InlineSearchField(
            query = "search query",
            onQueryChange = {},
            onDismiss = {}
        )
    }
}

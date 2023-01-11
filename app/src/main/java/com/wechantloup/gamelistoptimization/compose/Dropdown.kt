package com.wechantloup.gamelistoptimization.compose

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

// ExposedDropdownMenuBox is experimental
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T: DropdownComparable> Dropdown(
    modifier: Modifier = Modifier,
    title: String,
    values: List<T>,
    selectedValue: T? = null,
    onValueSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption: T? by remember { mutableStateOf(selectedValue) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            enabled = values.isNotEmpty(),
            readOnly = true,
            value = selectedOption?.toString() ?: "",
            onValueChange = {},
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            values.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onValueSelected(selectionOption)
                        selectedOption = selectionOption
                        expanded = false
                    },
                ) {
                    Text(text = selectionOption.toString())
                }
            }
        }
    }

    val selection = selectedOption
    if (values.isNotEmpty() && (selection == null || !values.any { it.isSameAs(selection) })) {
        val value = values[0]
        selectedOption = value
        onValueSelected(value)
    }
}

interface DropdownComparable {
    fun isSameAs(other: DropdownComparable): Boolean { return this == other }
}

@Preview(showBackground = true)
@Composable
fun DropdownPreview() {
    Dropdown(
        title = "Games",
        values = listOf(StringComparable("Sonic"), StringComparable("Sonic2")),
        onValueSelected = {}
    )
}

private class StringComparable(val value: String): DropdownComparable {
    override fun toString(): String {
        return value
    }
}

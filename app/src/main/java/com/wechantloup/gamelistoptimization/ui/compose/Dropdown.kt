package com.wechantloup.gamelistoptimization.ui.compose

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
fun <T : DropdownComparable> Dropdown(
    modifier: Modifier = Modifier,
    title: String,
    values: List<T>,
    selectedIndex: Int = -1,
    onValueSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    if (values.isNotEmpty() && selectedIndex == -1) {
        onValueSelected(values[0])
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        val valueName = if (selectedIndex >= 0) values[selectedIndex].toString() else ""
        OutlinedTextField(
            enabled = values.isNotEmpty(),
            readOnly = true,
            value = valueName,
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
                        expanded = false

                        if (selectedIndex > 0 && selectionOption.isSameAs(values[selectedIndex])) return@DropdownMenuItem

                        onValueSelected(selectionOption)
                    },
                ) {
                    Text(text = selectionOption.toString())
                }
            }
        }
    }
}

interface DropdownComparable {

    fun isSameAs(other: DropdownComparable): Boolean {
        return this == other
    }
}

@Preview(showBackground = true)
@Composable
fun DropdownPreview() {
    Dropdown(
        title = "Games",
        values = listOf(StringComparable("Sonic"), StringComparable("Sonic2")),
        selectedIndex = 0,
        onValueSelected = {}
    )
}

private class StringComparable(val value: String) : DropdownComparable {

    override fun toString(): String {
        return value
    }
}

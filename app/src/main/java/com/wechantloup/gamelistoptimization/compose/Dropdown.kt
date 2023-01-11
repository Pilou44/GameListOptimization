package com.wechantloup.gamelistoptimization.compose

import android.util.Log
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
    selectedIndex: Int,
    onValueSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(selectedIndex) }

    if (values.isNotEmpty() && index == -1) {
        index = 0
        onValueSelected(values[index])
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        val valueName = if (index >= 0) values[index].toString() else ""
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
                        
                        if (selectionOption.isSameAs(values[index])) return@DropdownMenuItem

                        Log.d("TOTO", "On drop down clicked")
                        onValueSelected(selectionOption)
                        index = values.indexOfFirst { it.isSameAs(selectionOption) }
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

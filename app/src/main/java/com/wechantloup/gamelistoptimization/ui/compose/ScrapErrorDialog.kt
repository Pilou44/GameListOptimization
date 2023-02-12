package com.wechantloup.gamelistoptimization.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wechantloup.gamelistoptimization.model.ScrapResult

@Composable
fun ScrapErrorDialog(
    errors: List<ScrapResult>,
    clearErrors: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (errors.size == 1) {
        "Scrap error"
    } else {
        "Scrap errors"
    }
    AlertDialog(
        onDismissRequest = clearErrors,
        modifier = modifier,
        title = { Text(text = title) },
        text = { Message(errors) },
        buttons = { DismissButton(clearErrors) },
    )
}

@Composable
private fun Message(
    errors: List<ScrapResult>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        val requestErrors = errors.filter { it.status == ScrapResult.Status.TOO_MANY_REQUESTS }
        val crcErrors = errors.filter { it.status == ScrapResult.Status.BAD_CRC }
        val unknownErrors = errors.filter { it.status == ScrapResult.Status.UNKNOWN_GAME }

        if (requestErrors.isNotEmpty()) {
            Text("Too many requests error on:")
            requestErrors.forEach {
                Text("- ${it.game.getRomName()}")
            }
        }
        if (crcErrors.isNotEmpty()) {
            Text("CRC error on:")
            crcErrors.forEach {
                Text("- ${it.game.getRomName()}")
            }
        }
        if (unknownErrors.isNotEmpty()) {
            Text("Unknown game error on:")
            unknownErrors.forEach {
                Text("- ${it.game.getRomName()}")
            }
        }
    }
}

@Composable
private fun DismissButton(
    clearErrors: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = clearErrors,
        modifier = modifier,
    ) {
        Text(text = "OK")
    }
}

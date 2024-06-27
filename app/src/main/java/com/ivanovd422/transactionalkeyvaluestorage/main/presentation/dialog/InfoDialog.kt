package com.ivanovd422.transactionalkeyvaluestorage.main.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ivanovd422.transactionalkeyvaluestorage.R
import java.lang.IllegalStateException

@Composable
fun InfoDialog(
    modifier: Modifier = Modifier,
    message: DialogMessage? = null,
    onDismiss: () -> Unit = {}
) {

    val (title, color) = when (message) {
        is DialogMessage.Success -> {
            stringResource(R.string.dialog_title_success) to Color.Green
        }

        is DialogMessage.Error -> {
            stringResource(R.string.dialog_title_error) to Color.Red
        }

        else -> {
            stringResource(R.string.dialog_title_info) to Color.Magenta
        }
    }

    val subTitle = when (message) {
        is DialogMessage.Error -> {
            stringResource(id = message.text)
        }

        is DialogMessage.Success -> {
            stringResource(id = message.text, message.key, message.value)
        }

        is DialogMessage.Info -> {
            stringResource(id = message.text)
        }

        else -> throw IllegalStateException("Wrong dialog type")
    }

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color)

                ) {
                    Text(
                        text = title,
                        Modifier.padding(20.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }

                Text(
                    text = subTitle,
                    Modifier
                        .align(Alignment.Start)
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )
                Button(
                    onClick = { onDismiss.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Text(text = stringResource(id = R.string.dialog_button_ok))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoDialogSuccessPreview() {
    InfoDialog(
        message = DialogMessage.Success(
            text = R.string.execution_success_set_key_value,
            key = "abc",
            value = "zxc"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun InfoDialogErrorPreview() {
    InfoDialog(
        message = DialogMessage.Error(
            text = R.string.execution_error_no_such_item,
        )
    )
}

@Preview(showBackground = true)
@Composable
fun InfoDialogInfoPreview() {
    InfoDialog(
        message = DialogMessage.Info(
            text = R.string.main_screen_transaction_started,
        )
    )
}
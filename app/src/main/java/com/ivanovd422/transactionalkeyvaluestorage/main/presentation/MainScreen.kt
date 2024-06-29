package com.ivanovd422.transactionalkeyvaluestorage.main.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ivanovd422.transactionalkeyvaluestorage.R
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.Command
import com.ivanovd422.transactionalkeyvaluestorage.main.presentation.dialog.InfoDialog


@Composable
internal fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state = viewModel.mainState

    state.dialogMessage?.let {
        InfoDialog(
            message = it,
            onDismiss = remember { { viewModel.onAction(KeyValueStorageAction.DialogClosed) } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.SpaceBetween
    ) {
        RadioButtonsGroup(
            commandType = state.commandType,
            onAction = remember {
                {
                    viewModel.onAction(it)
                }
            }
        )

        if (state.commandType.isSetCommand()) {
            SetCommandGroup(
                onAction = remember {
                    {
                        viewModel.onAction(it)
                    }
                }
            )
        }

        if (state.commandType.isGetCommand()) {
            GetCommandGroup(
                onAction = remember {
                    {
                        viewModel.onAction(it)
                    }
                }
            )
        }

        if (state.commandType.isDeleteCommand()) {
            DeleteCommandGroup(
                onAction = remember {
                    {
                        viewModel.onAction(it)
                    }
                }
            )
        }

        if (state.commandType.isCountCommand()) {
            CountCommandGroup(
                onAction = remember {
                    {
                        viewModel.onAction(it)
                    }
                }
            )
        }

        TransactionGroup(
            count = state.transactionsInProgress,
            onAction = remember {
                {
                    viewModel.onAction(it)
                }
            }
        )
    }
}

@Composable
private fun TransactionGroup(
    count: Int,
    onAction: (KeyValueStorageAction.ExecuteCommand) -> Unit
) {
    val title = stringResource(id = R.string.main_screen_transaction_status)
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "$title $count",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = { onAction.invoke(KeyValueStorageAction.ExecuteCommand(Command.Begin)) }) {
                Text(stringResource(id = R.string.main_screen_button_begin))
            }
            Button(onClick = { onAction.invoke(KeyValueStorageAction.ExecuteCommand(Command.Commit)) }) {
                Text(stringResource(id = R.string.main_screen_button_commit))
            }
            Button(onClick = { onAction.invoke(KeyValueStorageAction.ExecuteCommand(Command.Rollback)) }) {
                Text(stringResource(id = R.string.main_screen_button_rollback))
            }
        }
    }
}

@Composable
private fun SetCommandGroup(
    onAction: (KeyValueStorageAction.ExecuteCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyText by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(id = R.string.main_screen_group_title_key))
                TextField(
                    value = keyText, onValueChange = {
                        keyText = it
                    }, singleLine = true
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(id = R.string.main_screen_group_title_value))
                TextField(
                    value = valueText, onValueChange = {
                        valueText = it
                    }, singleLine = true
                )
            }
        }
        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            enabled = keyText.isNotBlank() && valueText.isNotBlank(),
            onClick = {
                keyboardController?.hide()
                onAction.invoke(
                    KeyValueStorageAction.ExecuteCommand(
                        Command.Set(
                            key = keyText,
                            value = valueText
                        )
                    )
                )
            }
        ) {
            Text(text = stringResource(id = R.string.main_screen_button_execute))
        }
    }
}

@Composable
private fun GetCommandGroup(
    onAction: (KeyValueStorageAction.ExecuteCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(id = R.string.main_screen_group_title_key))
        TextField(
            value = keyText, onValueChange = {
                keyText = it
            }, singleLine = true
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            enabled = keyText.isNotBlank(),
            onClick = {
                keyboardController?.hide()
                onAction.invoke(
                    KeyValueStorageAction.ExecuteCommand(
                        Command.Get(
                            key = keyText
                        )
                    )
                )
            }
        ) {
            Text(text = stringResource(id = R.string.main_screen_button_execute))
        }
    }
}

@Composable
private fun DeleteCommandGroup(
    onAction: (KeyValueStorageAction.ExecuteCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(id = R.string.main_screen_group_title_delete_by_key))
        TextField(
            value = keyText,
            onValueChange = {
                keyText = it
            },
            singleLine = true
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            enabled = keyText.isNotBlank(),
            onClick = {
                keyboardController?.hide()
                onAction.invoke(
                    KeyValueStorageAction.ExecuteCommand(
                        Command.Delete(
                            key = keyText,
                        )
                    )
                )
            }
        ) {
            Text(text = stringResource(id = R.string.main_screen_button_execute))
        }
    }
}

@Composable
private fun CountCommandGroup(
    onAction: (KeyValueStorageAction.ExecuteCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var valueText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(id = R.string.main_screen_group_title_count_by_value))
        TextField(
            value = valueText,
            onValueChange = {
                valueText = it
            },
            singleLine = true
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            enabled = valueText.isNotBlank(),
            onClick = {
                keyboardController?.hide()
                onAction.invoke(
                    KeyValueStorageAction.ExecuteCommand(
                        Command.Count(
                            value = valueText,
                        )
                    )
                )
            }
        ) {
            Text(text = stringResource(id = R.string.main_screen_button_execute))
        }
    }
}

@Composable
private fun RadioButtonsGroup(
    commandType: CommandType,
    modifier: Modifier = Modifier,
    onAction: (KeyValueStorageAction.ChangeTypeCommand) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RadioButtonWidget(radioGroupName = stringResource(id = R.string.main_screen_radio_title_set),
                isSelected = commandType.isSetCommand(),
                clicked = {
                    onAction.invoke(KeyValueStorageAction.ChangeTypeCommand(CommandType.SET))
                })
            RadioButtonWidget(
                radioGroupName = stringResource(id = R.string.main_screen_radio_title_get),
                isSelected = commandType.isGetCommand(),
                clicked = {
                    onAction.invoke(KeyValueStorageAction.ChangeTypeCommand(CommandType.GET))
                })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RadioButtonWidget(
                radioGroupName = stringResource(id = R.string.main_screen_radio_title_delete),
                isSelected = commandType.isDeleteCommand(),
                clicked = {
                    onAction.invoke(KeyValueStorageAction.ChangeTypeCommand(CommandType.DELETE))
                })
            RadioButtonWidget(
                radioGroupName = stringResource(id = R.string.main_screen_radio_title_count),
                isSelected = commandType.isCountCommand(),
                clicked = {
                    onAction.invoke(KeyValueStorageAction.ChangeTypeCommand(CommandType.COUNT))
                })
        }
    }
}

@Composable
private fun RadioButtonWidget(
    radioGroupName: String,
    isSelected: Boolean,
    clicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable {
            clicked.invoke()
        }, verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = { clicked.invoke() })
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.width(60.dp)
        ) {
            Text(radioGroupName)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MainScreen()
}

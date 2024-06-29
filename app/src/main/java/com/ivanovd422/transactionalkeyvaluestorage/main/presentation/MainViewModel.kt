package com.ivanovd422.transactionalkeyvaluestorage.main.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivanovd422.transactionalkeyvaluestorage.R
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.Command
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.MainInteractor
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.onFailure
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.onSuccess
import com.ivanovd422.transactionalkeyvaluestorage.main.presentation.dialog.DialogMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainInteractor: MainInteractor,
) : ViewModel() {

    var mainState by mutableStateOf(MainState())

    fun onAction(action: KeyValueStorageAction) {
        when (action) {
            is KeyValueStorageAction.ChangeTypeCommand -> {
                changeTypeCommand(action.commandType)
            }

            is KeyValueStorageAction.DialogClosed -> {
                mainState = mainState.copy(
                    dialogMessage = null
                )
            }

            is KeyValueStorageAction.ExecuteCommand -> {
                handleCommand(action.command)
            }

        }
    }

    private fun changeTypeCommand(command: CommandType) {
        mainState = mainState.copy(
            commandType = command
        )
    }

    private fun handleCommand(command: Command) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mainInteractor.executeCommand(command)
            }.onSuccess {
                handleCommandSuccess(command, it)
            }.onFailure {
                showDialog(DialogMessage.Error(it.error))
            }
        }
    }

    private fun handleCommandSuccess(command: Command, result: Any) {
        when (command) {
            is Command.Set -> {
                showDialog(
                    DialogMessage.Success(
                        text = R.string.execution_success_set_key_value,
                        key = command.key,
                        value = command.value,
                    )
                )
            }

            is Command.Get -> {
                showDialog(
                    DialogMessage.Success(
                        text = R.string.execution_success_get_by_key,
                        key = command.key,
                        value = result as String,
                    )
                )
            }

            is Command.Delete -> {
                showDialog(
                    DialogMessage.Success(
                        text = R.string.execution_success_delete,
                        key = command.key,
                        value = result as String,
                    )
                )
            }

            is Command.Count -> {
                showDialog(
                    DialogMessage.Success(
                        text = R.string.execution_success_count,
                        key = command.value,
                        value = (result as Int).toString(),
                    )
                )
            }

            is Command.Begin -> {
                mainState = mainState.copy(
                    transactionsInProgress = mainState.transactionsInProgress + 1
                )
                showDialog(DialogMessage.Info(R.string.main_screen_transaction_started))
            }

            is Command.Commit -> {
                mainState = mainState.copy(
                    transactionsInProgress = mainState.transactionsInProgress - 1
                )
                showDialog(DialogMessage.Info(R.string.main_screen_transaction_committed))
            }

            is Command.Rollback -> {
                mainState = mainState.copy(
                    transactionsInProgress = mainState.transactionsInProgress - 1
                )
                showDialog(DialogMessage.Info(R.string.main_screen_transaction_rolled_back))
            }
        }
    }

    private fun showDialog(message: DialogMessage) {
        mainState = mainState.copy(
            dialogMessage = message
        )
    }
}
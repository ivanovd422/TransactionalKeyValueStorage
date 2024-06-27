package com.ivanovd422.transactionalkeyvaluestorage.main.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ivanovd422.transactionalkeyvaluestorage.R
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.MainInteractor
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.onAnyResult
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.onFailure
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.onSuccess
import com.ivanovd422.transactionalkeyvaluestorage.main.presentation.dialog.DialogMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainInteractor: MainInteractor
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
                handleExecuteCommand(action.command)
            }

            is KeyValueStorageAction.Transaction -> handleTransaction(action)
        }
    }

    private fun changeTypeCommand(command: CommandType) {
        mainState = mainState.copy(
            commandType = command
        )
    }

    private fun handleTransaction(action: KeyValueStorageAction.Transaction) {
        when (action) {
            KeyValueStorageAction.Transaction.BeginTransactionCommand -> {
                mainInteractor.beginTransaction().onAnyResult { _, _ ->
                    mainState = mainState.copy(
                        transactionsInProgress = mainState.transactionsInProgress + 1
                    )
                    showDialog(DialogMessage.Info(R.string.main_screen_transaction_started))
                }
            }

            KeyValueStorageAction.Transaction.CommitTransactionCommand -> {
                mainInteractor.commitTransaction()
                    .onSuccess {
                        mainState = mainState.copy(
                            transactionsInProgress = mainState.transactionsInProgress - 1
                        )
                        showDialog(DialogMessage.Info(R.string.main_screen_transaction_committed))
                    }
                    .onFailure {
                        showDialog(DialogMessage.Error(it.error))
                    }
            }

            KeyValueStorageAction.Transaction.RollbackTransactionCommand -> {
                mainInteractor.rollbackTransaction()
                    .onSuccess {
                        mainState = mainState.copy(
                            transactionsInProgress = mainState.transactionsInProgress - 1
                        )
                        showDialog(DialogMessage.Info(R.string.main_screen_transaction_rolled_back))
                    }
                    .onFailure {
                        showDialog(DialogMessage.Error(it.error))
                    }
            }
        }
    }

    private fun handleExecuteCommand(command: Command) {
        when (command) {
            is Command.SetCommand -> {
                mainInteractor.set(command.key, command.value)
                    .onSuccess {
                        showDialog(
                            DialogMessage.Success(
                                text = R.string.execution_success_set_key_value,
                                key = it.first,
                                value = it.second,
                            )
                        )
                    }
                    .onFailure {
                        showDialog(DialogMessage.Error(it.error))
                    }
            }

            is Command.GetCommand -> {
                mainInteractor.get(command.key)
                    .onSuccess {
                        showDialog(
                            DialogMessage.Success(
                                text = R.string.execution_success_get_by_key,
                                key = command.key,
                                value = it,
                            )
                        )
                    }
                    .onFailure {
                        showDialog(DialogMessage.Error(it.error))
                    }
            }

            is Command.DeleteCommand -> {
                mainInteractor.delete(command.key)
                    .onSuccess {
                        showDialog(
                            DialogMessage.Success(
                                text = R.string.execution_success_delete,
                                key = command.key,
                                value = it,
                            )
                        )
                    }
                    .onFailure {
                        showDialog(DialogMessage.Error(it.error))
                    }
            }

            is Command.CountCommand -> {
                mainInteractor.count(command.value)
                    .onSuccess {
                        showDialog(
                            DialogMessage.Success(
                                text = R.string.execution_success_count,
                                key = command.value,
                                value = it.toString(),
                            )
                        )
                    }
                    .onFailure {
                        showDialog(DialogMessage.Error(it.error))
                    }
            }
        }
    }

    private fun showDialog(message: DialogMessage) {
        mainState = mainState.copy(
            dialogMessage = message
        )
    }
}
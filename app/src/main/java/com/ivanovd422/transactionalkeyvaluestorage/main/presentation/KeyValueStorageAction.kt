package com.ivanovd422.transactionalkeyvaluestorage.main.presentation

import com.ivanovd422.transactionalkeyvaluestorage.main.domain.Command

sealed interface KeyValueStorageAction {
    data class ExecuteCommand(val command: Command) : KeyValueStorageAction
    data class ChangeTypeCommand(val commandType: CommandType) : KeyValueStorageAction
    data object DialogClosed : KeyValueStorageAction
}
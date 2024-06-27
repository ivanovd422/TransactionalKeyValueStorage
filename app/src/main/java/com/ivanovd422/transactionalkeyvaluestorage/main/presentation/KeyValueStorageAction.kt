package com.ivanovd422.transactionalkeyvaluestorage.main.presentation

sealed interface KeyValueStorageAction {
    data class ExecuteCommand(val command: Command) : KeyValueStorageAction
    data class ChangeTypeCommand(val commandType: CommandType) : KeyValueStorageAction
    data object DialogClosed : KeyValueStorageAction

    sealed interface Transaction : KeyValueStorageAction {
        data object BeginTransactionCommand : Transaction
        data object CommitTransactionCommand : Transaction
        data object RollbackTransactionCommand : Transaction
    }
}

sealed interface Command {
    data class SetCommand(val key: String, val value: String) : Command
    data class GetCommand(val key: String) : Command
    data class DeleteCommand(val key: String) : Command
    data class CountCommand(val value: String) : Command
}
package com.ivanovd422.transactionalkeyvaluestorage.main.presentation

import com.ivanovd422.transactionalkeyvaluestorage.main.presentation.dialog.DialogMessage

data class MainState(
    val commandType: CommandType = CommandType.SET,
    val dialogMessage: DialogMessage? = null,
    val transactionsInProgress: Int = 0
)

fun CommandType.isSetCommand(): Boolean {
   return this == CommandType.SET
}

fun CommandType.isGetCommand(): Boolean {
    return this == CommandType.GET
}

fun CommandType.isDeleteCommand(): Boolean {
    return this == CommandType.DELETE
}

fun CommandType.isCountCommand(): Boolean {
    return this == CommandType.COUNT
}

enum class CommandType {
    SET,
    GET,
    DELETE,
    COUNT
}


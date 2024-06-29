package com.ivanovd422.transactionalkeyvaluestorage.main.domain

sealed interface Command {
    data class Set(val key: String, val value: String) : Command
    data class Get(val key: String): Command
    data class Delete(val key: String) : Command
    data class Count(val value: String) : Command
    data object Begin : Command
    data object Commit : Command
    data object Rollback : Command
}

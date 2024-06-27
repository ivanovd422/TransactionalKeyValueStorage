package com.ivanovd422.transactionalkeyvaluestorage.main.domain

import com.ivanovd422.transactionalkeyvaluestorage.R


sealed class ExecutionResult<R> {
    data class Success<R>(val value: R) : ExecutionResult<R>()
    data class Error<R>(val executionError: ExecutionError) : ExecutionResult<R>()
}

inline fun <T> ExecutionResult<T>.onFailure(action: (executionError: ExecutionError) -> Unit): ExecutionResult<T> {
    (this as? ExecutionResult.Error)?.let {
        action(executionError)
    }
    return this
}

inline fun <T> ExecutionResult<T>.onSuccess(action: (value: T) -> Unit): ExecutionResult<T> {
    (this as? ExecutionResult.Success)?.let {
        action(value)
    }
    return this
}

inline fun <T> ExecutionResult<T>.onAnyResult(action: (value: T?, executionError: ExecutionError?) -> Unit): ExecutionResult<T> {
    when (this) {
        is ExecutionResult.Success -> action(value, null)
        is ExecutionResult.Error -> action(null, executionError)
    }
    return this
}

enum class ExecutionError(val error: Int) {
    NO_SUCH_ITEM(R.string.execution_error_no_such_item),
    TRANSACTION_IS_EMPTY(R.string.execution_error_no_transaction),
}
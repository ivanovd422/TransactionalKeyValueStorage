package com.ivanovd422.transactionalkeyvaluestorage.main.domain

import com.ivanovd422.transactionalkeyvaluestorage.main.data.KeyValueStorage
import java.lang.RuntimeException
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val storage: KeyValueStorage
) {
    fun set(key: String, value: String): ExecutionResult<Pair<String, String>> {
        storage.set(key, value)
        return ExecutionResult.Success((Pair(key, value)))
    }

    fun get(key: String): ExecutionResult<String> {
        val value = storage.get(key)

        return if (value == null) {
            ExecutionResult.Error(ExecutionError.NO_SUCH_ITEM)
        } else {
            ExecutionResult.Success(value)
        }
    }

    fun delete(key: String): ExecutionResult<String> {
        if (storage.get(key) == null) {
            return ExecutionResult.Error(ExecutionError.NO_SUCH_ITEM)
        }

        val value = storage.delete(key)

        return if (value == null) {
            ExecutionResult.Error(ExecutionError.NO_SUCH_ITEM)
        } else {
            ExecutionResult.Success(value)
        }
    }

    fun count(value: String): ExecutionResult<Int> {
        val count = storage.count(value)

        return if (count == null) {
            ExecutionResult.Error(ExecutionError.NO_SUCH_ITEM)
        } else {
            ExecutionResult.Success(count)
        }
    }

    fun beginTransaction(): ExecutionResult<Unit> {
        storage.beginTransaction()
        return ExecutionResult.Success(Unit)
    }

    fun commitTransaction(): ExecutionResult<Unit> {
        return try {
            storage.commitTransaction()
            ExecutionResult.Success(Unit)
        } catch (e: RuntimeException) {
            ExecutionResult.Error((ExecutionError.TRANSACTION_IS_EMPTY))
        }
    }

    fun rollbackTransaction(): ExecutionResult<Unit> {
        return try {
            storage.rollbackTransaction()
            ExecutionResult.Success(Unit)
        } catch (e: RuntimeException) {
            ExecutionResult.Error((ExecutionError.TRANSACTION_IS_EMPTY))
        }
    }
}
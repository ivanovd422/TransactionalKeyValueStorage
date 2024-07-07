package com.ivanovd422.transactionalkeyvaluestorage.main.domain

import com.ivanovd422.transactionalkeyvaluestorage.main.data.KeyValueStorage
import java.lang.RuntimeException
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val storage: KeyValueStorage
) {

    suspend fun executeCommand(command: Command): ExecutionResult<out Any> {
        return when (command) {
            is Command.Set -> set(command.key, command.value)
            is Command.Get -> get(command.key)
            is Command.Delete -> delete(command.key)
            is Command.Count -> count(command.value)
            is Command.Begin -> beginTransaction()
            is Command.Commit -> commitTransaction()
            is Command.Rollback -> rollbackTransaction()
        }
    }

    private suspend fun set(key: String, value: String): ExecutionResult<Unit> {
        storage.set(key, value)
        return ExecutionResult.Success(Unit)
    }

    private suspend fun get(key: String): ExecutionResult<String> {
        val value = storage.get(key)

        return if (value == null) {
            ExecutionResult.Error(ExecutionError.NO_SUCH_ITEM)
        } else {
            ExecutionResult.Success(value)
        }
    }

    private suspend fun delete(key: String): ExecutionResult<String> {
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

    private suspend fun count(value: String): ExecutionResult<Int> {
        val count = storage.count(value)

        return if (count == null) {
            ExecutionResult.Error(ExecutionError.NO_SUCH_ITEM)
        } else {
            ExecutionResult.Success(count)
        }
    }

    private suspend fun beginTransaction(): ExecutionResult<Unit> {
        storage.beginTransaction()
        return ExecutionResult.Success(Unit)
    }

    private suspend fun commitTransaction(): ExecutionResult<Unit> {
        return try {
            storage.commitTransaction()
            ExecutionResult.Success(Unit)
        } catch (e: RuntimeException) {
            ExecutionResult.Error((ExecutionError.TRANSACTION_IS_EMPTY))
        }
    }

    private suspend fun rollbackTransaction(): ExecutionResult<Unit> {
        return try {
            storage.rollbackTransaction()
            ExecutionResult.Success(Unit)
        } catch (e: RuntimeException) {
            ExecutionResult.Error((ExecutionError.TRANSACTION_IS_EMPTY))
        }
    }
}
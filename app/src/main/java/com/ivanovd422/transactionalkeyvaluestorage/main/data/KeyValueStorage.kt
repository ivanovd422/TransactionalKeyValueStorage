package com.ivanovd422.transactionalkeyvaluestorage.main.data

import com.ivanovd422.transactionalkeyvaluestorage.main.di.SingleThreadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Stack
import javax.inject.Inject

class KeyValueStorage @Inject constructor(
    @SingleThreadDispatcher private val singleThreadDispatcher: CoroutineDispatcher
) {

    private val storage = mutableMapOf<String, String>()
    private val valuesCount = mutableMapOf<String, Int>()
    private val transactionStack = Stack<Transaction>()
    private val mutex = Mutex()

    suspend fun set(key: String, value: String): Unit = withContext(singleThreadDispatcher + NonCancellable) {
        mutex.withLock {
            if (transactionStack.isEmpty()) {
                set(key, value, storage, valuesCount)
            } else {
                transactionStack.peek().apply {
                    set(key, value, sessionStorage, sessionValuesCount)
                }
            }
        }
    }

    suspend fun get(key: String): String? {
        return mutex.withLock {
            if (transactionStack.isEmpty()) {
                storage[key]
            } else {
                transactionStack.peek().sessionStorage[key]
            }
        }
    }

    suspend fun delete(key: String): String? = withContext(singleThreadDispatcher + NonCancellable) {
        mutex.withLock {
            return@withContext if (transactionStack.isEmpty()) {
                delete(key, storage, valuesCount)
            } else {
                val transaction = transactionStack.peek()
                delete(key, transaction.sessionStorage, transaction.sessionValuesCount)
            }
        }
    }

    suspend fun count(value: String): Int? {
        return mutex.withLock {
            if (transactionStack.isEmpty()) {
                valuesCount[value]
            } else {
                transactionStack.peek().sessionValuesCount[value]
            }
        }
    }

    suspend fun beginTransaction() = withContext(singleThreadDispatcher + NonCancellable) {
        mutex.withLock {
            if (transactionStack.isEmpty()) {
                transactionStack.add(Transaction(storage.toMutableMap(), valuesCount.toMutableMap()))
            } else {
                val prevTransaction = transactionStack.peek()
                transactionStack.add(
                    Transaction(
                        prevTransaction.sessionStorage.toMutableMap(),
                        prevTransaction.sessionValuesCount.toMutableMap()
                    )
                )
            }
        }
    }

    suspend fun commitTransaction(): Unit = withContext(singleThreadDispatcher + NonCancellable) {
        mutex.withLock {
            val transaction = transactionStack.pop()
            if (transactionStack.isEmpty()) {
                commitTransaction(transaction, storage, valuesCount)
            } else {
                commitTransaction(
                    transaction,
                    transactionStack.peek().sessionStorage,
                    transactionStack.peek().sessionValuesCount,
                )
            }
        }
    }

    suspend fun rollbackTransaction() {
        withContext(singleThreadDispatcher + NonCancellable) {
            mutex.withLock {
                transactionStack.pop()
            }
        }
    }

    private fun set(
        key: String,
        value: String,
        storage: MutableMap<String, String>,
        valuesCount: MutableMap<String, Int>
    ) {
        if (storage.containsKey(key) && storage[key] != value) {
            val oldValue = storage[key]!!
            if (valuesCount.getOrDefault(oldValue, 0) > 1) {
                valuesCount[oldValue] = valuesCount[oldValue]!! - 1
            } else {
                valuesCount.remove(oldValue)
            }
        }
        if (!storage.containsKey(key) || storage[key] != value) {
            valuesCount[value] = valuesCount.getOrDefault(value, 0) + 1
        }
        storage[key] = value
    }

    private fun delete(
        key: String,
        storage: MutableMap<String, String>,
        valuesCount: MutableMap<String, Int>
    ): String? {
        val value = storage[key]
        if (valuesCount[value]!! > 1) {
            valuesCount[value!!] = valuesCount[value]!! - 1
        } else {
            valuesCount.remove(value)
        }
        return storage.remove(key)
    }

    private fun commitTransaction(
        transaction: Transaction,
        storage: MutableMap<String, String>,
        valuesCount: MutableMap<String, Int>
    ) {
        storage.apply {
            clear()
            putAll(transaction.sessionStorage)
        }
        valuesCount.apply {
            clear()
            putAll(transaction.sessionValuesCount)
        }
    }
}

private class Transaction(
    val sessionStorage: MutableMap<String, String>,
    val sessionValuesCount: MutableMap<String, Int>,
)
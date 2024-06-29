package com.ivanovd422.transactionalkeyvaluestorage.main.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Stack
import javax.inject.Inject

class KeyValueStorage @Inject constructor() {

    private val storage = mutableMapOf<String, String>()
    private val valuesCount = mutableMapOf<String, Int>()
    private val transactionStack = Stack<Transaction>()
    private val mutex = Mutex()

    suspend fun set(key: String, value: String) {
        mutex.withLock {
            if (transactionStack.isEmpty()) {
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
            } else {
                transactionStack.peek().apply {
                    if (sessionStorage.containsKey(key) && sessionStorage[key] != value) {
                        val oldValue = sessionStorage[key]!!
                        if (sessionValuesCount.getOrDefault(oldValue, 0) > 1) {
                            sessionValuesCount[oldValue] = sessionValuesCount[oldValue]!! - 1
                        } else {
                            sessionValuesCount.remove(oldValue)
                        }
                    }
                    if (!sessionStorage.containsKey(key) || sessionStorage[key] != value) {
                        sessionValuesCount[value] = sessionValuesCount.getOrDefault(value, 0) + 1
                    }
                    sessionStorage[key] = value
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

    suspend fun delete(key: String): String? {
        return mutex.withLock {
            if (transactionStack.isEmpty()) {
                val value = storage[key]
                if (valuesCount[value]!! > 1) {
                    valuesCount[value!!] = valuesCount[value]!! - 1
                } else {
                    valuesCount.remove(value)
                }
                storage.remove(key)
            } else {
                val transaction = transactionStack.peek()
                val value = transaction.sessionStorage[key]
                if (transaction.sessionValuesCount[value]!! > 1) {
                    transaction.sessionValuesCount[value!!] = transaction.sessionValuesCount[value]!! - 1
                } else {
                    transaction.sessionValuesCount.remove(value)
                }
                transaction.sessionStorage.remove(key)
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

    suspend fun beginTransaction() {
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

    suspend fun commitTransaction() {
        mutex.withLock {
            val transaction = transactionStack.pop()
            if (transactionStack.isEmpty()) {
                storage.apply {
                    clear()
                    putAll(transaction.sessionStorage)
                }
                valuesCount.apply {
                    clear()
                    putAll(transaction.sessionValuesCount)
                }
            } else {
                transactionStack.peek().apply {
                    sessionStorage.clear()
                    sessionStorage.putAll(transaction.sessionStorage)
                    sessionValuesCount.clear()
                    sessionValuesCount.putAll(transaction.sessionValuesCount)
                }
            }
        }
    }

    suspend fun rollbackTransaction() {
        mutex.withLock {
            transactionStack.pop()
        }
    }
}

private class Transaction(
    val sessionStorage: MutableMap<String, String>,
    val sessionValuesCount: MutableMap<String, Int>,
)
package com.ivanovd422.transactionalkeyvaluestorage.main.data

import java.util.Stack
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject

class KeyValueStorage @Inject constructor() {

    private val storage = mutableMapOf<String, String>()
    private val valuesCount = mutableMapOf<String, Int>()
    private val transactionStack = Stack<Transaction>()

    private val rwLock = ReentrantReadWriteLock()
    private val readLock = rwLock.readLock()
    private val writeLock = rwLock.writeLock()

    fun set(key: String, value: String) {
        writeLock.lock()
        try {
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
        } finally {
            writeLock.unlock()
        }
    }

    fun get(key: String): String? {
        readLock.lock()
        try {
            return if (transactionStack.isEmpty()) {
                storage[key]
            } else {
                transactionStack.peek().sessionStorage[key]
            }
        } finally {
            readLock.unlock()
        }
    }

    fun delete(key: String): String? {
        writeLock.lock()
        try {
            if (transactionStack.isEmpty()) {
                val value = storage[key]
                if (valuesCount[value]!! > 1) {
                    valuesCount[value!!] = valuesCount[value]!! - 1
                } else {
                    valuesCount.remove(value)
                }
                return storage.remove(key)
            } else {
                transactionStack.peek().apply {
                    val value = sessionStorage[key]
                    if (sessionValuesCount[value]!! > 1) {
                        sessionValuesCount[value!!] = sessionValuesCount[value]!! - 1
                    } else {
                        sessionValuesCount.remove(value)
                    }
                    return sessionStorage.remove(key)
                }
            }
        } finally {
            writeLock.unlock()
        }
    }

    fun count(value: String): Int? {
        readLock.lock()
        try {
            return if (transactionStack.isEmpty()) {
                valuesCount[value]
            } else {
                transactionStack.peek().sessionValuesCount[value]
            }
        } finally {
            readLock.unlock()
        }
    }

    fun beginTransaction() {
        writeLock.lock()
        try {
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
        } finally {
            writeLock.unlock()
        }
    }

    fun commitTransaction() {
        writeLock.lock()
        try {
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
        } finally {
            writeLock.unlock()
        }
    }

    fun rollbackTransaction() {
        writeLock.lock()
        try {
            transactionStack.pop()
        } finally {
            writeLock.unlock()
        }
    }
}

private class Transaction(
    val sessionStorage: MutableMap<String, String>,
    val sessionValuesCount: MutableMap<String, Int>,
)
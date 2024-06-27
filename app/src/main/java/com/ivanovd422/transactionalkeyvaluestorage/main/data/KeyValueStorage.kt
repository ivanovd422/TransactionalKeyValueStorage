package com.ivanovd422.transactionalkeyvaluestorage.main.data

import java.util.Stack
import javax.inject.Inject

class KeyValueStorage @Inject constructor() {

    private val storage = mutableMapOf<String, String>()
    private val valuesCount = mutableMapOf<String, Int>()
    private val transactionStack = Stack<Transaction>()

    fun set(key: String, value: String) {
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

    fun get(key: String): String? {
        return if (transactionStack.isEmpty()) {
            storage[key]
        } else {
            transactionStack.peek().sessionStorage[key]
        }
    }

    fun delete(key: String): String? {
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
    }

    fun count(value: String): Int? {
        return if (transactionStack.isEmpty()) {
            valuesCount[value]
        } else {
            transactionStack.peek().sessionValuesCount[value]
        }
    }

    fun beginTransaction() {
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

    fun commitTransaction() {
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

    fun rollbackTransaction() {
        transactionStack.pop()
    }
}

private class Transaction(
    val sessionStorage: MutableMap<String, String>,
    val sessionValuesCount: MutableMap<String, Int>,
)
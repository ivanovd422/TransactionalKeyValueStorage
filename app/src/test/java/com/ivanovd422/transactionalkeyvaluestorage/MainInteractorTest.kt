package com.ivanovd422.transactionalkeyvaluestorage

import com.ivanovd422.transactionalkeyvaluestorage.main.data.KeyValueStorage
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.ExecutionResult
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.MainInteractor
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

class MainInteractorTest {

    private val storageMock: KeyValueStorage = mock()
    private val interactor = MainInteractor(storageMock)
    private val key = "key"
    private val value = "value"

    @Test
    fun `should return pair of key,value which were set`() {
        val result = interactor.set(key, value)

        assertTrue(result is ExecutionResult.Success)
        assertEquals(key, (result as ExecutionResult.Success).value.first)
        assertEquals(value, result.value.second)
    }

    @Test
    fun `should return error when there is no such value`() {
        whenever(storageMock.get(key)).thenReturn(null)
        val result = interactor.get(key)
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return correct value when there is such value`() {
        whenever(storageMock.get(key)).thenReturn(value)
        val result = interactor.get(key)
        assertTrue(result is ExecutionResult.Success)
        assertEquals(value, (result as ExecutionResult.Success).value)
    }

    @Test
    fun `should return error during deleting if there is no such value`() {
        whenever(storageMock.get(key)).thenReturn(null)
        val result = interactor.delete(key)
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return success with deleted value`() {
        whenever(storageMock.get(key)).thenReturn(value)
        whenever(storageMock.delete(key)).thenReturn(value)
        val result = interactor.delete(key)
        assertTrue(result is ExecutionResult.Success)
        assertEquals(value, (result as ExecutionResult.Success).value)
    }

    @Test
    fun `should return null count when there is no such value`() {
        whenever(storageMock.count(value)).thenReturn(null)
        val result = interactor.count(value)
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return correct count`() {
        whenever(storageMock.count(value)).thenReturn(10)
        val result = interactor.count(value)
        assertTrue(result is ExecutionResult.Success)
        assertEquals(10, (result as ExecutionResult.Success).value)
    }

    @Test
    fun `should always return success when begins transaction`() {
        val result = interactor.beginTransaction()
        assertTrue(result is ExecutionResult.Success)
    }

    @Test
    fun `should return error when storage throws an exception during commitment`() {
        whenever(storageMock.commitTransaction()).thenThrow(RuntimeException("Exception"))
        val result = interactor.commitTransaction()
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return success when storage begins transaction`() {
        val result = interactor.commitTransaction()
        assertTrue(result is ExecutionResult.Success)
    }

    @Test
    fun `should return error when storage throws an exception during rolling back`() {
        whenever(storageMock.rollbackTransaction()).thenThrow(RuntimeException("Exception"))
        val result = interactor.rollbackTransaction()
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return success when storage rolls back`() {
        val result = interactor.rollbackTransaction()
        assertTrue(result is ExecutionResult.Success)
    }
}
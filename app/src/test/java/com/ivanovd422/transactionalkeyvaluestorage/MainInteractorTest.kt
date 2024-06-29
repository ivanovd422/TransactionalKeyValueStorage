package com.ivanovd422.transactionalkeyvaluestorage

import com.ivanovd422.transactionalkeyvaluestorage.main.data.KeyValueStorage
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.Command
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.ExecutionResult
import com.ivanovd422.transactionalkeyvaluestorage.main.domain.MainInteractor
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking

class MainInteractorTest {

    private val storageMock: KeyValueStorage = mock()
    private val interactor = MainInteractor(storageMock)
    private val key = "key"
    private val value = "value"

    @Test
    fun `should return pair of key,value which were set`() = runBlocking {
        val result = interactor.executeCommand(Command.Set(key, value))

        assertTrue(result is ExecutionResult.Success)
    }

    @Test
    fun `should return error when there is no such value`() = runBlocking {
        whenever(storageMock.get(key)).thenReturn(null)
        val result = interactor.executeCommand(Command.Get(key))
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return correct value when there is such value`() = runBlocking {
        whenever(storageMock.get(key)).thenReturn(value)
        val result = interactor.executeCommand(Command.Get(key))
        assertTrue(result is ExecutionResult.Success)
        assertEquals(value, (result as ExecutionResult.Success).value)
    }

    @Test
    fun `should return error during deleting if there is no such value`() = runBlocking {
        whenever(storageMock.get(key)).thenReturn(null)
        val result = interactor.executeCommand(Command.Delete(key))
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return success with deleted value`() = runBlocking {
        whenever(storageMock.get(key)).thenReturn(value)
        whenever(storageMock.delete(key)).thenReturn(value)
        val result = interactor.executeCommand(Command.Delete(key))
        assertTrue(result is ExecutionResult.Success)
        assertEquals(value, (result as ExecutionResult.Success).value)
    }

    @Test
    fun `should return null count when there is no such value`() = runBlocking {
        whenever(storageMock.count(value)).thenReturn(null)
        val result = interactor.executeCommand(Command.Count(value))
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return correct count`() = runBlocking {
        whenever(storageMock.count(value)).thenReturn(10)
        val result = interactor.executeCommand(Command.Count(value))
        assertTrue(result is ExecutionResult.Success)
        assertEquals(10, (result as ExecutionResult.Success).value)
    }

    @Test
    fun `should always return success when begins transaction`() = runBlocking {
        val result = interactor.executeCommand(Command.Begin)
        assertTrue(result is ExecutionResult.Success)
    }

    @Test
    fun `should return error when storage throws an exception during commitment`() = runBlocking {
        whenever(storageMock.commitTransaction()).thenThrow(RuntimeException("Exception"))
        val result = interactor.executeCommand(Command.Commit)
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return success when storage begins transaction`() = runBlocking {
        val result = interactor.executeCommand(Command.Commit)
        assertTrue(result is ExecutionResult.Success)
    }

    @Test
    fun `should return error when storage throws an exception during rolling back`() = runBlocking {
        whenever(storageMock.rollbackTransaction()).thenThrow(RuntimeException("Exception"))
        val result = interactor.executeCommand(Command.Rollback)
        assertTrue(result is ExecutionResult.Error)
    }

    @Test
    fun `should return success when storage rolls back`() = runBlocking {
        val result = interactor.executeCommand(Command.Rollback)
        assertTrue(result is ExecutionResult.Success)
    }
}
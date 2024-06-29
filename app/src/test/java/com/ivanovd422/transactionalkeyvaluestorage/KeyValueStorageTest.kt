package com.ivanovd422.transactionalkeyvaluestorage

import com.ivanovd422.transactionalkeyvaluestorage.main.data.KeyValueStorage
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class KeyValueStorageTest {

    private val storage = KeyValueStorage()

    @Test
    fun `should return 1 count when there is only one value`() = runBlocking {
        storage.set("123", "456")
        val count = storage.count("456")
        assertEquals(1, count)
    }

    @Test
    fun `should return 1 count after several settings the same pair key and value`() = runBlocking {
        storage.set("123", "456")
        storage.set("123", "456")
        storage.set("123", "456")
        val count = storage.count("456")
        assertEquals(1, count)
    }

    @Test
    fun `should return null count when there is no value for this key`() = runBlocking {
        val count = storage.count("123")
        assertEquals(null, count)
    }

    @Test
    fun `should return 2 count when there are 2 the same keys`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "456")
        val count = storage.count("456")
        assertEquals(2, count)
    }

    @Test
    fun `should return 0 count when the value was rewritten`() = runBlocking {
        storage.set("123", "456")
        storage.set("123", "zxc")
        val count = storage.count("456")
        assertEquals(null, count)
    }

    @Test
    fun `should return null when there is no such value`() = runBlocking {
        assertEquals(null, storage.get("123"))
    }

    @Test
    fun `should return correct value when it was rewritten`() = runBlocking {
        storage.set("123", "456")
        storage.set("123", "zxc")
        assertEquals("zxc", storage.get("123"))
    }

    @Test
    fun `should return correct value during deleting`() = runBlocking {
        storage.set("123", "456")
        assertEquals("456", storage.delete("123"))
    }

    @Test
    fun `should return null after deleting`() = runBlocking {
        storage.set("123", "456")
        storage.delete("123")
        assertEquals(null, storage.get("123"))
    }

    @Test
    fun `should return correct count after setting several keys with the same value`() = runBlocking {
        storage.set("123", "456")
        storage.set("zxc", "456")
        storage.set("abc", "456")
        assertEquals(3, storage.count("456"))
    }

    @Test
    fun `should return correct count after few deleting`() = runBlocking {
        storage.set("123", "456")
        storage.set("zxc", "456")
        storage.set("abc", "456")
        storage.delete("123")
        assertEquals(2, storage.count("456"))
    }

    @Test
    fun `should return null count after deleting all items`() = runBlocking {
        storage.set("123", "456")
        storage.set("zxc", "456")
        storage.set("abc", "456")
        storage.delete("123")
        storage.delete("zxc")
        storage.delete("abc")
        assertEquals(null, storage.count("456"))
    }

    // Transaction
    @Test
    fun `should return the same state after begging transaction`() = runBlocking {
        storage.set("123", "456")
        storage.beginTransaction()
        assertEquals("456", storage.get("123"))
    }

    @Test
    fun `should return the same state after transaction simple rollback`() = runBlocking {
        storage.set("123", "456")
        storage.beginTransaction()
        storage.rollbackTransaction()
        assertEquals("456", storage.get("123"))
    }

    @Test
    fun `should return the same state after transaction simple commit`() = runBlocking {
        storage.set("123", "456")
        storage.beginTransaction()
        storage.commitTransaction()
        assertEquals("456", storage.get("123"))
    }

    @Test
    fun `should return updated state after deleting with committed transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "zxc")
        storage.beginTransaction()
        storage.delete("123")
        storage.commitTransaction()
        assertEquals(null, storage.get("123"))
        assertEquals("zxc", storage.get("abc"))
    }

    @Test
    fun `should return old state after deleting with rolled back transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "zxc")
        storage.beginTransaction()
        storage.delete("123")
        storage.rollbackTransaction()
        assertEquals("456", storage.get("123"))
        assertEquals("zxc", storage.get("abc"))
    }

    @Test
    fun `should return correct count after beginning transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "456")
        storage.beginTransaction()
        assertEquals(2, storage.count("456"))
    }

    @Test
    fun `should return updated count after changes with beginning transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "456")
        storage.beginTransaction()
        storage.delete("abc")
        assertEquals(1, storage.count("456"))
    }

    @Test
    fun `should return old count after rolled back transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "456")
        storage.beginTransaction()
        storage.delete("abc")
        storage.rollbackTransaction()
        assertEquals(2, storage.count("456"))
    }

    @Test
    fun `should return new count after commitment transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "456")
        storage.beginTransaction()
        storage.delete("abc")
        storage.delete("123")
        storage.commitTransaction()
        assertEquals(null, storage.count("456"))
    }

    @Test
    fun `should return correct count after beginning transaction and changing the value`() = runBlocking {
        storage.set("foo", "123")
        storage.set("bar", "123")
        storage.set("zxc", "123")
        storage.beginTransaction()
        storage.set("foo", "456")
        assertEquals(2, storage.count("123"))
    }

    // Nested Transaction
    @Test
    fun `should return the same state after rolled back nested transactions`() = runBlocking {
        storage.set("123", "456")
        storage.set("abc", "zxc")
        storage.beginTransaction()
        storage.beginTransaction()
        storage.rollbackTransaction()
        storage.rollbackTransaction()
        assertEquals("456", storage.get("123"))
        assertEquals("zxc", storage.get("abc"))
    }

    @Test
    fun `should show updated state after nested transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("foo", "zxc")
        storage.set("bar", "zxc")
        storage.beginTransaction()
        storage.beginTransaction()

        storage.set("123", "777")
        storage.delete("bar")
        storage.set("foo", "bar")

        storage.commitTransaction()
        assertEquals("777", storage.get("123"))
        assertEquals("bar", storage.get("foo"))
        assertEquals(null, storage.get("bar"))
    }

    @Test
    fun `should show old state after commitment of child transaction and rolled back parent transaction`() = runBlocking {
        storage.set("123", "456")
        storage.set("foo", "zxc")
        storage.set("bar", "zxc")
        storage.beginTransaction()
        storage.beginTransaction()

        storage.set("123", "777")
        storage.delete("bar")
        storage.set("foo", "bar")

        storage.commitTransaction()
        storage.rollbackTransaction()
        assertEquals("456", storage.get("123"))
        assertEquals("zxc", storage.get("foo"))
        assertEquals("zxc", storage.get("bar"))
    }
}
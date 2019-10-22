package at.florianschuster.control.util

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObjectStoreTest {
    private val testObjectKey = "object"
    private val testObject = TestObject()

    @Test
    fun `set associated object in store and retrieve`() {
        val store = Store()

        store.associatedObject(testObjectKey) { testObject }

        val retrievedTestObject = store.associatedObject<TestObject>(testObjectKey)
        assertTrue { testObject == retrievedTestObject }

        val secondTestObject = TestObject()
        val againRetrievedTestObject = store.associatedObject(testObjectKey) { secondTestObject }
        assertTrue { testObject == againRetrievedTestObject }
        assertFalse { secondTestObject == againRetrievedTestObject }
    }

    @Test
    fun `clear store`() {
        val store = Store()

        store.associatedObject(testObjectKey) { testObject }
        store.clearAssociatedObjects()

        val retrievedTestObject = store.associatedObject<TestObject>(testObjectKey)
        assertTrue { retrievedTestObject == null }
    }

    private class Store : ObjectStore
    class TestObject
}

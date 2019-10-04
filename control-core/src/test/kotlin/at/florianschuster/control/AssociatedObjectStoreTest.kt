package at.florianschuster.control

import org.junit.Test

class AssociatedObjectStoreTest {
    private val testObjectKey = "object"
    private val testObject = TestObject()

    @Test
    fun `set associated object in store and retrieve`() {
        val store = Store()

        store.associatedObject(testObjectKey) { testObject }

        val retrievedTestObject = store.associatedObject<TestObject>(testObjectKey)
        assert(testObject == retrievedTestObject)
    }

    @Test
    fun `clear store`() {
        val store = Store()

        store.associatedObject(testObjectKey) { testObject }
        store.clearAssociatedObjects()

        val retrievedTestObject = store.associatedObject<TestObject>(testObjectKey)
        assert(retrievedTestObject == null)
    }
}

private class Store : AssociatedObjectStore
private data class TestObject(val element: Int = 123)
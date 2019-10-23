package at.florianschuster.control.util

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssociatedObjectTest {
    private val testObject = StoredObject()

    @Test
    fun `set associated object and retrieve`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { testObject }

        val retrievedTestObject = associatedObject.valueFor<StoredObject>(container)
        assertTrue { testObject == retrievedTestObject }
    }

    @Test
    fun `valueFor does not overwrite`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { testObject }

        val secondTestObject = StoredObject()
        val againRetrievedTestObject = associatedObject.valueFor(container) { secondTestObject }
        assertTrue { testObject == againRetrievedTestObject }
        assertFalse { secondTestObject == againRetrievedTestObject }
    }

    @Test
    fun `clear associated object`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { testObject }
        associatedObject.clearFor(container)

        val retrievedTestObject = associatedObject.valueFor<StoredObject>(container)
        assertTrue { retrievedTestObject == null }
    }

    @Test
    fun `associated object threading test`() {
        // todo
    }

    private class StoredObject
}

package at.florianschuster.control.util

import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class AssociatedObjectTest {
    private val testObject = StoredObject()

    @Test
    fun `set associated object and retrieve`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { testObject }

        val retrievedTestObject = associatedObject.valueFor<StoredObject>(container)
        assertSame(retrievedTestObject, testObject)
    }

    @Test
    fun `valueFor does not overwrite`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { testObject }

        val secondTestObject = StoredObject()
        val retrievedTestObject = associatedObject.valueFor(container) { secondTestObject }
        assertSame(retrievedTestObject, testObject)
    }

    @Test
    fun `clear associated object`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { testObject }
        associatedObject.clearFor(container)

        val retrievedTestObject = associatedObject.valueFor<StoredObject>(container)
        assertNull(retrievedTestObject)
    }

    @Test
    fun `store null value`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueFor(container) { null }

        val retrievedTestObject = associatedObject.valueFor<Any>(container)
        assertNull(retrievedTestObject)
    }

    private class StoredObject
}

package at.florianschuster.control.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class AssociatedObjectTest {
    private val testObject = StoredObject()

    @Test
    fun `set associated object and retrieve`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueForOrCreate(container) { testObject }

        val retrievedTestObject = associatedObject.valueFor<StoredObject>(container)
        assertSame(retrievedTestObject, testObject)
    }

    @Test
    fun `valueFor does not overwrite`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueForOrCreate(container) { testObject }

        val secondTestObject = StoredObject()
        val retrievedTestObject = associatedObject.valueForOrCreate(container) { secondTestObject }
        assertSame(retrievedTestObject, testObject)
    }

    @Test
    fun `clear associated object`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueForOrCreate(container) { testObject }
        associatedObject.clearFor(container)

        val retrievedTestObject = associatedObject.valueFor<StoredObject>(container)
        assertNull(retrievedTestObject)
    }

    @Test
    fun `store null value`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.valueForOrCreate(container) { null }

        val retrievedTestObject = associatedObject.valueFor<Any>(container)
        assertNull(retrievedTestObject)
    }

    @Test
    fun `set value does overwrite`() {
        val container = Any()
        val associatedObject = AssociatedObject()

        associatedObject.setValue(container, 14)
        assertEquals(14, associatedObject.valueFor<Int>(container))

        associatedObject.setValue(container, -24)
        assertEquals(-24, associatedObject.valueFor<Int>(container))
    }

    private class StoredObject
}

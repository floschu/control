package at.florianschuster.control

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DefaultControllerTagTestJvm {

    @Test
    fun `defaultControllerTag in object`() {
        assertEquals(EXPECTED_TAG, TestObject.tag)
    }

    @Test
    fun `defaultControllerTag in class`() {
        assertEquals(EXPECTED_TAG, TestClass().tag)
    }

    @Test
    fun `defaultControllerTag in anonymous object`() {
        val sut = object {
            val tag = defaultControllerTag()
        }
        assertEquals(EXPECTED_TAG, sut.tag)
    }

    companion object {
        private const val EXPECTED_TAG = "DefaultControllerTagTestJvm"
    }
}

private object TestObject {
    val tag = defaultControllerTag()
}

private class TestClass {
    val tag = defaultControllerTag()
}
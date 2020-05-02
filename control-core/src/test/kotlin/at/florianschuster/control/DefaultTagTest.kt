package at.florianschuster.control

import org.junit.Test
import kotlin.test.assertEquals

internal class DefaultTagTest {

    @Test
    fun `defaultTag in object`() {
        assertEquals(expectedTag, TestObject.tag)
    }

    @Test
    fun `defaultTag in class`() {
        assertEquals(expectedTag, TestClass().tag)
    }

    @Test
    fun `defaultTag in anonymous class`() {
        val sut = object {
            val tag = defaultTag()
        }
        assertEquals(expectedTag, sut.tag)
    }

    companion object {
        private const val expectedTag = "DefaultTagTest"
    }
}

private object TestObject {
    val tag = defaultTag()
}

private class TestClass {
    val tag = defaultTag()
}
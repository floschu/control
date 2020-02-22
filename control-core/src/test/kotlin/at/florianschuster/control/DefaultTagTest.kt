package at.florianschuster.control

import org.junit.Test
import kotlin.test.assertEquals

internal class DefaultTagTest {

    @Test
    fun `tag in object`() {
        assertEquals(expectedTag, TestObject.tag)
    }

    @Test
    fun `tag in class`() {
        assertEquals(expectedTag, TestClass().tag)
    }

    @Test
    fun `tag in anonymous class`() {
        val obj = object {
            val tag: String get() = defaultTag("")
        }
        assertEquals(expectedTag, obj.tag)
    }

    companion object {
        private const val expectedTag = "DefaultTagTest"
    }
}

private object TestObject {
    val tag: String get() = defaultTag("")
}

private class TestClass {
    val tag: String get() = defaultTag("")
}
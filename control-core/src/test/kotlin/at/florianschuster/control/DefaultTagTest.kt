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
            val tag = defaultTag("")
        }
        assertEquals(expectedTag, sut.tag)
    }

    @Test
    fun `suffix in defaultTag`() {
        val expectedSuffix = "suffix"
        val sut = object {
            val tag = defaultTag(expectedSuffix)
        }
        assertEquals(expectedTag + expectedSuffix, sut.tag)
    }

    @Test
    fun `default suffix in defaultTag`() {
        val sut = object {
            val tag = defaultTag()
        }
        assertEquals(expectedTag + defaultTagSuffix, sut.tag)
    }

    companion object {
        private const val expectedTag = "DefaultTagTest"
    }
}

private object TestObject {
    val tag = defaultTag("")
}

private class TestClass {
    val tag = defaultTag("")
}
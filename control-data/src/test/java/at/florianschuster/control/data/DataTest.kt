package at.florianschuster.control.data

import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DataTest {

    @Test
    fun `success data from factory`() {
        val value = 123
        val data = Data { value }

        assertEquals(Data.Success(value), data)
    }

    @Test
    fun `failure data from factory`() {
        val exception = IOException()
        val data = Data { throw exception }

        assertEquals(Data.Failure(exception), data)
    }

    @Test
    fun `invoke only contains element at success`() {
        assertNull(Data.Uninitialized.invoke())
        assertNull(Data.Loading.invoke())
        assertNull(Data.Failure(Error()).invoke())
        assertNotNull(Data.Success(1).invoke())
    }
}
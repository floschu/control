package at.florianschuster.control

import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

internal class LogConfigurationTest {

    @Test
    fun `setting default log configuration`() {
        LogConfiguration.DEFAULT
        assertEquals(LogConfiguration.None, LogConfiguration.DEFAULT)
        LogConfiguration.DEFAULT = LogConfiguration.Simple("tag")
        assertEquals(LogConfiguration.Simple("tag"), LogConfiguration.DEFAULT)
    }

    @Test
    fun `none log methods are not called`() {
        val noneLogConfiguration = spyk(LogConfiguration.None)
        noneLogConfiguration.log(function, message)
        noneLogConfiguration.log(function, exception)
        verify(exactly = 0) { noneLogConfiguration.createMessage(any(), any(), any()) }
    }

    @Test
    fun `simple log methods are called`() {
        val simpleLogConfiguration = spyk(LogConfiguration.Simple("test"))
        simpleLogConfiguration.log(function, message)
        simpleLogConfiguration.log(function, exception)
        verify(exactly = 2) { simpleLogConfiguration.createMessage(any(), any(), any()) }
    }

    @Test
    fun `elaborate log methods are called`() {
        val elaborateLogConfiguration = spyk(LogConfiguration.Elaborate("test"))
        elaborateLogConfiguration.log(function, message)
        elaborateLogConfiguration.log(function, exception)
        verify(exactly = 2) { elaborateLogConfiguration.createMessage(any(), any(), any()) }
    }

    @Test
    fun `custom log methods are called`() {
        val customLogs = mutableListOf<String>()
        val customLogConfiguration = LogConfiguration.Custom("test") { customLogs.add(it) }
        customLogConfiguration.log(function, message)
        customLogConfiguration.log(function, exception)
        assertEquals(2, customLogs.count())
    }

    companion object {
        private val function = "function"
        private val exception = IOException("test")
        private val message = "$exception"
    }
}
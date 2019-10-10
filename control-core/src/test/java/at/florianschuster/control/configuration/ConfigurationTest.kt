package at.florianschuster.control.configuration

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ConfigurationTest {

    private val loggedErrors = mutableListOf<Throwable>()
    private val loggedOperations = mutableListOf<String>()

    @Before
    fun setup() {
        loggedErrors.clear()
        loggedOperations.clear()

        configureControl {
            errors { loggedErrors.add(it) }
            operations { loggedOperations.add(it) }
        }
    }

    @Test
    fun `configurations are set correctly`() {
        assert(Control.configuration.errorLogger != null)
        assert(Control.configuration.operationLogger != null)
    }

    @Test
    fun `error logging works correctly`() {
        val illegalStateException = IllegalStateException()
        Control.log(illegalStateException)
        assertEquals(loggedErrors, listOf<Throwable>(illegalStateException))

        val illegalArgumentException = IllegalArgumentException()
        Control.log(illegalArgumentException)
        assertEquals(
            loggedErrors,
            listOf<Throwable>(illegalStateException, illegalArgumentException)
        )
    }

    @Test
    fun `operations logging works correctly`() {
        assert(loggedOperations == listOf(Operation.ControlConfigured.toString()))

        val testOperation = Operation.Destroyed("test")
        Control.log { testOperation }
        assert(
            loggedOperations == listOf(
                Operation.ControlConfigured.toString(),
                testOperation.toString()
            )
        )
    }
}

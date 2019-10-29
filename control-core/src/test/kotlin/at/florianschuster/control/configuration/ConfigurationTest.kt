package at.florianschuster.control.configuration

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ConfigurationTest {

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
        assertNotNull(Control.configuration.errorLogger)
        assertNotNull(Control.configuration.operationLogger)
    }

    @Test
    fun `error logging works correctly`() {
        val illegalStateException = IllegalStateException()
        Control.log(illegalStateException)

        assertEquals(listOf<Throwable>(illegalStateException), loggedErrors)

        val illegalArgumentException = IllegalArgumentException()
        Control.log(illegalArgumentException)

        assertEquals(
            listOf<Throwable>(illegalStateException, illegalArgumentException),
            loggedErrors
        )
    }

    @Test
    fun `operations logging works correctly`() {
        assertEquals(listOf(Operation.ControlConfigured.toString()), loggedOperations)

        val testOperation = Operation.Canceled("test")
        Control.log { testOperation }

        assertEquals(
            listOf(
                Operation.ControlConfigured.toString(),
                testOperation.toString()
            ),
            loggedOperations
        )
    }
}

package at.florianschuster.control.configuration

import org.junit.Before
import org.junit.Test

class ControlConfigTest {
    private val defaultEscalateCrashes = false
    private val loggedErrors = arrayListOf<Throwable>()
    private val loggedOperations = arrayListOf<String>()

    @Before
    fun setup() {
        loggedErrors.clear()
        loggedOperations.clear()

        configureControl {
            crashes(escalate = defaultEscalateCrashes)
            errorLogger { loggedErrors.add(it) }
            operationLogger { loggedOperations.add(it) }
        }
    }

    @Test
    fun `configurations are set correctly`() {
        assert(ControlConfig.configuration.errorLogger != null)
        assert(ControlConfig.configuration.operationLogger != null)
        assert(defaultEscalateCrashes == ControlConfig.configuration.escalateCrashes)
    }

    @Test
    fun `error logging works correctly`() {
        val illegalStateException = IllegalStateException()
        ControlConfig.handleError(illegalStateException)
        assert(loggedErrors == listOf(illegalStateException))

        val illegalArgumentException = IllegalArgumentException()
        ControlConfig.handleError(illegalArgumentException)
        assert(loggedErrors == listOf(illegalStateException, illegalArgumentException))
    }

    @Test
    fun `operations logging works correctly`() {
        assert(loggedOperations == listOf(Operation.ControlConfigured.toString()))

        val testOperation = Operation.Destroyed("test")
        ControlConfig.log { testOperation }
        assert(loggedOperations == listOf(Operation.ControlConfigured.toString(), testOperation.toString()))
    }

}

package at.florianschuster.control

import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals

internal class StoreLoggerTest { // todo

    // @Test
    // fun `setting default log configuration`() {
    //     ControlLogConfiguration.default = ControlLogConfiguration.Println(tag)
    //     assertEquals(ControlLogConfiguration.Println(tag), ControlLogConfiguration.default)
    //
    //     ControlLogConfiguration.default = ControlLogConfiguration.None
    //     assertEquals(ControlLogConfiguration.None, ControlLogConfiguration.default)
    // }
    //
    // @Test
    // fun `none log, methods are not called`() {
    //     val noneLogConfiguration = spyk(ControlLogConfiguration.None)
    //     noneLogConfiguration.log(function, message)
    //     noneLogConfiguration.log(function, exception)
    //     verify(exactly = 0) { noneLogConfiguration.createMessage(any(), any(), any()) }
    // }
    //
    // @Test
    // fun `elaborate log, methods are called`() {
    //     val elaborateLogConfiguration = spyk(ControlLogConfiguration.Println(tag))
    //     elaborateLogConfiguration.log(function, message)
    //     elaborateLogConfiguration.log(function, exception)
    //     verify(exactly = 2) { elaborateLogConfiguration.createMessage(any(), any(), any()) }
    // }
    //
    // @Test
    // fun `custom log, all variations, methods are called`() {
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = true
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = false
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = true,
    //         operations = { }
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = false,
    //         operations = { }
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = true,
    //         errors = { }
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = false,
    //         errors = { }
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = true,
    //         operations = { },
    //         errors = { }
    //     ).test()
    //
    //     ControlLogConfiguration.Custom(
    //         tag,
    //         elaborate = false,
    //         operations = { },
    //         errors = { }
    //     ).test()
    // }
    //
    // private fun ControlLogConfiguration.Custom.test() {
    //     val spiedOperations = if (operations == null) null else spyk<(String) -> Unit>()
    //     val spiedErrors = if (errors == null) null else spyk<(Throwable) -> Unit>()
    //     val spiedLogConfiguration = spyk(
    //         ControlLogConfiguration.Custom(
    //             tag,
    //             elaborate,
    //             spiedOperations,
    //             spiedErrors
    //         ),
    //         recordPrivateCalls = true
    //     )
    //
    //     spiedLogConfiguration.log(function, message)
    //     if (spiedOperations != null) {
    //         verify(exactly = 1) { spiedOperations(any()) }
    //         verify(exactly = 1) { spiedLogConfiguration.createMessage(tag, function, any()) }
    //     }
    //
    //     spiedLogConfiguration.log(function, exception)
    //     if (spiedErrors != null) {
    //         verify(exactly = 1) { spiedErrors(exception) }
    //     } else if (spiedOperations != null) {
    //         verify(exactly = 2) { spiedOperations(any()) }
    //         verify(exactly = 2) { spiedLogConfiguration.createMessage(tag, function, any()) }
    //     }
    // }

    companion object {
        private const val tag = "TestTag"
        private const val function = "TestFunction"
        private val exception = IllegalStateException("Test")
        private val message = exception.toString()
    }
}
package at.florianschuster.control.kotlincounter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.system.exitProcess

private const val AvailableCommands = "available commands -> + , - , exit"

// todo why is controller not started on first dispatch
internal fun main() {
    println("🎛 <control-counter>")
    println("$AvailableCommands\n")

    val scope = CoroutineScope(Dispatchers.IO)
    val controller = scope.createCounterController()

    while (true) {
        when (readLine()) {
            "+" -> controller.dispatch(CounterAction.Increment)
            "-" -> controller.dispatch(CounterAction.Decrement)
            "exit" -> {
                scope.cancel()
                exitProcess(0)
            }
            else -> println(AvailableCommands)
        }
    }
}
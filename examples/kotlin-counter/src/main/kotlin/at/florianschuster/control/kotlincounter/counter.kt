package at.florianschuster.control.kotlincounter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.system.exitProcess

private const val AvailableCommands = "available commands -> + , - , exit"

internal fun main(args: Array<String>) {
    println("🎛 <control-counter>")
    println("$AvailableCommands\n")

    val scope = CoroutineScope(Dispatchers.Unconfined)
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
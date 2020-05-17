package at.florianschuster.control.kotlincounter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.system.exitProcess

private const val AvailableCommands = "available commands -> + , - , exit"

/**
 * small counter example in which you can increment (with +) and decrement (with -) a value.
 * the state of the [CounterController] is printed via [println].
 */
internal fun main(args : Array<String>) {
    println("🎛 <control-counter>")
    println("$AvailableCommands\n")

    val controller = CoroutineScope(Dispatchers.Unconfined).createCounterController()

    while (true) {
        when (readLine()) {
            "+" -> controller.dispatch(CounterAction.Increment)
            "-" -> controller.dispatch(CounterAction.Decrement)
            "exit" -> exitProcess(0)
            else -> println(AvailableCommands)
        }
    }
}
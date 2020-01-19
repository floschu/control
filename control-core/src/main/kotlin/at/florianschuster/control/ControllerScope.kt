package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Default [CoroutineScope] used in [Controller].
 */
@Suppress("FunctionName")
fun ControllerScope(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): CoroutineScope = object : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher
}

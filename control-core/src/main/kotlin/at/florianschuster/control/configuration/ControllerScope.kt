package at.florianschuster.control.configuration

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class ControllerScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
}

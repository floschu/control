package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * See [Controller.cancel] for more details.
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <State> ControllerDelegate<*, State>.cancel() = controller.cancel()
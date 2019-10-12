package at.florianschuster.control

import at.florianschuster.control.configuration.Control
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll

/**
 * This processor acts as a [Flow] through [AbstractFlow] and accepts values via [invoke].
 * Support multiple collectors.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class ActionProcessor<T> : AbstractFlow<T>(), (T) -> Unit {
    private val channel: BroadcastChannel<T> = BroadcastChannel(1)

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        collector.emitAll(channel.openSubscription())
    }

    override fun invoke(value: T) {
        try {
            channel.offer(value)
        } catch (e: ClosedSendChannelException) {
            Control.log(e)
        }
    }
}
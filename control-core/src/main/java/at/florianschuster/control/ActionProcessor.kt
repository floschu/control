package at.florianschuster.control

import at.florianschuster.control.configuration.Control
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll

/**
 * This processor acts as a [Flow] through [AbstractFlow], resembling something like a Relay.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class ActionProcessor<T> : AbstractFlow<T>(), (T) -> Unit {
    private val channel: BroadcastChannel<T> = BroadcastChannel(1)

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        collector.emitAll(channel.asFlow())
    }

    override fun invoke(value: T) {
        try {
            channel.offer(value)
        } catch (e: ClosedSendChannelException) {
            Control.log(e)
        }
    }

    fun cancel() {
        channel.cancel()
    }
}
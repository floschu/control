package at.florianschuster.test

import at.florianschuster.test.configuration.Control
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This processor acts as a [Flow] through [AbstractFlow] and accepts values via [invoke].
 * Supports only one collector.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class EventProcessor<T> : AbstractFlow<T>(), (T) -> Unit {
    private val channel: BroadcastChannel<T> = BroadcastChannel(1)
    private val collected = AtomicBoolean(false)

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        check(!collected.get()) { "Only one collector allowed." }
        collected.set(true)
        collector.emitAll(channel.openSubscription())
    }

    override fun invoke(value: T) {
        try {
            channel.offer(value)
        } catch (e: CancellationException) {
            Control.log(e)
        }
    }
}
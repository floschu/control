package at.florianschuster.control

import at.florianschuster.control.util.safeOffer
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
 *
 * Supports only one collector if [singleCollector] is true.
 * If [singleCollector] is false [PublishProcessor] will throw an [IllegalStateException] if more
 * than one collectors try to collect the output flow.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class PublishProcessor<T>(
    private val singleCollector: Boolean = false
) : AbstractFlow<T>(), (T) -> Unit {
    private val channel: BroadcastChannel<T> = BroadcastChannel(1)
    private val collected = AtomicBoolean(false)

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        if (singleCollector) {
            check(!collected.get()) { "Only one collector allowed." }
            collected.set(true)
        }
        collector.emitAll(channel.openSubscription())
    }

    override fun invoke(value: T) {
        channel.safeOffer(value)
    }
}
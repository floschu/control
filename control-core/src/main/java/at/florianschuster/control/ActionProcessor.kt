package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll

@ExperimentalCoroutinesApi
@FlowPreview
class ActionProcessor<T> : AbstractFlow<T>(), FlowCollector<T> {
    private val channel: BroadcastChannel<T> = BroadcastChannel(1)

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        collector.emitAll(channel.asFlow())
    }

    override suspend fun emit(value: T) {
        channel.send(value)
    }

    fun offer(value: T): Boolean {
        return channel.offer(value)
    }
}
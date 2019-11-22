package at.florianschuster.control.util

import at.florianschuster.control.configuration.Control
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel

@ExperimentalCoroutinesApi
internal fun <T> SendChannel<T>.safeOffer(value: T) {
    if (isClosedForSend) return
    try {
        offer(value)
    } catch (e: CancellationException) {
        Control.log(e)
    }
}
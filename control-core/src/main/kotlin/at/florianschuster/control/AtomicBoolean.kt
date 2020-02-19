package at.florianschuster.control

import kotlin.reflect.KProperty

/**
 * Todo this only works on JVM...
 */
internal class AtomicBoolean(private var value: Boolean) {

    operator fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return synchronized(thisRef) { value }
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        synchronized(thisRef) { this.value = value }
    }
}
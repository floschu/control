package at.florianschuster.control.util

import java.util.WeakHashMap

internal class AssociatedObject {
    private val store = WeakHashMap<Any, Any>()

    fun <Value> valueFor(key: Any): Value? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as? Value
    }

    fun <Value> valueForOrCreate(key: Any, creator: () -> Value): Value {
        return valueFor(key) ?: creator().also { store[key] = it }
    }

    fun <Value> setValue(key: Any, value: Value) {
        store[key] = value
    }

    fun clearFor(key: Any) {
        store.remove(key)
    }
}
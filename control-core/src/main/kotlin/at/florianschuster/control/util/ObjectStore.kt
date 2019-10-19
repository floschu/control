package at.florianschuster.control.util

import at.florianschuster.control.Controller

/**
 * Used to store objects of [Controller].
 * Do not use this outside of the library unless you know what you are doing.
 */
interface ObjectStore {

    private val store: HashMap<String, Any>
        get() {
            val currentStore: HashMap<String, Any>? = stores[this]
            return if (currentStore != null) currentStore
            else {
                val store: HashMap<String, Any> = HashMap()
                stores[this] = store
                store
            }
        }

    /**
     * Retrieves the value from the [store] corresponding to the [key] if it exists.
     */
    fun <T> associatedObject(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as? T
    }

    /**
     * Retrieves the value from the [store] corresponding to the [key] if it exists.
     * If it does not exist, the value is created with the [creator] and stored in the [store].
     */
    fun <T> associatedObject(key: String, creator: () -> T): T {
        val obj: T? = associatedObject<T>(key)
        return if (obj != null) obj
        else {
            val defaultObj: T = creator()
            store[key] = defaultObj as Any
            defaultObj
        }
    }

    /**
     * Clears the [store].
     */
    fun clearAssociatedObjects() {
        stores[this]?.clear()
        stores.remove(this)
    }

    companion object {
        private val stores: HashMap<Any, HashMap<String, Any>> = hashMapOf()
    }
}

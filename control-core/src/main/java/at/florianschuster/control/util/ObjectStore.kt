package at.florianschuster.control.util

/**
 * Used to store objects of [Controller].
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

    fun <T> associatedObject(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as? T
    }

    fun <T> associatedObject(key: String, default: () -> T): T {
        val obj: T? = associatedObject<T>(key)
        return if (obj != null) obj
        else {
            val defaultObj: T = default()
            store[key] = defaultObj as Any
            defaultObj
        }
    }

    fun clearAssociatedObjects() {
        stores[this]?.clear()
        stores.remove(this)
    }

    companion object {
        private val stores: HashMap<Any, HashMap<String, Any>> = hashMapOf()
    }
}

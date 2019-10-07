package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

suspend fun <T> Flow<T>.test(scope: CoroutineScope): List<T> {
    val list = arrayListOf<T>()
    scope.launch { collect { list.add(it) } }
    return list
}
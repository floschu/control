package at.florianschuster.control.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope

@ExperimentalCoroutinesApi
fun <T> Flow<T>.test(scope: TestCoroutineScope): List<T> {
    val values = mutableListOf<T>()
    scope.launch { collect { values.add(it) } }
    return values
}
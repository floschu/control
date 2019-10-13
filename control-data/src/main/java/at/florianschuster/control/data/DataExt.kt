package at.florianschuster.control.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

/**
 * Maps a [Flow] to a [Flow] of [Data]. Throwables are mapped to [Data.Failure]
 * and normal emissions are mapped to [Data.Success].
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.asDataFlow(): Flow<Data<T>> =
    map { Data.Success(it) }.catch<Data<T>> { e -> emit(Data.Failure(e)) }

/**
 * Filters [Flow] of [Data] to only emit [Data.Success] type and maps to
 * [Data.Success.value].
 */
fun <T> Flow<Data<T>>.filterSuccessData(): Flow<T> =
    filterIsInstance<Data.Success<T>>().map { it.value }

/**
 * Filters [Flow] of [Data] to only emit [Data.Failure] type and maps to
 * [Data.Failure.error].
 */
fun <T> Flow<Data<T>>.filterFailureData(): Flow<Throwable> =
    filterIsInstance<Data.Failure>().map { it.error }
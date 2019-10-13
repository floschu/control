package at.florianschuster.control.data

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

class DataExtTest {

    @Test
    fun `data flow extension converts correctly`() = runBlockingTest {
        val error = Exception()
        val dataList = (1 until 10).asFlow()
            .map {
                if (it == 5) throw error
                it
            }
            .asDataFlow()
            .toList()

        assertEquals(
            listOf(
                Data.Success(1),
                Data.Success(2),
                Data.Success(3),
                Data.Success(4),
                Data.Failure(error)
            ),
            dataList
        )
    }

    @Test
    fun `filterSuccessData only filters success`() = runBlockingTest {
        val dataList = flow {
            emit(Data.Success(Unit))
            emit(Data.Success(Unit))
            emit(Data.Success(Unit))
            emit(Data.Failure(Exception()))
            emit(Data.Success(Unit))
        }.filterSuccessData().toList()

        assertEquals(listOf(Unit, Unit, Unit, Unit), dataList)
    }

    @Test
    fun `filterFailureData only filters failure`() = runBlockingTest {
        val error = Exception()
        val dataList = flow {
            emit(Data.Success(Unit))
            emit(Data.Success(Unit))
            emit(Data.Success(Unit))
            emit(Data.Failure(error))
            emit(Data.Success(Unit))
        }.filterFailureData().toList()

        assertEquals(listOf(error), dataList)
    }
}
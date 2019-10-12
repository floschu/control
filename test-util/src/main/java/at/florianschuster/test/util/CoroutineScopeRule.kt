package at.florianschuster.test.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class CoroutineScopeRule : TestWatcher(), TestCoroutineScope by TestCoroutineScope() {

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
    }
}
package util

import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestCoroutineScopeRule : TestWatcher(), TestCoroutineScope by TestCoroutineScope() {

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
    }
}
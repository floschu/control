package at.florianschuster.control.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Rule that is a [TestCoroutineScope].
 * Coroutine's launched in [TestCoroutineScopeRule] are auto canceled after the test completes.
 */
@ExperimentalCoroutinesApi
class TestCoroutineScopeRule(
    val mainDispatcher: CoroutineDispatcher? = null
) : TestRule, TestCoroutineScope by TestCoroutineScope() {

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                if (mainDispatcher != null) Dispatchers.setMain(mainDispatcher)
                base.evaluate()
                cleanupTestCoroutines()
                if (mainDispatcher != null) Dispatchers.resetMain()
            }
        }
}
package at.florianschuster.control.util

import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CoroutineTestRuleScope : TestRule, TestCoroutineScope by TestCoroutineScope() {

    override fun apply(
        base: Statement?,
        description: Description?
    ): Statement = object : Statement() {
        override fun evaluate() {
            base?.evaluate()
            cleanupTestCoroutines()
        }
    }
}
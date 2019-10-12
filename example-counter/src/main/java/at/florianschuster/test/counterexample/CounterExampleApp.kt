package at.florianschuster.test.counterexample

import android.app.Application
import at.florianschuster.test.configuration.configureControl

class CounterExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureControl {
            errors { println("Control Error: $it") }
            operations(loggingEnabled = true, logger = ::println)
        }
    }
}

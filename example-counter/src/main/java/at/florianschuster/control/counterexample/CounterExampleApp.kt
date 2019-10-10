package at.florianschuster.control.counterexample

import android.app.Application
import at.florianschuster.control.configuration.configureControl

class CounterExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureControl {
            errors(escalateErrors = BuildConfig.DEBUG, logger = { println("Control Error: $it") })
            operations(loggingEnabled = true, logger = ::println)
        }
    }
}

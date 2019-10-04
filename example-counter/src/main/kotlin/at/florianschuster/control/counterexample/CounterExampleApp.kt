package at.florianschuster.control.counterexample

import android.app.Application
import at.florianschuster.control.configuration.configureControl

class CounterExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureControl {
            crashes(escalate = BuildConfig.DEBUG)
            operationLogger(logger = ::println)
            errorLogger(logger = { println("Error: $it") })
        }
    }
}

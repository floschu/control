package at.florianschuster.control.githubexample

import android.app.Application
import at.florianschuster.control.configuration.configureControl

class GithubExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureControl {
            errors(escalateErrors = BuildConfig.DEBUG, logger = { print("Control Error: $it") })
            operations(loggingEnabled = true, logger = ::println)
        }
    }
}

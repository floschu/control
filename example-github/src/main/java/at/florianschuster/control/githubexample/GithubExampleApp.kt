package at.florianschuster.control.githubexample

import android.app.Application
import at.florianschuster.control.configuration.configureControl

class GithubExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureControl {
            crashes(escalate = BuildConfig.DEBUG)
            operationLogger(logger = ::println)
            errorLogger(logger = { print("Error: $it") })
        }
    }
}

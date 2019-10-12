package at.florianschuster.test.githubexample

import android.app.Application
import at.florianschuster.test.configuration.configureControl

class GithubExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureControl {
            errors { println("Control Error: $it") }
            operations(loggingEnabled = true, logger = ::println)
        }
    }
}

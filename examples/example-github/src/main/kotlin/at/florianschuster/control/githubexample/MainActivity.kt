package at.florianschuster.control.githubexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.florianschuster.control.githubexample.search.GithubView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) return
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, GithubView())
            .commit()
    }
}
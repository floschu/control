package at.florianschuster.control.githubexample

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import at.florianschuster.control.githubexample.search.GithubView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val containerId = 123
        setContentView(FrameLayout(this).apply { id = containerId })
        supportFragmentManager.beginTransaction().replace(containerId, GithubView()).commit()
    }
}
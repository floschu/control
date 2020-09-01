package at.florianschuster.control.androidgithub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import at.florianschuster.control.androidgithub.search.SearchView

internal class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(android.R.id.content, SearchView())
            }
        }
    }
}
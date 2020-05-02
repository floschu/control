package at.florianschuster.control.counterexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

internal class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) return
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, CounterView())
            .commit()
    }
}
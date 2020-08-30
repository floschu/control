package at.florianschuster.control.androidgithub

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun View.showSnackBar(
    @StringRes messageResource: Int
) = suspendCancellableCoroutine<Unit> { continuation ->
    val snackbar = Snackbar
        .make(this, messageResource, Snackbar.LENGTH_LONG)
        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                continuation.resume(Unit)
            }
        })
    snackbar.show()
    continuation.invokeOnCancellation { snackbar.dismiss() }
}
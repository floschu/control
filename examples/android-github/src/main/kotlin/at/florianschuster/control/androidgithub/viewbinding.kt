package at.florianschuster.control.androidgithub

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal fun <Binding : ViewBinding> Fragment.viewBinding(
    binder: (View) -> Binding
): ReadOnlyProperty<Fragment, Binding> = object : ReadOnlyProperty<Fragment, Binding> {

    private var binding: Binding? = null

    init {
        viewLifecycleOwnerLiveData.observe(this@viewBinding) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(
                LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        binding = null
                    }
                }
            )
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Binding {
        val binding = binding
        if (binding != null) return binding
        val lifecycleState = viewLifecycleOwner.lifecycle.currentState
        check(lifecycleState.isAtLeast(Lifecycle.State.INITIALIZED)) { "fragment view is destroyed" }
        return binder(thisRef.requireView()).also { this.binding = it }
    }
}
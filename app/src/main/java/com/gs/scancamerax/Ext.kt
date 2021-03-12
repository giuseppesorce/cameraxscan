package com.gs.scancamerax

import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Fragment.replaceFragment(container: Int, fragment: Fragment?, tag: String) {
    fragment?.let {
        this.childFragmentManager
            .beginTransaction()
            .replace(container, it, tag)
            .commitAllowingStateLoss()
    }
}

fun FragmentActivity.replaceFragment(container: Int, fragment: Fragment?, tag: String) {
    fragment?.let {
        this.supportFragmentManager
            .beginTransaction()
            .replace(container, it, tag)
            .commitAllowingStateLoss()
    }
}

fun FragmentActivity.delete(container: Int, fragment: Fragment?) {
    fragment?.let {
        this.supportFragmentManager
            .beginTransaction().remove(fragment)
            .commitAllowingStateLoss()
    }
}

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}
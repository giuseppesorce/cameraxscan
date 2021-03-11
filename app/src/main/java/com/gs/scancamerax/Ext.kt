package com.gs.scancamerax

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
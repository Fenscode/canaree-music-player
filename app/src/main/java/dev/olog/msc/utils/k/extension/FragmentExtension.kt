@file:Suppress("NOTHING_TO_INLINE")

package dev.olog.msc.utils.k.extension

import android.content.Context
import androidx.annotation.DimenRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun <T: Fragment> T.withArguments(vararg params: Pair<String, Any>) : T {
    arguments = bundleOf(*params)
    return this
}

inline val Fragment.ctx : Context
    get() = context!!

inline val Fragment.act : FragmentActivity
    get() = activity!!

fun Fragment.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)
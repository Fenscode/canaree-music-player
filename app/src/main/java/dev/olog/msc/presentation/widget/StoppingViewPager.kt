package dev.olog.msc.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class StoppingViewPager @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null

) : androidx.viewpager.widget.ViewPager(context, attrs) {

    var isSwipeEnabled = true

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (isSwipeEnabled){
            return super.onInterceptTouchEvent(ev)
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (isSwipeEnabled){
            return super.onTouchEvent(ev)
        }
        return false
    }

}
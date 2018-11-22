package dev.olog.msc.utils.k.extension

import androidx.fragment.app.FragmentActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dev.olog.msc.R
import dev.olog.msc.presentation.base.HasSlidingPanel


fun SlidingUpPanelLayout?.isCollapsed() = this != null &&
        panelState == SlidingUpPanelLayout.PanelState.COLLAPSED
fun SlidingUpPanelLayout?.isExpanded() = this != null &&
        panelState != SlidingUpPanelLayout.PanelState.COLLAPSED

fun SlidingUpPanelLayout?.collapse() {
    if (this != null){
        panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
    }
}

fun SlidingUpPanelLayout?.expand() {
    if (this != null){
        panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }
}

fun FragmentActivity.findSlidingPanel(): SlidingUpPanelLayout? {
    return if(this is HasSlidingPanel) this.findViewById(R.id.slidingPanel) else null
}
package dev.olog.msc.presentation.detail.scroll.listener

import android.view.View
import androidx.core.view.isVisible
import dev.olog.msc.R
import dev.olog.msc.presentation.detail.DetailFragment
import dev.olog.msc.utils.k.extension.dimen
import dev.olog.msc.utils.k.extension.toggleVisibility
import kotlinx.android.synthetic.main.fragment_detail.view.*

class HeaderVisibilityScrollListener(
        private val fragment: DetailFragment

) : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

    private val context = fragment.context!!
    private val toolbarHeight by lazy {
        val statusBarHeight = fragment.view!!.statusBar.height
        statusBarHeight + context.dimen(R.dimen.toolbar)
    }

    override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
        val child = recyclerView.getChildAt(0)
        val holder = recyclerView.getChildViewHolder(child)

        val view = fragment.view!!

        if (holder.itemViewType == R.layout.item_detail_item_image) {
            val bottom = child.bottom - child.findViewById<View>(R.id.textWrapper).height
            val needDarkLayout = bottom - toolbarHeight < 0

            view.statusBar.toggleVisibility(needDarkLayout, false)
            view.toolbar.toggleVisibility(needDarkLayout, false)
            view.headerText.toggleVisibility(needDarkLayout && !view.searchWrapper.isVisible, false)

            fragment.hasLightStatusBarColor = needDarkLayout

        } else {
            view.statusBar.toggleVisibility(true, false)
            view.toolbar.toggleVisibility(true, false)
            view.headerText.toggleVisibility(!view.searchWrapper.isVisible, false)

            fragment.hasLightStatusBarColor = true
        }
    }

}
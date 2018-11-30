package dev.olog.msc.presentation.detail


import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.RxTextView
import dev.olog.msc.R
import dev.olog.msc.presentation.BindingsAdapter
import dev.olog.msc.presentation.base.BaseFragment
import dev.olog.msc.presentation.base.adapter.drag.TouchHelperAdapterCallback
import dev.olog.msc.presentation.base.music.service.MediaProvider
import dev.olog.msc.presentation.detail.scroll.listener.HeaderVisibilityScrollListener
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.presentation.theme.AppTheme
import dev.olog.msc.presentation.utils.lazyFast
import dev.olog.msc.presentation.viewModelProvider
import dev.olog.msc.presentation.widget.image.view.ShapeImageView
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import javax.inject.Inject
import kotlin.properties.Delegates

class DetailFragment : BaseFragment() {

    companion object {
        const val TAG = "DetailFragment"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"

        @JvmStatic
        fun newInstance(mediaId: MediaId): DetailFragment {
            return DetailFragment().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId.toString())
        }
    }

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazyFast { viewModelProvider<DetailFragmentViewModel>(viewModelFactory) }

    private val recyclerOnScrollListener by lazyFast { HeaderVisibilityScrollListener(this) }

    private val recycledViewPool by lazyFast { RecyclerView.RecycledViewPool() }

    private val mediaId by lazyFast {
        val mediaId = arguments!!.getString(DetailFragment.ARGUMENTS_MEDIA_ID)!!
        MediaId.fromString(mediaId)
    }

    private val mostPlayedAdapter by lazyFast { DetailMostPlayedAdapter(lifecycle, navigator, act as MediaProvider) }
    private val recentlyAddedAdapter by lazyFast { DetailRecentlyAddedAdapter(lifecycle, navigator, act as MediaProvider) }
    private val relatedArtistAdapter by lazyFast { DetailRelatedArtistsAdapter(lifecycle, navigator) }
    private val albumsAdapter by lazyFast { DetailAlbumsAdapter(lifecycle, navigator) }

    private val adapter by lazyFast { DetailFragmentAdapter(
            lifecycle, mediaId, recentlyAddedAdapter, mostPlayedAdapter, relatedArtistAdapter,
            albumsAdapter, navigator, act as MediaProvider, viewModel, recycledViewPool
    ) }

    internal var hasLightStatusBarColor by Delegates.observable(false) { _, _, new ->
        adjustStatusBarColor(new)
    }

    override fun onViewBound(view: View, savedInstanceState: Bundle?) {
        view.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(ctx)
        view.list.adapter = adapter
        view.list.setRecycledViewPool(recycledViewPool)
        view.list.setHasFixedSize(true)

        var swipeDirections = ItemTouchHelper.LEFT
        if (adapter.canSwipeRight){
            swipeDirections = swipeDirections or ItemTouchHelper.RIGHT
        }
        val callback = TouchHelperAdapterCallback(adapter, swipeDirections)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(view.list)
        adapter.touchHelper = touchHelper

        view.fastScroller.attachRecyclerView(view.list)
        view.fastScroller.showBubble(false)

        view.cover?.setVisible()

        viewModel.getMostPlayedLiveData()
                .subscribe(viewLifecycleOwner, mostPlayedAdapter::updateDataSet)

        viewModel.getRecentlyAddedLiveData()
                .subscribe(viewLifecycleOwner, recentlyAddedAdapter::updateDataSet)

        viewModel.getRelatedArtistsLiveData()
                .subscribe(viewLifecycleOwner, relatedArtistAdapter::updateDataSet)

        viewModel.getAlbumsLiveData()
                .subscribe(viewLifecycleOwner, albumsAdapter::updateDataSet)

        viewModel.observeData()
                .subscribe(viewLifecycleOwner) { map ->
                    val copy = map.deepCopy()
                    if (copy.isEmpty()){
                        act.onBackPressed()
                    } else {
                        if (ctx.isLandscape){
                            // header in list is not used in landscape
                            copy[DetailFragmentDataType.HEADER]!!.clear()
                        }
                        adapter.updateDataSet(copy)
                    }
                }

        viewModel.getItemLiveData().subscribe(viewLifecycleOwner) { item ->
            if (item.isNotEmpty()){
                headerText.text = item[0].title
                val cover = view.findViewById<View>(R.id.cover)
                if (!isPortrait() && cover is ShapeImageView){
                    BindingsAdapter.loadBigAlbumImage(cover, item[0])
                }
            }
        }

        RxTextView.afterTextChangeEvents(view.editText)
                .map { it.view() }
                .asLiveData()
                .subscribe(viewLifecycleOwner) { edit ->
                    val isEmpty = edit.text.isEmpty()
                    view.clear.toggleVisibility(!isEmpty, true)
                    viewModel.updateFilter(edit.text.toString())
                }
    }

    override fun onResume() {
        super.onResume()
        if (ctx.isPortrait){
            list.addOnScrollListener(recyclerOnScrollListener)
        }
        back.setOnClickListener { act.onBackPressed() }
        more.setOnClickListener { navigator.toDialog(viewModel.mediaId, more) }
        filter.setOnClickListener {
            searchWrapper.toggleVisibility(!searchWrapper.isVisible, true)

            if (isDetailItemImageExpanded() || ctx.isLandscape){
                headerText.toggleVisibility(!searchWrapper.isVisible, true)
            } else {
                headerText.toggleVisibility(false, true)
            }

        }
        clear.setOnClickListener { editText.setText("") }
    }

    override fun onPause() {
        super.onPause()
        if (ctx.isPortrait){
            list.removeOnScrollListener(recyclerOnScrollListener)
        }
        back.setOnClickListener(null)
        more.setOnClickListener(null)
        filter.setOnClickListener(null)
        clear.setOnClickListener(null)
    }

    private fun isDetailItemImageExpanded(): Boolean {
        val child = list.getChildAt(0)
        val holder = list.getChildViewHolder(child)
        if (holder.itemViewType == R.layout.item_detail_item_image){
            val bottom = child.bottom - child.findViewById<View>(R.id.textWrapper).height
            val toolbarHeight = statusBar.height + dimen(R.dimen.toolbar)
            return bottom - toolbarHeight < 0
        }
        return true
    }

    internal fun adjustStatusBarColor(lightStatusBar: Boolean = hasLightStatusBarColor){
        if (lightStatusBar){
            setLightStatusBar()
        } else {
            removeLightStatusBar()
        }
    }

    private fun removeLightStatusBar(){
        act.window.removeLightStatusBar()
        val color = ContextCompat.getColor(ctx, R.color.detail_button_color_light)
        view?.back?.setColorFilter(color)
        more?.setColorFilter(color)
        filter?.setColorFilter(color)
    }

    private fun setLightStatusBar(){
        if (AppTheme.isDarkTheme()){
            return
        }

        act.window.setLightStatusBar()
        val color = ContextCompat.getColor(ctx, R.color.detail_button_color_dark)
        view?.back?.setColorFilter(color)
        more?.setColorFilter(color)
        filter?.setColorFilter(color)
    }

    override fun provideLayoutId(): Int = R.layout.fragment_detail
}

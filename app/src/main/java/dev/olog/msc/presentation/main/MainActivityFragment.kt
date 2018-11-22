package dev.olog.msc.presentation.main

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dev.olog.msc.R
import dev.olog.msc.presentation.base.BaseFragment
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.presentation.theme.AppTheme
import dev.olog.msc.utils.k.extension.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import javax.inject.Inject

class MainActivityFragment: BaseFragment() {

    companion object {
        const val TAG = "MainActivityFragment"
    }

    @Inject lateinit var presenter: MainActivityPresenter
    @Inject lateinit var navigator: Navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var navigateTo = presenter.getLastBottomViewPage()
        if (!presenter.canShowPodcastCategory()){
            view.bottomNavigation.menu.removeItem(R.id.navigation_podcasts)
            if (navigateTo == R.id.navigation_podcasts) {
                navigateTo = R.id.navigation_songs
                presenter.setLastBottomViewPage(navigateTo)
            }
        }

        view.slidingPanel.panelHeight = dimen(R.dimen.sliding_panel_peek) + dimen(R.dimen.bottom_navigation_height)
        view.bottomNavigation.selectedItemId = navigateTo
        bottomNavigate(navigateTo, false)

        presenter.isRepositoryEmptyUseCase.execute()
                .asLiveData()
                .subscribe(this, this::handleEmptyRepository)


        if (AppTheme.isMiniTheme()){
            view.slidingPanel.setParallaxOffset(0)
            view.playerLayout.layoutParams = SlidingUpPanelLayout.LayoutParams(
                    SlidingUpPanelLayout.LayoutParams.MATCH_PARENT, SlidingUpPanelLayout.LayoutParams.WRAP_CONTENT
            )
        }

        view.bottomWrapper.doOnPreDraw {
            if (slidingPanel.isExpanded()){
                view.bottomWrapper.translationY = view.bottomWrapper.height.toFloat()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation.setOnNavigationItemSelectedListener {
            presenter.setLastBottomViewPage(it.itemId)
            bottomNavigate(it.itemId, false)
            true
        }
        bottomNavigation.setOnNavigationItemReselectedListener { bottomNavigate(it.itemId, true) }
        slidingPanel.addPanelSlideListener(onPanelSlide)
        handleFakeView(slidingPanel.panelState)
    }

    override fun onPause() {
        super.onPause()
        bottomNavigation.setOnNavigationItemSelectedListener(null)
        bottomNavigation.setOnNavigationItemReselectedListener(null)
        slidingPanel.removePanelSlideListener(onPanelSlide)
    }

    fun bottomNavigate(itemId: Int, forceRecreate: Boolean){
        when (itemId){
            R.id.navigation_songs -> navigator.toLibraryCategories(forceRecreate)
            R.id.navigation_search -> navigator.toSearchFragment()
            R.id.navigation_podcasts -> navigator.toPodcastCategories(forceRecreate)
            R.id.navigation_queue -> navigator.toPlayingQueueFragment()
            else -> bottomNavigate(R.id.navigation_songs, forceRecreate)
        }
    }


    private val onPanelSlide = object : SlidingUpPanelLayout.PanelSlideListener {

        override fun onPanelSlide(panel: View, slideOffset: Float) {
            bottomWrapper.translationY = bottomWrapper.height * clamp(slideOffset, 0f, 1f)
        }

        override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
            handleFakeView(newState)
        }
    }

    private fun handleEmptyRepository(isEmpty: Boolean){
        if (isEmpty){
            slidingPanel.panelHeight = dimen(R.dimen.bottom_navigation_height)
        } else {
            slidingPanel.panelHeight = dimen(R.dimen.sliding_panel_peek) + dimen(R.dimen.bottom_navigation_height)
        }
    }

    private fun handleFakeView(state: SlidingUpPanelLayout.PanelState){
        when (state){
            SlidingUpPanelLayout.PanelState.EXPANDED,
            SlidingUpPanelLayout.PanelState.ANCHORED -> {
                fakeView.isClickable = true
                fakeView.isFocusable = true
                fakeView.setOnClickListener { slidingPanel.collapse() }
            }
            else -> {
                fakeView.setOnClickListener(null)
                fakeView.isClickable = false
                fakeView.isFocusable = false
            }
        }
    }

    override fun provideLayoutId(): Int = R.layout.fragment_main

}
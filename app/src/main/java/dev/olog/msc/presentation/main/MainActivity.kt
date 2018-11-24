package dev.olog.msc.presentation.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.crashlytics.android.Crashlytics
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.firebase.analytics.FirebaseAnalytics
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dev.olog.msc.Permissions
import dev.olog.msc.R
import dev.olog.msc.constants.AppConstants
import dev.olog.msc.constants.FloatingWindowsConstants
import dev.olog.msc.constants.MusicConstants
import dev.olog.msc.floating.window.service.FloatingWindowHelper
import dev.olog.msc.music.service.MusicService
import dev.olog.msc.presentation.DrawsOnTop
import dev.olog.msc.presentation.base.HasBilling
import dev.olog.msc.presentation.base.HasSlidingPanel
import dev.olog.msc.presentation.base.bottom.sheet.DimBottomSheetDialogFragment
import dev.olog.msc.presentation.base.music.service.MusicGlueActivity
import dev.olog.msc.presentation.dialog.rate.request.RateAppDialog
import dev.olog.msc.presentation.library.categories.track.CategoriesFragment
import dev.olog.msc.presentation.library.folder.tree.FolderTreeFragment
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.presentation.preferences.PreferencesActivity
import dev.olog.msc.presentation.utils.animation.HasSafeTransition
import dev.olog.msc.pro.IBilling
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.*
import javax.inject.Inject


class MainActivity : MusicGlueActivity(), HasSlidingPanel, HasBilling {

    companion object {
        const val INVITE_FRIEND_CODE = 12198
    }

    @Inject lateinit var presenter: MainActivityPresenter
    @Inject lateinit var navigator: Navigator
    // handles lifecycle itself
    @Inject override lateinit var billing: IBilling

    @Suppress("unused") @Inject
    lateinit var statusBarColorBehavior: StatusBarColorBehavior
    @Suppress("unused") @Inject
    lateinit var rateAppDialog : RateAppDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val canReadStorage = Permissions.canReadStorage(this)
        val isFirstAccess = presenter.isFirstAccess()
        val toFirstAccess = !canReadStorage || isFirstAccess
        if (toFirstAccess){
            navigator.toFirstAccess()
            return
        } else if (savedInstanceState == null){
            navigator.toMain()
        }

        intent?.let { handleIntent(it) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action){
            FloatingWindowsConstants.ACTION_START_SERVICE -> {
                FloatingWindowHelper.startServiceIfHasOverlayPermission(this)
            }
            AppConstants.ACTION_CONTENT_VIEW -> getSlidingPanel().expand()
            MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH -> {
                val serviceIntent = Intent(this, MusicService::class.java)
                serviceIntent.action = MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
                ContextCompat.startForegroundService(this, serviceIntent)
            }
            AppConstants.SHORTCUT_DETAIL -> {
                val string = intent.getStringExtra(AppConstants.SHORTCUT_DETAIL_MEDIA_ID)
                val mediaId = MediaId.fromString(string)
                navigator.toDetailFragment(mediaId)
            }
            Intent.ACTION_VIEW -> {
                val serviceIntent = Intent(this, MusicService::class.java)
                serviceIntent.action = MusicConstants.ACTION_PLAY_FROM_URI
                serviceIntent.data = intent.data
                ContextCompat.startForegroundService(this, serviceIntent)
            }
        }
        setIntent(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK){
            when (requestCode){
                PreferencesActivity.REQUEST_CODE -> recreate()
                INVITE_FRIEND_CODE -> handleOnFriendsInvited(resultCode, data)
            }
            return
        }

        if (requestCode == FloatingWindowHelper.REQUEST_CODE_HOVER_PERMISSION){
            FloatingWindowHelper.startServiceIfHasOverlayPermission(this)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleOnFriendsInvited(resultCode: Int, data: Intent?){
        try {
            val invitedIds = AppInviteInvitation.getInvitationIds(resultCode, data!!)
            val analytics = FirebaseAnalytics.getInstance(this)
            analytics.logEvent("invited_friends", bundleOf(
                    "friends_number_invited" to invitedIds.size
            ))
            analytics.setUserProperty("invited_friends", "true")
        } catch (ex: Exception){
            ex.printStackTrace()
            Crashlytics.logException(ex)
        }
    }

    override fun onBackPressed() {
        try {
            if (tryPopFolderBack()){
                return
            }

            val topFragment = getTopFragment()

            when {
                topFragment is HasSafeTransition && topFragment.isAnimating() -> {
//                  prevents circular reveal crash
                }
                topFragment is DrawsOnTop -> super.onBackPressed()
                topFragment is DimBottomSheetDialogFragment -> supportFragmentManager.popBackStack()
                getSlidingPanel().isExpanded() -> getSlidingPanel().collapse()
                else -> super.onBackPressed()
            }
        } catch (ex: IllegalStateException){ /*random fragment manager crashes */}

    }

    private fun tryPopFolderBack(): Boolean {
        val categories = findFragmentByTag<CategoriesFragment>(CategoriesFragment.TAG)
        categories?.view?.findViewById<androidx.viewpager.widget.ViewPager>(R.id.viewPager)?.let { pager ->
            val currentItem = pager.adapter?.instantiateItem(pager, pager.currentItem) as androidx.fragment.app.Fragment
            return if (currentItem is FolderTreeFragment){
                currentItem.pop()
            } else false

        } ?: return false
    }

    private fun findMainFragment(): MainActivityFragment? {
        return findFragmentByTag(MainActivityFragment.TAG)
    }
    override fun getSlidingPanel(): SlidingUpPanelLayout? = findSlidingPanel()
}
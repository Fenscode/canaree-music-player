package dev.olog.msc.presentation.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import dev.olog.msc.Permissions
import dev.olog.msc.R
import dev.olog.msc.presentation.base.BaseFragment
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.presentation.theme.ThemedDialog
import dev.olog.msc.presentation.utils.lazyFast
import dev.olog.msc.updatePermissionValve
import dev.olog.msc.utils.k.extension.act
import dev.olog.msc.utils.k.extension.ctx
import dev.olog.msc.utils.k.extension.simpleDialog
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_splash.view.*
import javax.inject.Inject

class SplashFragmentWrapper : BaseFragment(), View.OnClickListener {

    companion object {
        const val TAG = "SplashFragmentWrapper"
    }

    @Inject lateinit var navigator: Navigator
    private val adapter by lazyFast { SplashActivityViewPagerAdapter(fragmentManager!!) }

    override fun onViewBound(view: View, savedInstanceState: Bundle?) {
        view.viewPager.adapter = adapter
        view.inkIndicator.setViewPager(view.viewPager)
    }

    override fun onResume() {
        super.onResume()
        next.setOnClickListener {
            if (viewPager.currentItem == 0){
                viewPager.setCurrentItem(1, true)
            } else {
                requestStoragePermission()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        next.setOnClickListener(null)
    }

    override fun onClick(v: View?) {
        if (viewPager.currentItem == 0){
            viewPager.setCurrentItem(1, true)
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission(){
        if (!Permissions.canReadStorage(ctx)){
            Permissions.requestReadStorage(this)
        } else {
            onStoragePermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (Permissions.checkWriteCode(requestCode)){
            if (Permissions.canReadStorage(ctx)){
                onStoragePermissionGranted()
            } else {
                onStoragePermissionDenied()
            }
        }
    }

    private fun onStoragePermissionGranted(){
        updatePermissionValve(ctx, true)

//        explain trial
        ThemedDialog.builder(ctx)
                .setTitle(R.string.trial_title)
                .setMessage(R.string.trial_message)
                .setPositiveButton(R.string.trial_positive_button) { _, _ -> navigator.toMain() }
                .show()
    }

    private fun onStoragePermissionDenied(){
        if (Permissions.hasUserDisabledReadStorage(act)){
            act.simpleDialog {
                setTitle(R.string.splash_storage_permission)
                setMessage(R.string.splash_storage_permission_disabled)
                setPositiveButton(R.string.popup_positive_ok, { _, _ -> toSettings() })
                setNegativeButton(R.string.popup_negative_no, null)
            }
        }
    }

    private fun toSettings(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", act.packageName, null))
        startActivity(intent)
    }

    override fun provideLayoutId(): Int = R.layout.activity_splash
}
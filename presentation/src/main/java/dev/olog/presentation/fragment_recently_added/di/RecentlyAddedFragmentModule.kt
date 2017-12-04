package dev.olog.presentation.fragment_recently_added.di

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dev.olog.presentation.dagger.FragmentLifecycle
import dev.olog.presentation.fragment_recently_added.RecentlyAddedFragment
import dev.olog.presentation.fragment_recently_added.RecentlyAddedFragmentViewModel
import dev.olog.presentation.fragment_recently_added.RecentlyAddedFragmentViewModelFactory

@Module
class RecentlyAddedFragmentModule(
        private val fragment: RecentlyAddedFragment
) {

    @Provides
    @FragmentLifecycle
    internal fun lifecycle(): Lifecycle = fragment.lifecycle

    @Provides
    internal fun provideMediaId(): String {
        return fragment.arguments!!.getString(RecentlyAddedFragment.ARGUMENTS_MEDIA_ID)
    }

    @Provides
    internal fun provideViewModel(factory: RecentlyAddedFragmentViewModelFactory): RecentlyAddedFragmentViewModel {

        return ViewModelProviders.of(fragment, factory).get(RecentlyAddedFragmentViewModel::class.java)
    }

}
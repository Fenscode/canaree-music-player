package dev.olog.msc.presentation.main.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.olog.msc.presentation.dialog.sleep.timer.SleepTimerPickerDialog
import dev.olog.msc.presentation.equalizer.EqualizerFragment
import dev.olog.msc.presentation.library.categories.podcast.CategoriesPodcastFragment
import dev.olog.msc.presentation.library.categories.track.CategoriesFragment
import dev.olog.msc.presentation.main.MainActivityFragment
import dev.olog.msc.presentation.mini.player.MiniPlayerFragment
import dev.olog.msc.presentation.offline.lyrics.OfflineLyricsFragment
import dev.olog.msc.presentation.splash.SplashFragmentWrapper

@Module
abstract class MainActivityFragmentsModule {

    @ContributesAndroidInjector
    abstract fun provideSplashFragmentWrapper(): SplashFragmentWrapper

    @ContributesAndroidInjector
    abstract fun provideMainActivityFragment(): MainActivityFragment

    @ContributesAndroidInjector
    abstract fun provideMiniPlayer() : MiniPlayerFragment

    @ContributesAndroidInjector
    abstract fun provideEqualizerFragment(): EqualizerFragment

    @ContributesAndroidInjector
    abstract fun provideSleepTimerDialog() : SleepTimerPickerDialog

    @ContributesAndroidInjector
    abstract fun provideOfflineLyricsFragment(): OfflineLyricsFragment

    @ContributesAndroidInjector
    abstract fun provideCategoriesFragment(): CategoriesFragment

    @ContributesAndroidInjector
    abstract fun provideCategoriesPodcastFragment(): CategoriesPodcastFragment
}
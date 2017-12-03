package dev.olog.presentation.activity_main.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import dev.olog.presentation.activity_main.MainActivity
import dev.olog.presentation.dagger.PerActivity
import dev.olog.presentation.dialog_add_favorite.di.AddFavoriteDialogInjector
import dev.olog.presentation.dialog_add_playlist.di.AddPlaylistDialogInjector
import dev.olog.presentation.dialog_add_queue.di.AddQueueDialogInjector
import dev.olog.presentation.dialog_clear_playlist.di.ClearPlaylistDialogInjector
import dev.olog.presentation.dialog_delete.di.DeleteDialogInjector
import dev.olog.presentation.dialog_new_playlist.di.NewPlaylistDialogInjector
import dev.olog.presentation.dialog_rename.di.RenameDialogInjector
import dev.olog.presentation.dialog_set_ringtone.di.SetRingtoneDialogInjector
import dev.olog.presentation.fragment_detail.di.DetailFragmentInjector
import dev.olog.presentation.fragment_mini_player.di.MiniPlayerFragmentInjector
import dev.olog.presentation.fragment_player.di.PlayerFragmentInjector
import dev.olog.presentation.fragment_queue.di.PlayingQueueInjector
import dev.olog.presentation.fragment_related_artist.di.RelatedArtistFragmentInjector
import dev.olog.presentation.fragment_tab.di.TabFragmentInjector
import dev.olog.presentation.navigation.NavigatorModule

@Subcomponent(modules = arrayOf(
        MainActivityModule::class,
        NavigatorModule::class,

        // fragments
        TabFragmentInjector::class,
        DetailFragmentInjector::class,
        PlayerFragmentInjector::class,
        MiniPlayerFragmentInjector::class,
        RelatedArtistFragmentInjector::class,
        PlayingQueueInjector::class,

        // dialogs
        AddFavoriteDialogInjector::class,
        AddPlaylistDialogInjector::class,
        NewPlaylistDialogInjector::class,
        AddQueueDialogInjector::class,
        SetRingtoneDialogInjector::class,
        RenameDialogInjector::class,
        ClearPlaylistDialogInjector::class,
        DeleteDialogInjector::class
))
@PerActivity
interface MainActivitySubComponent :AndroidInjector<MainActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>() {

        abstract fun module(module: MainActivityModule): Builder

        override fun seedInstance(instance: MainActivity) {
            module(MainActivityModule(instance))
        }
    }

}
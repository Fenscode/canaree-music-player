package dev.olog.msc.presentation.main.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import dev.olog.msc.dagger.scope.PerActivity
import dev.olog.msc.presentation.detail.di.DetailFragmentInjector
import dev.olog.msc.presentation.dialog.DialogModule
import dev.olog.msc.presentation.edit.album.di.EditAlbumFragmentInjector
import dev.olog.msc.presentation.edit.artist.di.EditArtistFragmentInjector
import dev.olog.msc.presentation.edit.track.di.EditTrackFragmentInjector
import dev.olog.msc.presentation.library.folder.tree.di.FolderTreeFragmentModule
import dev.olog.msc.presentation.library.tab.di.TabFragmentInjector
import dev.olog.msc.presentation.main.MainActivity
import dev.olog.msc.presentation.player.di.PlayerFragmentModule
import dev.olog.msc.presentation.playing.queue.di.PlayingQueueFragmentInjector
import dev.olog.msc.presentation.playlist.track.chooser.di.PlaylistTracksChooserInjector
import dev.olog.msc.presentation.recently.added.di.RecentlyAddedFragmentInjector
import dev.olog.msc.presentation.related.artists.di.RelatedArtistFragmentInjector
import dev.olog.msc.presentation.search.di.SearchFragmentInjector

@Subcomponent(modules = arrayOf(
        MainActivityModule::class,
        MainActivityFragmentsModule::class,
//
//        // fragments
        TabFragmentInjector::class,
        FolderTreeFragmentModule::class,
        DetailFragmentInjector::class,
        PlayerFragmentModule::class,
        RecentlyAddedFragmentInjector::class,
        RelatedArtistFragmentInjector::class,
        SearchFragmentInjector::class,
        PlayingQueueFragmentInjector::class,
        EditTrackFragmentInjector::class,
        EditAlbumFragmentInjector::class,
        EditArtistFragmentInjector::class,
        PlaylistTracksChooserInjector::class,

        DialogModule::class
))
@PerActivity
interface MainActivitySubComponent : AndroidInjector<MainActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>() {

        abstract fun module(module: MainActivityModule): Builder

        override fun seedInstance(instance: MainActivity) {
            module(MainActivityModule(instance))
        }
    }

}
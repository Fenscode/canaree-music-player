package dev.olog.msc.presentation.dialog

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.olog.msc.presentation.dialog.add.favorite.AddFavoriteDialog
import dev.olog.msc.presentation.dialog.clear.playlist.ClearPlaylistDialog
import dev.olog.msc.presentation.dialog.create.playlist.NewPlaylistDialog
import dev.olog.msc.presentation.dialog.delete.DeleteDialog
import dev.olog.msc.presentation.dialog.play.later.PlayLaterDialog
import dev.olog.msc.presentation.dialog.play.next.PlayNextDialog
import dev.olog.msc.presentation.dialog.remove.duplicates.RemoveDuplicatesDialog
import dev.olog.msc.presentation.dialog.rename.RenameDialog
import dev.olog.msc.presentation.dialog.set.ringtone.SetRingtoneDialog

@Module
abstract class DialogModule {

    @ContributesAndroidInjector
    abstract fun provideAddFavoriteDialog(): AddFavoriteDialog

    @ContributesAndroidInjector
    abstract fun provideClearPlaylistDialog(): ClearPlaylistDialog

    @ContributesAndroidInjector
    abstract fun provideNewPlaylistDialog(): NewPlaylistDialog

    @ContributesAndroidInjector
    abstract fun provideDeleteDialog(): DeleteDialog

    @ContributesAndroidInjector
    abstract fun providePlayLaterDialog(): PlayLaterDialog

    @ContributesAndroidInjector
    abstract fun providePlayNextDialog(): PlayNextDialog

    @ContributesAndroidInjector
    abstract fun provideRemoveDuplicatesDialog(): RemoveDuplicatesDialog

    @ContributesAndroidInjector
    abstract fun provideRenameDialog(): RenameDialog

    @ContributesAndroidInjector
    abstract fun provideSetRingtoneDialog(): SetRingtoneDialog

}
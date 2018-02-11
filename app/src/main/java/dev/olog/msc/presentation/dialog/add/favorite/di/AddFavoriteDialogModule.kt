package dev.olog.msc.presentation.dialog.add.favorite.di

import android.arch.lifecycle.Lifecycle
import dagger.Module
import dagger.Provides
import dev.olog.msc.dagger.FragmentLifecycle
import dev.olog.msc.presentation.dialog.add.favorite.AddFavoriteDialog
import dev.olog.msc.utils.MediaId

@Module
class AddFavoriteDialogModule(
        private val fragment: AddFavoriteDialog
) {

    @Provides
    @FragmentLifecycle
    fun provideLifecycle(): Lifecycle = fragment.lifecycle

    @Provides
    fun provideMediaId(): MediaId {
        val mediaId = fragment.arguments!!.getString(AddFavoriteDialog.ARGUMENTS_MEDIA_ID)
        return MediaId.fromString(mediaId)
    }

    @Provides
    fun provideListSize(): Int {
        return fragment.arguments!!.getInt(AddFavoriteDialog.ARGUMENTS_LIST_SIZE)
    }

}
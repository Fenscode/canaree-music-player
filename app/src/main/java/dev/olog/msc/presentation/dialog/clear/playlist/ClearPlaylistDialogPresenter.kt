package dev.olog.msc.presentation.dialog.clear.playlist

import dev.olog.msc.domain.interactor.dialog.ClearPlaylistUseCase
import dev.olog.msc.utils.MediaId
import io.reactivex.Completable
import javax.inject.Inject

class ClearPlaylistDialogPresenter @Inject constructor(
        private val useCase: ClearPlaylistUseCase

) {

    fun execute(mediaId: MediaId): Completable {
        return useCase.execute(mediaId)
    }

}
package dev.olog.msc.presentation.dialog.remove.duplicates

import dev.olog.msc.domain.interactor.dialog.RemoveDuplicatesUseCase
import dev.olog.msc.utils.MediaId
import io.reactivex.Completable
import javax.inject.Inject

class RemoveDuplicatesDialogPresenter @Inject constructor(
        private val useCase: RemoveDuplicatesUseCase
) {

    fun execute(mediaId: MediaId): Completable {
        return useCase.execute(mediaId)
    }

}
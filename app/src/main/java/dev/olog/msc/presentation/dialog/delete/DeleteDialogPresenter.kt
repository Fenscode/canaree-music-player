package dev.olog.msc.presentation.dialog.delete

import dev.olog.msc.domain.interactor.dialog.DeleteUseCase
import dev.olog.msc.utils.MediaId
import io.reactivex.Completable
import javax.inject.Inject

class DeleteDialogPresenter @Inject constructor(
        private val deleteUseCase: DeleteUseCase
) {


    fun execute(mediaId: MediaId): Completable {
        return deleteUseCase.execute(mediaId)
    }

}
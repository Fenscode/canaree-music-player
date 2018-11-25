package dev.olog.msc.presentation.dialog.play.later

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import dev.olog.msc.constants.MusicConstants
import dev.olog.msc.domain.interactor.all.GetSongListByParamUseCase
import dev.olog.msc.utils.MediaId
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PlayLaterDialogPresenter @Inject constructor(
        private val getSongListByParamUseCase: GetSongListByParamUseCase
) {

    fun execute(mediaId: MediaId, mediaController: MediaControllerCompat): Completable {
        return if (mediaId.isLeaf){
            Single.fromCallable { "${mediaId.leaf!!}" }.subscribeOn(Schedulers.io())
        } else {
            getSongListByParamUseCase.execute(mediaId)
                    .firstOrError()
                    .map { songList -> songList.asSequence().map { it.id }.joinToString() }
        }.map { mediaController.addQueueItem(newMediaDescriptionItem(mediaId, it)) }
                .ignoreElement()
    }

    private fun newMediaDescriptionItem(mediaId: MediaId, songId: String): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
                .setMediaId(songId)
                .setExtras(bundleOf(MusicConstants.IS_PODCAST to mediaId.isAnyPodcast))
                .build()
    }

}
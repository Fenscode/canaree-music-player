package dev.olog.msc.presentation.dialog.rename

import dev.olog.msc.domain.entity.PlaylistType
import dev.olog.msc.domain.interactor.all.GetPlaylistsBlockingUseCase
import dev.olog.msc.domain.interactor.dialog.RenameUseCase
import dev.olog.msc.utils.MediaId
import io.reactivex.Completable
import javax.inject.Inject

class RenameDialogPresenter @Inject constructor(
        private val getPlaylistSiblingsUseCase: GetPlaylistsBlockingUseCase,
        private val renameUseCase: RenameUseCase

) {

    private var existingPlaylists : List<String>? = null

    private fun getExistingsPlaylist(mediaId: MediaId): List<String> {
        return getPlaylistSiblingsUseCase
                .execute(if (mediaId.isPodcast) PlaylistType.PODCAST else PlaylistType.TRACK)
                .map { it.title }
                .map { it.toLowerCase() }
    }

    fun execute(mediaId: MediaId, newTitle: String) : Completable {
        return renameUseCase.execute(Pair(mediaId, newTitle))
    }

    /**
     * returns false if is invalid
     */
    fun checkData(mediaId: MediaId, playlistTitle: String): Boolean {
        if (existingPlaylists == null){
            existingPlaylists = getExistingsPlaylist(mediaId)
        }

        return when {
            mediaId.isPlaylist || mediaId.isPodcastPlaylist -> !existingPlaylists!!.contains(playlistTitle.toLowerCase())
            else -> throw IllegalArgumentException("invalid media id category $mediaId")
        }
    }

}
package dev.olog.msc.presentation.dialog.create.playlist

import dev.olog.msc.domain.entity.PlaylistType
import dev.olog.msc.domain.interactor.all.GetPlaylistsBlockingUseCase
import dev.olog.msc.domain.interactor.all.GetSongListByParamUseCase
import dev.olog.msc.domain.interactor.item.GetPodcastUseCase
import dev.olog.msc.domain.interactor.item.GetSongUseCase
import dev.olog.msc.domain.interactor.playing.queue.GetPlayingQueueUseCase
import dev.olog.msc.domain.interactor.playlist.InsertCustomTrackListRequest
import dev.olog.msc.domain.interactor.playlist.InsertCustomTrackListToPlaylist
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.mapToList
import io.reactivex.Completable
import javax.inject.Inject

class NewPlaylistDialogPresenter @Inject constructor(
        private val playlistsUseCase: GetPlaylistsBlockingUseCase,
        private val insertCustomTrackListToPlaylist: InsertCustomTrackListToPlaylist,
        private val getSongListByParamUseCase: GetSongListByParamUseCase,
        private val getSongUseCase: GetSongUseCase,
        private val getPodcastUseCase: GetPodcastUseCase,
        private val getPlayinghQueueUseCase: GetPlayingQueueUseCase

) {

    private fun getPlaylistType(mediaId: MediaId): PlaylistType {
        return if (mediaId.isPodcast) PlaylistType.PODCAST else PlaylistType.TRACK
    }

    private var existingPlaylists : List<String>? = null

    private fun getExistingPlaylist(mediaId: MediaId): List<String> {
        val playlistType = getPlaylistType(mediaId)
        return playlistsUseCase.execute(playlistType)
                .map { it.title.toLowerCase() }
    }

    fun execute(mediaId: MediaId, playlistTitle: String) : Completable {
        val playlistType = getPlaylistType(mediaId)

        if (mediaId.isPlayingQueue){
            return getPlayinghQueueUseCase.execute().mapToList { it.id }
                    .flatMapCompletable {
                        insertCustomTrackListToPlaylist.execute(InsertCustomTrackListRequest(playlistTitle, it, playlistType))
                    }
        }

        return if (mediaId.isLeaf && mediaId.isPodcast) {
            getPodcastUseCase.execute(mediaId).firstOrError().map { listOf(it.id) }
                    .flatMapCompletable {
                        insertCustomTrackListToPlaylist.execute(InsertCustomTrackListRequest(playlistTitle, it, playlistType))
                    }
        } else if (mediaId.isLeaf) {
            getSongUseCase.execute(mediaId).firstOrError().map { listOf(it.id) }
                    .flatMapCompletable {
                        insertCustomTrackListToPlaylist.execute(InsertCustomTrackListRequest(playlistTitle, it, playlistType))
                    }
        } else {
            getSongListByParamUseCase.execute(mediaId).firstOrError().mapToList { it.id }
                    .flatMapCompletable {
                        insertCustomTrackListToPlaylist.execute(InsertCustomTrackListRequest(playlistTitle, it, playlistType))
                    }
        }
    }

    fun isStringValid(mediaId: MediaId, string: String): Boolean {
        if (existingPlaylists == null){
            existingPlaylists = getExistingPlaylist(mediaId)
        }
        return !existingPlaylists!!.contains(string.toLowerCase())
    }

}
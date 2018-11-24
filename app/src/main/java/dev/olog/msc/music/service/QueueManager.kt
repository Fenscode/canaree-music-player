package dev.olog.msc.music.service

import android.net.Uri
import android.os.Bundle
import dev.olog.msc.constants.MusicConstants
import dev.olog.msc.domain.entity.SortArranging
import dev.olog.msc.domain.entity.SortType
import dev.olog.msc.domain.gateway.GenreGateway
import dev.olog.msc.domain.gateway.prefs.MusicPreferencesGateway
import dev.olog.msc.domain.interactor.PodcastPositionUseCase
import dev.olog.msc.domain.interactor.all.GetSongListByParamUseCase
import dev.olog.msc.domain.interactor.all.most.played.GetMostPlayedSongsUseCase
import dev.olog.msc.domain.interactor.all.recently.added.GetRecentlyAddedUseCase
import dev.olog.msc.domain.interactor.item.GetSongByFileUseCase
import dev.olog.msc.domain.interactor.playing.queue.GetPlayingQueueBlockingUseCase
import dev.olog.msc.domain.interactor.playing.queue.GetPlayingQueueUseCase
import dev.olog.msc.music.service.interfaces.Queue
import dev.olog.msc.music.service.model.*
import dev.olog.msc.music.service.voice.VoiceSearch
import dev.olog.msc.music.service.voice.VoiceSearchParams
import dev.olog.msc.utils.Comparators.getAscendingComparator
import dev.olog.msc.utils.Comparators.getDescendingComparator
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.clamp
import dev.olog.msc.utils.k.extension.swap
import io.reactivex.Single
import io.reactivex.functions.Function
import java.text.Collator
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class QueueManager @Inject constructor(
        private val queueImpl: QueueImpl,
        private val getPlayingQueueUseCase: GetPlayingQueueUseCase,
        private val getPlayingQueueBlockingUseCase: GetPlayingQueueBlockingUseCase,
        private val musicPreferencesUseCase: MusicPreferencesGateway,
        private val shuffleMode: ShuffleMode,
        private val getSongListByParamUseCase: GetSongListByParamUseCase,
        private val getMostPlayedSongsUseCase: GetMostPlayedSongsUseCase,
        private val getRecentlyAddedUseCase: GetRecentlyAddedUseCase,
        private val getSongByFileUseCase: GetSongByFileUseCase,
        private val genreGateway: GenreGateway,
        private val collator: Collator,
        private val enhancedShuffle: EnhancedShuffle,
        private val podcastPosition: PodcastPositionUseCase

) : Queue {

    private val isReady = AtomicBoolean(false)

    override fun isReady(): Boolean = isReady.get()

    override fun prepare(): Single<PlayerMediaEntity> {
        return getPlayingQueueUseCase.execute()
                .map { list -> list.map { it.toMediaEntity() } }
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .map { lastSessionSong.apply(it) }
                .doOnSuccess { (list, position) -> queueImpl.updateCurrentSongPosition(list, position) }
                .map { (list, position) -> list[position].toPlayerMediaEntity(
                        queueImpl.computePositionInQueue(list, position), getLastSessionBookmark(list[position])) }
                .doOnSuccess { isReady.compareAndSet(false, true) }
    }

    override fun prepareBlocking(): PlayerMediaEntity? {
        try {
            val queue = getPlayingQueueBlockingUseCase.execute()
                    .map { it.toMediaEntity() }
            queueImpl.updatePlayingQueueAndPersist(queue)
            val (list, position) = lastSessionSong.apply(queue)
            queueImpl.updateCurrentSongPosition(list, position)
            val mediaEntity = list[position].toPlayerMediaEntity(
                    queueImpl.computePositionInQueue(list, position), getLastSessionBookmark(list[position])
            )
            isReady.compareAndSet(false, true)
            return mediaEntity
        } catch (ex: Exception){
            return null
        }
    }

    private fun getLastSessionBookmark(mediaEntity: MediaEntity): Long {
        if (mediaEntity.isPodcast){
            val bookmark = podcastPosition.get(mediaEntity.id, mediaEntity.duration)
            return clamp(bookmark, 0L, mediaEntity.duration)
        } else {
            val bookmark = musicPreferencesUseCase.getBookmark().toInt()
            return clamp(bookmark.toLong(), 0L, mediaEntity.duration)
        }
    }

    private fun getPodcastBookmarkOrDefault(mediaEntity: MediaEntity?, default: Long = 0L): Long {
        if (mediaEntity?.isPodcast == true){
            val bookmark = podcastPosition.get(mediaEntity.id, mediaEntity.duration)
            return clamp(bookmark, 0L, mediaEntity.duration)
        } else {
            return default
        }
    }

    override fun handleSkipToQueueItem(idInPlaylist: Long): PlayerMediaEntity {
        val mediaEntity = queueImpl.getSongById(idInPlaylist)
        val bookmark = getPodcastBookmarkOrDefault(mediaEntity)
        return mediaEntity.toPlayerMediaEntity(queueImpl.currentPositionInQueue(), bookmark)
    }

    override fun handleSkipToNext(trackEnded: Boolean): PlayerMediaEntity? {
        val mediaEntity = queueImpl.getNextSong(trackEnded)
        val bookmark = getPodcastBookmarkOrDefault(mediaEntity)
        return mediaEntity?.toPlayerMediaEntity(queueImpl.currentPositionInQueue(), bookmark)
    }

    override fun getPlayingSong(): PlayerMediaEntity {
        val mediaEntity = queueImpl.getCurrentSong()!!
        val bookmark = getPodcastBookmarkOrDefault(mediaEntity)
        return mediaEntity.toPlayerMediaEntity(queueImpl.currentPositionInQueue(), bookmark)
    }

    override fun handleSkipToPrevious(playerBookmark: Long): PlayerMediaEntity? {
        val mediaEntity = queueImpl.getPreviousSong(playerBookmark)
        val bookmark = getPodcastBookmarkOrDefault(mediaEntity)
        return mediaEntity?.toPlayerMediaEntity(queueImpl.currentPositionInQueue(), bookmark)
    }

    override fun handlePlayFromMediaId(mediaId: MediaId, extras: Bundle?): Single<PlayerMediaEntity> {
        val songId = mediaId.leaf ?: -1L

        return getSongListByParamUseCase.execute(mediaId)
                .firstOrError()
                .map { it.mapIndexed { index, song -> song.toMediaEntity(index, mediaId) } }
                .map { sortOnDemand(it, extras) }
                .map { shuffleIfNeeded(songId).apply(it) }
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .map { getCurrentSongOnPlayFromId(songId).apply(it) }
                .doOnSuccess { (list , position) -> queueImpl.updateCurrentSongPosition(list, position) }
                .map { (list, position) ->
                    val bookmark = getPodcastBookmarkOrDefault(list[position])
                    list[position].toPlayerMediaEntity(queueImpl.computePositionInQueue(list, position), bookmark) }
    }

    override fun handlePlayFolderTree(mediaId: MediaId): Single<PlayerMediaEntity> {
        return handlePlayFromMediaId(mediaId, null)
    }

    private fun sortOnDemand(list: List<MediaEntity>, extras: Bundle?): List<MediaEntity> {
        return try {
            extras!!
            val sortOrder = SortType.valueOf(extras.getString(MusicConstants.ARGUMENT_SORT_TYPE)!!)
            val arranging = SortArranging.valueOf(extras.getString(MusicConstants.ARGUMENT_SORT_ARRANGING)!!)
            return if (arranging == SortArranging.ASCENDING){
                list.sortedWith(getAscendingComparator(sortOrder, collator))
            } else {
                list.sortedWith(getDescendingComparator(sortOrder, collator))
            }
        } catch (ex: Exception){
            list
        }

    }

    override fun handlePlayRecentlyPlayed(mediaId: MediaId): Single<PlayerMediaEntity> {
        val songId = mediaId.leaf!!

        return getRecentlyAddedUseCase.execute(mediaId)
                .firstOrError()
                .map { it.mapIndexed { index, song-> song.toMediaEntity(index, mediaId) } }
                .map { shuffleIfNeeded(songId).apply(it) }
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .map { getCurrentSongOnPlayFromId(songId).apply(it) }
                .doOnSuccess { (list , position) -> queueImpl.updateCurrentSongPosition(list, position) }
                .map { (list, position) ->
                    val bookmark = getPodcastBookmarkOrDefault(list[position])
                    list[position].toPlayerMediaEntity(queueImpl.computePositionInQueue(list, position), bookmark) }
    }

    override fun handlePlayMostPlayed(mediaId: MediaId): Single<PlayerMediaEntity> {
        val songId = mediaId.leaf!!

        return getMostPlayedSongsUseCase.execute(mediaId)
                .firstOrError()
                .map { it.mapIndexed { index, song -> song.toMediaEntity(index, mediaId) } }
                .map { shuffleIfNeeded(songId).apply(it) }
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .map { getCurrentSongOnPlayFromId(songId).apply(it) }
                .doOnSuccess { (list , position) -> queueImpl.updateCurrentSongPosition(list, position) }
                .map { (list, position) ->
                    val bookmark = getPodcastBookmarkOrDefault(list[position])
                    list[position].toPlayerMediaEntity(queueImpl.computePositionInQueue(list, position), bookmark) }
    }

    override fun handlePlayShuffle(mediaId: MediaId): Single<PlayerMediaEntity> {
        return getSongListByParamUseCase.execute(mediaId)
                .firstOrError()
                .map {
                    shuffleMode.setEnabled(true)
                    it
                }
                .map { it.mapIndexed { index, song -> song.toMediaEntity(index, mediaId) } }
                .map { enhancedShuffle.shuffle(it.toMutableList()) }
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .map { Pair(it, 0) }
                .doOnSuccess { (list, position) -> queueImpl.updateCurrentSongPosition(list,position) }
                .map { (list, position) ->
                    val bookmark = getPodcastBookmarkOrDefault(list[position])
                    list[position].toPlayerMediaEntity(queueImpl.computePositionInQueue(list, position), bookmark) }
    }

    override fun handlePlayFromUri(uri: Uri): Single<PlayerMediaEntity> {
        return getSongByFileUseCase.execute(uri)
                .delay(500, TimeUnit.MILLISECONDS)
                .map { it.toMediaEntity(0, MediaId.songId(it.id)) }
                .map { listOf(it) }
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .doOnSuccess { list -> queueImpl.updateCurrentSongPosition(list, 0) }
                .map { list ->
                    val bookmark = getPodcastBookmarkOrDefault(list[0])
                    list[0].toPlayerMediaEntity(PositionInQueue.BOTH, bookmark)
                }
    }

    override fun handlePlayFromGoogleSearch(query: String, extras: Bundle): Single<PlayerMediaEntity> {
//        Log.d("VoiceSearch", "Creating playing queue for musics from search: $query, params=$extras")

        val params = VoiceSearchParams(query, extras)

        val mediaId = MediaId.songId(-1)

        val songList = when {
            params.isUnstructured -> VoiceSearch.search(getSongListByParamUseCase.execute(mediaId), query)
            params.isAlbumFocus -> VoiceSearch.filterByAlbum(getSongListByParamUseCase.execute(mediaId), params.album)
            params.isArtistFocus -> VoiceSearch.filterByArtist(getSongListByParamUseCase.execute(mediaId), params.artist)
            params.isSongFocus -> VoiceSearch.filterByTitle(getSongListByParamUseCase.execute(mediaId), params.song)
            params.isGenreFocus -> VoiceSearch.filterByGenre(genreGateway, params.genre)
            else -> VoiceSearch.noFilter(getSongListByParamUseCase.execute(mediaId).map { it.shuffled() })
        }

        return songList
                .doOnSuccess(queueImpl::updatePlayingQueueAndPersist)
                .map { Pair(it, 0) }
                .doOnSuccess { (list, position) -> queueImpl.updateCurrentSongPosition(list,position) }
                .map { (list, position) ->
                    val bookmark = getPodcastBookmarkOrDefault(list[position])
                    list[position].toPlayerMediaEntity(queueImpl.computePositionInQueue(list, position), bookmark) }
                .doOnSuccess { shuffleMode.setEnabled(false) }

    }

    override fun handleSwap(extras: Bundle) {
        val from = extras.getInt(MusicConstants.ARGUMENT_SWAP_FROM, 0)
        val to = extras.getInt(MusicConstants.ARGUMENT_SWAP_TO, 0)
        queueImpl.handleSwap(from, to)
    }

    override fun handleSwapRelative(extras: Bundle) {
        val from = extras.getInt(MusicConstants.ARGUMENT_SWAP_FROM, 0)
        val to = extras.getInt(MusicConstants.ARGUMENT_SWAP_TO, 0)
        queueImpl.handleSwapRelative(from, to)
    }

    override fun handleRemove(extras: Bundle): Boolean {
        val position = extras.getInt(MusicConstants.ARGUMENT_REMOVE_POSITION)
        return queueImpl.handleRemove(position)
    }

    override fun handleRemoveRelative(extras: Bundle): Boolean {
        val position = extras.getInt(MusicConstants.ARGUMENT_REMOVE_POSITION)
        return queueImpl.handleRemoveRelative(position)
    }

    override fun sort() {
        queueImpl.sort()
    }

    override fun shuffle() {
        queueImpl.shuffle()
    }

    private val lastSessionSong = Function<List<MediaEntity>, Pair<List<MediaEntity>, Int>> { list ->
        val idInPlaylist = musicPreferencesUseCase.getLastIdInPlaylist()
        val currentPosition = clamp(list.indexOfFirst { it.idInPlaylist == idInPlaylist }, 0, list.lastIndex)
        Pair(list, currentPosition)
    }

    private fun getCurrentSongOnPlayFromId(songId: Long) = Function<List<MediaEntity>, Pair<List<MediaEntity>, Int>> { list ->
        if (shuffleMode.isEnabled() || songId == -1L){
            Pair(list, 0)
        } else {
            val position = clamp(list.indexOfFirst { it.id == songId }, 0, list.lastIndex)
            Pair(list, position)
        }
    }

    private fun shuffleIfNeeded(songId: Long) = Function<List<MediaEntity>, List<MediaEntity>> { l ->
        var list = l.toList()
        if (shuffleMode.isEnabled()){
            val item = list.firstOrNull { it.id == songId } ?: l
            list = enhancedShuffle.shuffle(list.toMutableList())
            val songPosition = list.indexOf(item)
            if (songPosition != 0 && songPosition != -1){
                list.swap(0, songPosition)
            }
        }
        list
    }

    override fun getCurrentPositionInQueue(): PositionInQueue {
        return queueImpl.currentPositionInQueue()
    }

    override fun onRepeatModeChanged() {
        queueImpl.onRepeatModeChanged()
    }

    override fun playLater(songIds: List<Long>, isPodcast: Boolean): PositionInQueue {
        val currentPositionInQueue = getCurrentPositionInQueue()
        queueImpl.playLater(songIds, isPodcast)
        return when (currentPositionInQueue){
            PositionInQueue.BOTH -> PositionInQueue.FIRST
            PositionInQueue.LAST -> PositionInQueue.IN_MIDDLE
            else -> currentPositionInQueue
        }
    }

    override fun playNext(songIds: List<Long>, isPodcast: Boolean): PositionInQueue {
        val currentPositionInQueue = getCurrentPositionInQueue()
        queueImpl.playNext(songIds, isPodcast)
        return when (currentPositionInQueue){
            PositionInQueue.BOTH -> PositionInQueue.FIRST
            PositionInQueue.LAST -> PositionInQueue.IN_MIDDLE
            else -> currentPositionInQueue
        }
    }

//    override fun moveToPlayNext(idInPlaylist: Int): PositionInQueue {
//        val currentPositionInQueue = getCurrentPositionInQueue()
//        queueImpl.moveToPlayNext(idInPlaylist)
//        return when (currentPositionInQueue){
//            PositionInQueue.BOTH -> PositionInQueue.FIRST
//            else -> PositionInQueue.IN_MIDDLE
//        }
//    }

    override fun updatePodcastPosition(position: Long) {
        val mediaEntity = queueImpl.getCurrentSong()
        if (mediaEntity?.isPodcast == true){
            podcastPosition.set(mediaEntity.id, position)
        }
    }
}
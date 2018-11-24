package dev.olog.msc.data.repository

import dev.olog.msc.data.db.AppDatabase
import dev.olog.msc.domain.entity.PlayingQueueSong
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.gateway.PlayingQueueGateway
import dev.olog.msc.domain.gateway.PodcastGateway
import dev.olog.msc.domain.gateway.SongGateway
import dev.olog.msc.domain.interactor.playing.queue.UpdatePlayingQueueUseCaseRequest
import dev.olog.msc.utils.MediaId
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class PlayingQueueRepository @Inject constructor(
        database: AppDatabase,
        private val songGateway: SongGateway,
        private val podcastGateway: PodcastGateway

) : PlayingQueueGateway {

    private val playingQueueDao = database.playingQueueDao()

    override fun getAll(): Single<List<PlayingQueueSong>> {
        return Single.concat(
                playingQueueDao.getAllAsSongs(
                        songGateway.getAll().firstOrError(),
                        podcastGateway.getAll().firstOrError()
                ).firstOrError(),

                songGateway.getAll().firstOrError()
                        .map { it.mapIndexed { index, song -> song.toPlayingQueueSong(index) } }
        ).filter { it.isNotEmpty() }.firstOrError()
    }

    override fun getAllBlocking(): List<PlayingQueueSong> {
        return playingQueueDao.getAllSongsBlocking(
                songGateway.getAllBlocking(),
                podcastGateway.getAllBlocking()
        )
    }

    override fun observeAll(): Observable<List<PlayingQueueSong>> {
        return playingQueueDao.getAllAsSongs(
                songGateway.getAll().firstOrError(),
                podcastGateway.getAll().firstOrError()
        )
    }

    override fun update(list: List<UpdatePlayingQueueUseCaseRequest>): Completable {
        return playingQueueDao.insert(list)
    }

    override fun observeMiniQueue(): Observable<List<PlayingQueueSong>> {
        return playingQueueDao.observeMiniQueue(
                songGateway.getAll().firstOrError(),
                podcastGateway.getAll().firstOrError()
        )
    }

    override fun getMiniQueueBlocking(): List<PlayingQueueSong> {
        return playingQueueDao.getMiniQueueBlocking(
                songGateway.getAllBlocking(),
                podcastGateway.getAllBlocking()
        )
    }

    override fun updateMiniQueue(tracksId: List<Pair<Int, Long>>) {
        playingQueueDao.updateMiniQueue(tracksId)
    }

    private fun Song.toPlayingQueueSong(progressive: Int): PlayingQueueSong {
        return PlayingQueueSong(
                this.id,
                progressive,
                MediaId.songId(this.id),
                this.artistId,
                this.albumId,
                this.title,
                this.artist,
                this.albumArtist,
                this.album,
                this.image,
                this.duration,
                this.dateAdded,
                this.path,
                this.folder,
                this.discNumber,
                this.trackNumber,
                false
        )
    }

}

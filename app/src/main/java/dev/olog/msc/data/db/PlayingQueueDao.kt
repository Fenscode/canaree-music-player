package dev.olog.msc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.olog.msc.data.entity.MiniQueueEntity
import dev.olog.msc.data.entity.PlayingQueueEntity
import dev.olog.msc.domain.entity.PlayingQueueSong
import dev.olog.msc.domain.entity.Podcast
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.interactor.playing.queue.UpdatePlayingQueueUseCaseRequest
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.MediaIdCategory
import dev.olog.msc.utils.k.extension.toSparseArray
import io.reactivex.*
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers

@Dao
abstract class PlayingQueueDao {

    @Query("""
        SELECT * FROM playing_queue
        ORDER BY progressive
    """)
    internal abstract fun getAllImpl(): Flowable<List<PlayingQueueEntity>>

    @Query("""
        SELECT * FROM playing_queue
        ORDER BY progressive
    """)
    internal abstract fun getAllBlockingImpl(): List<PlayingQueueEntity>

    @Query("DELETE FROM playing_queue")
    internal abstract fun deleteAllImpl()

    @Query("""
        SELECT *
        FROM mini_queue
        ORDER BY timeAdded
    """)
    internal abstract fun getMiniQueueImpl(): Flowable<List<MiniQueueEntity>>

    @Query("""
        SELECT *
        FROM mini_queue
        ORDER BY timeAdded
    """)
    internal abstract fun getMiniQueueBlockingImpl(): List<MiniQueueEntity>

    fun observeMiniQueue(songList: Single<List<Song>>, podcastList: Single<List<Podcast>>)
            : Observable<List<PlayingQueueSong>> {


        return getMiniQueueImpl()
                .subscribeOn(Schedulers.io())
                .toObservable()
                .flatMapSingle { ids ->  Singles.zip(songList, podcastList) { songList, podcastList ->
                    val result = mutableListOf<PlayingQueueSong>()
                    for (item in ids){
                        var song : Any? = songList.firstOrNull { it.id == item.id }
                        if (song == null){
                            song = podcastList.firstOrNull { it.id == item.id }
                        }
                        if (song == null){
                            continue
                        }

                        val itemToAdd = if (song is Song){
                            song.toPlayingQueueSong(item.idInPlaylist, MediaIdCategory.SONGS.toString(), "")
                        } else if (song is Podcast){
                            song.toPlayingQueueSong(item.idInPlaylist, MediaIdCategory.SONGS.toString(), "")
                        } else {
                            throw IllegalArgumentException("must be song or podcast, passed $song")
                        }
                        result.add(itemToAdd)

                    }
                    result.toList()

                } }
    }

    fun getMiniQueueBlocking(songList: List<Song>, podcastList: List<Podcast>)
            : List<PlayingQueueSong> {
        val queue = getMiniQueueBlockingImpl()
        val sparseSongs = songList.toSparseArray { it.id }
        val sparsePodcasts = podcastList.toSparseArray { it.id }
        return queue.mapNotNull {  queueItem ->
            var song : Any? = sparseSongs.get(queueItem.id)
            if (song == null){
                song = sparsePodcasts.get(queueItem.id)
            }
            if (song == null){
                null
            } else {
                if (song is Song){
                    song.toPlayingQueueSong(queueItem.idInPlaylist, MediaIdCategory.SONGS.toString(), "")
                } else if (song is Podcast) {
                    song.toPlayingQueueSong(queueItem.idInPlaylist, MediaIdCategory.SONGS.toString(), "")
                } else {
                    null
                }
            }
        }
    }

    @Transaction
    open fun updateMiniQueue(list: List<Pair<Int, Long>>) {
        deleteMiniQueueImpl()
        insertMiniQueueImpl(list.map { MiniQueueEntity(it.first, it.second, System.nanoTime()) })
    }

    @Insert
    internal abstract fun insertAllImpl(list: List<PlayingQueueEntity>)

    @Query("DELETE FROM mini_queue")
    internal abstract fun deleteMiniQueueImpl()

    @Insert
    internal abstract fun insertMiniQueueImpl(list: List<MiniQueueEntity>)

    fun getAllSongsBlocking(songList: List<Song>, podcastList: List<Podcast>)
            : List<PlayingQueueSong> {
        val queue = getAllBlockingImpl()
        val sparseSongs = songList.toSparseArray { it.id }
        val sparsePodcasts = podcastList.toSparseArray { it.id }
        return queue.mapNotNull {  queueItem ->
            var song : Any? = sparseSongs.get(queueItem.songId)
            if (song == null){
                song = sparsePodcasts.get(queueItem.songId)
            }
            if (song == null){
                null
            } else {
                if (song is Song){
                    song.toPlayingQueueSong(queueItem.idInPlaylist, queueItem.category, queueItem.categoryValue)
                } else if (song is Podcast) {
                    song.toPlayingQueueSong(queueItem.idInPlaylist, queueItem.category, queueItem.categoryValue)
                } else {
                    null
                }
            }
        }
    }

    fun getAllAsSongs(songList: Single<List<Song>>, podcastList: Single<List<Podcast>>)
            : Observable<List<PlayingQueueSong>> {

        return this.getAllImpl()
                .toObservable()
                .flatMapSingle { ids ->  Singles.zip(songList, podcastList) { songList, podcastList ->

                    val result = mutableListOf<PlayingQueueSong>()
                    for (item in ids){
                        var song : Any? = songList.firstOrNull { it.id == item.songId }
                        if (song == null){
                            song = podcastList.firstOrNull { it.id == item.songId }
                        }
                        if (song == null){
                            continue
                        }

                        val itemToAdd = if (song is Song){
                            song.toPlayingQueueSong(item.idInPlaylist, item.category, item.categoryValue)
                        } else if (song is Podcast){
                            song.toPlayingQueueSong(item.idInPlaylist, item.category, item.categoryValue)
                        } else {
                            throw IllegalArgumentException("must be song or podcast, passed $song")
                        }
                        result.add(itemToAdd)

                    }
                    result.toList()

                } }
    }

    fun insert(list: List<UpdatePlayingQueueUseCaseRequest>) : Completable {

        return Single.fromCallable { deleteAllImpl() }
                .map { list.map {
                    val (mediaId, songId, idInPlaylist) = it
                    PlayingQueueEntity(
                            songId = songId,
                            category = mediaId.category.toString(),
                            categoryValue = mediaId.categoryValue,
                            idInPlaylist = idInPlaylist
                    ) }
                }.flatMapCompletable { queueList -> CompletableSource { insertAllImpl(queueList) } }
    }

    private fun Song.toPlayingQueueSong(idInPlaylist: Int, category: String, categoryValue: String)
            : PlayingQueueSong {

        return PlayingQueueSong(
                this.id,
                idInPlaylist,
                MediaId.createCategoryValue(MediaIdCategory.valueOf(category), categoryValue),
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

    private fun Podcast.toPlayingQueueSong(idInPlaylist: Int, category: String, categoryValue: String)
            : PlayingQueueSong {

        return PlayingQueueSong(
                this.id,
                idInPlaylist,
                MediaId.createCategoryValue(MediaIdCategory.valueOf(category), categoryValue),
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
                true
        )
    }


}
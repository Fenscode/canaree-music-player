package dev.olog.msc.data.repository

import android.provider.MediaStore
import com.squareup.sqlbrite3.BriteContentResolver
import dev.olog.msc.constants.AppConstants
import dev.olog.msc.data.db.AppDatabase
import dev.olog.msc.data.mapper.toAlbum
import dev.olog.msc.domain.entity.Album
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.gateway.AlbumGateway
import dev.olog.msc.domain.gateway.SongGateway
import dev.olog.msc.utils.k.extension.emitThenDebounce
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject
import javax.inject.Singleton

private val MEDIA_STORE_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

@Singleton
class AlbumRepository @Inject constructor(
        private val rxContentResolver: BriteContentResolver,
        private val songGateway: SongGateway,
        appDatabase: AppDatabase

) : BaseRepository<Album, Long>(), AlbumGateway {

    private val lastPlayedDao = appDatabase.lastPlayedAlbumDao()

    override fun queryAllData(): Observable<List<Album>> {
        return rxContentResolver.createQuery(
                MEDIA_STORE_URI, arrayOf("count(*)"), null,
                null, null, false
        ).mapToOne { 0 }
                .flatMap { songGateway.getAll() }
                .map { songList ->

                    songList.asSequence()
                            .filter { it.album != AppConstants.UNKNOWN }
                            .distinctBy { it.albumId }
                            .map { song ->
                                song.toAlbum(songList.count { it.albumId == song.albumId })
                            }.sortedBy { it.title.toLowerCase() }
                            .toList()
                }.onErrorReturn { listOf() }
    }

    override fun getByParamImpl(list: List<Album>, param: Long): Album {
        return list.first { it.id == param }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun observeSongListByParam(albumId: Long): Observable<List<Song>> {
        val observable = songGateway.getAll().map { it.filter { it.albumId == albumId } }

        return observable.emitThenDebounce()
    }

    override fun observeByArtist(artistId: Long): Observable<List<Album>> {
        return getAll().map { it.filter { it.artistId == artistId } }
    }

    override fun getLastPlayed(): Observable<List<Album>> {
        val observable = Observables.combineLatest(
                getAll(),
                lastPlayedDao.getAll().toObservable(),
                { all, lastPlayed ->

            if (all.size < 10) {
                listOf() // too few album to show recents
            } else {
                lastPlayed.asSequence()
                        .mapNotNull { last -> all.firstOrNull { it.id == last.id } }
                        .take(10)
                        .toList()
            }
        })
        return observable.emitThenDebounce()
    }

    override fun addLastPlayed(item: Album): Completable {
        return lastPlayedDao.insertOne(item)
    }
}
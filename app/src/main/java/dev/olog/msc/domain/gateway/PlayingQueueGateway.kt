package dev.olog.msc.domain.gateway

import dev.olog.msc.domain.entity.PlayingQueueSong
import dev.olog.msc.domain.interactor.playing.queue.UpdatePlayingQueueUseCaseRequest
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PlayingQueueGateway {

    fun observeAll(): Observable<List<PlayingQueueSong>>

    fun getAll(): Single<List<PlayingQueueSong>>
    fun getAllBlocking(): List<PlayingQueueSong>

    fun update(list: List<UpdatePlayingQueueUseCaseRequest>): Completable

    fun observeMiniQueue(): Observable<List<PlayingQueueSong>>
    fun getMiniQueueBlocking(): List<PlayingQueueSong>
    fun updateMiniQueue(tracksId: List<Pair<Int, Long>>)

}
package dev.olog.msc.domain.interactor.playing.queue

import dev.olog.msc.domain.entity.PlayingQueueSong
import dev.olog.msc.domain.gateway.PlayingQueueGateway
import javax.inject.Inject

class GetMiniQueueBlockingUseCase @Inject constructor(
        private val gateway: PlayingQueueGateway
) {

    fun execute(): List<PlayingQueueSong> {
        return gateway.getMiniQueueBlocking()
    }
}
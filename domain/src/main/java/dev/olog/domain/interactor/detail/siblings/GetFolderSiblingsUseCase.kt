package dev.olog.domain.interactor.detail.siblings

import dev.olog.domain.entity.Folder
import dev.olog.domain.executor.IoScheduler
import dev.olog.domain.gateway.FolderGateway
import dev.olog.domain.interactor.base.FlowableUseCaseWithParam
import dev.olog.shared.MediaIdHelper
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import javax.inject.Inject

class GetFolderSiblingsUseCase @Inject constructor(
        schedulers: IoScheduler,
        private val gateway: FolderGateway

) : FlowableUseCaseWithParam<List<Folder>, String>(schedulers) {


    override fun buildUseCaseObservable(mediaId: String) : Flowable<List<Folder>> {
        val categoryValue = MediaIdHelper.extractCategoryValue(mediaId)

        return gateway.getAll()
                .flatMapSingle {
                    it.toFlowable().filter { it.path != categoryValue }.toList()
                }
                .map { if (it.size > 1) it else listOf() }
    }
}
package dev.olog.msc.domain.interactor.all.sorted

import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.entity.SortArranging
import dev.olog.msc.domain.executors.IoScheduler
import dev.olog.msc.domain.interactor.all.GetSongListByParamUseCase
import dev.olog.msc.domain.interactor.all.sorted.util.GetSortArrangingUseCase
import dev.olog.msc.domain.interactor.all.sorted.util.GetSortOrderUseCase
import dev.olog.msc.domain.interactor.base.ObservableUseCaseWithParam
import dev.olog.msc.utils.Comparators.getAscendingComparator
import dev.olog.msc.utils.Comparators.getDescendingComparator
import dev.olog.msc.utils.MediaId
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import java.text.Collator
import javax.inject.Inject

class GetSortedSongListByParamUseCase @Inject constructor(
        schedulers: IoScheduler,
        private val getSongListByParamUseCase: GetSongListByParamUseCase,
        private val getSortOrderUseCase: GetSortOrderUseCase,
        private val getSortArrangingUseCase: GetSortArrangingUseCase,
        private val collator: Collator

) : ObservableUseCaseWithParam<List<Song>, MediaId>(schedulers){

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun buildUseCaseObservable(mediaId: MediaId): Observable<List<Song>> {
        return Observables.combineLatest(
                getSongListByParamUseCase.execute(mediaId),
                getSortOrderUseCase.execute(mediaId),
                getSortArrangingUseCase.execute()) { songList, sortOrder, arranging ->
                    if (arranging == SortArranging.ASCENDING){
                        songList.sortedWith(getAscendingComparator(collator, sortOrder))
                    } else {
                        songList.sortedWith(getDescendingComparator(collator, sortOrder))
                    }
                }
    }

}
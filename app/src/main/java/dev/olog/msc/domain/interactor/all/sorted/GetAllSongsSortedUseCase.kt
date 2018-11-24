package dev.olog.msc.domain.interactor.all.sorted

import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.entity.SortArranging
import dev.olog.msc.domain.executors.IoScheduler
import dev.olog.msc.domain.gateway.prefs.AppPreferencesGateway
import dev.olog.msc.domain.interactor.all.GetAllSongsUseCase
import dev.olog.msc.domain.interactor.base.ObservableUseCase
import dev.olog.msc.utils.Comparators.getAscendingComparator
import dev.olog.msc.utils.Comparators.getDescendingComparator
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import java.text.Collator
import javax.inject.Inject

class GetAllSongsSortedUseCase @Inject constructor(
        schedulers: IoScheduler,
        private val getAllUseCase: GetAllSongsUseCase,
        private val appPrefsGateway: AppPreferencesGateway,
        private val collator: Collator


) : ObservableUseCase<List<Song>>(schedulers){

    override fun buildUseCaseObservable(): Observable<List<Song>> {
        return Observables.combineLatest(
                getAllUseCase.execute(),
                appPrefsGateway.observeAllTracksSortOrder()
            ) { tracks, order ->
                val (sort, arranging) = order

                if (arranging == SortArranging.ASCENDING){
                    tracks.sortedWith(getAscendingComparator(collator, sort))
                } else {
                    tracks.sortedWith(getDescendingComparator(collator, sort))
                }
            }
    }

}
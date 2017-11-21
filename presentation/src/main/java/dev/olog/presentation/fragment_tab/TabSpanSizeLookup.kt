package dev.olog.presentation.fragment_tab

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import dev.olog.presentation.activity_main.TabViewPagerAdapter.Companion.ALBUM
import dev.olog.presentation.activity_main.TabViewPagerAdapter.Companion.ARTIST
import dev.olog.presentation.activity_main.TabViewPagerAdapter.Companion.FOLDER
import dev.olog.presentation.activity_main.TabViewPagerAdapter.Companion.GENRE
import dev.olog.presentation.activity_main.TabViewPagerAdapter.Companion.PLAYLIST
import dev.olog.presentation.activity_main.TabViewPagerAdapter.Companion.SONG
import dev.olog.presentation.dagger.ActivityContext
import dev.olog.presentation.dagger.PerFragment
import dev.olog.presentation.utils.isPortrait
import javax.inject.Inject

@PerFragment
class TabSpanSizeLookup @Inject constructor(
        @ActivityContext context: Context,
        private val source: Int

) : GridLayoutManager.SpanSizeLookup() {

    companion object {
        const val SPAN_COUNT = 12
    }

    private val isPortrait = context.isPortrait

    private fun getSpan(): Int{
        return when {
            source == SONG && isPortrait -> SPAN_COUNT
            source == SONG && !isPortrait -> SPAN_COUNT / 2
            (source == ALBUM || source == ARTIST) && isPortrait -> SPAN_COUNT / 2
            (source == FOLDER || source == PLAYLIST || source == GENRE) && isPortrait -> SPAN_COUNT / 3
            else -> SPAN_COUNT / 4
        }
    }

    override fun getSpanSize(position: Int): Int = getSpan()

}
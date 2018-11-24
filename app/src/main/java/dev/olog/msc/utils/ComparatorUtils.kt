package dev.olog.msc.utils

import androidx.annotation.Keep
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.entity.SortType
import dev.olog.msc.music.service.model.MediaEntity
import java.text.Collator

@Suppress("unused")
@Keep
object Comparators {

    fun getAscendingComparator(collator: Collator, sortType: SortType): Comparator<Song> {
        return when (sortType){
            SortType.TITLE -> Comparator { o1, o2 -> collator.safeCompare(o1.title, o2.title) }
            SortType.ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o1.artist, o2.artist) }
            SortType.ALBUM -> Comparator { o1, o2 -> collator.safeCompare(o1.album, o2.album) }
            SortType.ALBUM_ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o1.albumArtist, o2.albumArtist) }
            SortType.DURATION -> compareBy { it.duration }
            SortType.RECENTLY_ADDED -> compareByDescending { it.dateAdded }
            SortType.TRACK_NUMBER -> ComparatorUtils.getAscendingTrackNumberComparator()
            SortType.CUSTOM -> compareBy { 0 }
        }
    }

    fun getDescendingComparator(collator: Collator, sortType: SortType): Comparator<Song> {
        return when (sortType){
            SortType.TITLE -> Comparator { o1, o2 -> collator.safeCompare(o2.title, o1.title) }
            SortType.ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o2.artist, o1.artist) }
            SortType.ALBUM -> Comparator { o1, o2 -> collator.safeCompare(o2.album, o1.album) }
            SortType.ALBUM_ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o2.albumArtist, o1.albumArtist) }
            SortType.DURATION -> compareByDescending { it.duration }
            SortType.RECENTLY_ADDED -> compareBy { it.dateAdded }
            SortType.TRACK_NUMBER -> ComparatorUtils.getDescendingTrackNumberComparator()
            SortType.CUSTOM -> compareByDescending { 0 }
        }
    }

    fun getAscendingComparator(sortType: SortType, collator: Collator): Comparator<MediaEntity> {
        return when (sortType){
            SortType.TITLE -> Comparator { o1, o2 -> collator.safeCompare(o1.title, o2.title) }
            SortType.ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o1.artist, o2.artist) }
            SortType.ALBUM -> Comparator { o1, o2 -> collator.safeCompare(o1.album, o2.album) }
            SortType.ALBUM_ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o1.albumArtist, o2.albumArtist) }
            SortType.DURATION -> compareBy { it.duration }
            SortType.RECENTLY_ADDED -> compareByDescending { it.dateAdded }
            SortType.TRACK_NUMBER -> ComparatorUtils.getMediaEntityAscendingTrackNumberComparator()
            SortType.CUSTOM -> compareBy { 0 }
        }
    }

    fun getDescendingComparator(sortType: SortType, collator: Collator): Comparator<MediaEntity> {
        return when (sortType){
            SortType.TITLE -> Comparator { o1, o2 -> collator.safeCompare(o2.title, o1.title) }
            SortType.ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o2.artist, o1.artist) }
            SortType.ALBUM -> Comparator { o1, o2 -> collator.safeCompare(o2.album, o1.album) }
            SortType.ALBUM_ARTIST -> Comparator { o1, o2 -> collator.safeCompare(o2.albumArtist, o1.albumArtist) }
            SortType.DURATION -> compareByDescending { it.duration }
            SortType.RECENTLY_ADDED -> compareBy { it.dateAdded }
            SortType.TRACK_NUMBER -> ComparatorUtils.getMediaEntityDescendingTrackNumberComparator()
            SortType.CUSTOM -> compareByDescending { 0 }
        }
    }


}

object ComparatorUtils {

    fun getAscendingTrackNumberComparator(): Comparator<Song> {
        return Comparator { o1: Song, o2: Song ->
            val tmp = o1.discNumber - o2.discNumber
            if (tmp == 0){
                o1.trackNumber - o2.trackNumber
            } else {
                tmp
            }
        }
    }

    fun getDescendingTrackNumberComparator(): Comparator<Song> {
        return Comparator { o1: Song, o2: Song ->
            val tmp = o2.discNumber - o1.discNumber
            if (tmp == 0){
                o2.trackNumber - o1.trackNumber
            } else {
                tmp
            }
        }
    }

    fun getMediaEntityAscendingTrackNumberComparator(): Comparator<MediaEntity> {
        return Comparator { o1: MediaEntity, o2: MediaEntity ->
            val tmp = o1.discNumber - o2.discNumber
            if (tmp == 0){
                o1.trackNumber - o2.trackNumber
            } else {
                tmp
            }
        }
    }

    fun getMediaEntityDescendingTrackNumberComparator(): Comparator<MediaEntity> {
        return Comparator { o1: MediaEntity, o2: MediaEntity ->
            val tmp = o2.discNumber - o1.discNumber
            if (tmp == 0){
                o2.trackNumber - o1.trackNumber
            } else {
                tmp
            }
        }
    }

}
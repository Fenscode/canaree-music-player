package dev.olog.data

import android.content.res.Resources

object DataConstants {

    const val LAST_ADDED_ID: Long = -5000
    const val FAVORITE_LIST_ID: Long = -4000
    const val HISTORY_LIST_ID: Long = -3000

    const val UNKNOWN = "<unknown>"
    lateinit var UNKNOWN_ALBUM: String
    lateinit var UNKNOWN_ARTIST: String

    fun init(resources: Resources){
        UNKNOWN_ALBUM = resources.getString(R.string.unknown_album)
        UNKNOWN_ARTIST = resources.getString(R.string.unknown_artist)
    }

}
package dev.olog.msc.domain.entity

data class PodcastArtist (
        val id: Long,
        val name: String,
        val albumArtist: String,
        val songs: Int,
        val albums: Int,
        val image: String
)
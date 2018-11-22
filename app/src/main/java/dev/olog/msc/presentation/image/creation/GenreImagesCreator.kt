package dev.olog.msc.presentation.image.creation

import android.content.Context
import android.provider.MediaStore
import dev.olog.msc.dagger.qualifier.ApplicationContext
import dev.olog.msc.data.repository.util.CommonQuery
import dev.olog.msc.domain.entity.Genre
import dev.olog.msc.presentation.image.creation.impl.MergedImagesCreator
import dev.olog.msc.utils.assertBackgroundThread
import dev.olog.msc.utils.img.ImagesFolderUtils
import io.reactivex.Flowable
import javax.inject.Inject

private val MEDIA_STORE_URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI

class GenreImagesCreator @Inject constructor(
        @ApplicationContext private val ctx: Context

) {


    fun execute(genres: List<Genre>) : Flowable<*> {
        return Flowable.fromIterable(genres)
                .observeOn(ImagesThreadPool.scheduler)
                .parallel()
                .runOn(ImagesThreadPool.scheduler)
                .map {
                    val uri = MediaStore.Audio.Genres.Members.getContentUri("external", it.id)
                    Pair(it, CommonQuery.extractAlbumIdsFromSongs(ctx.contentResolver, uri))
                }
                .map { (genre, albumsId) -> try {
                    makeImage(genre, albumsId)
                } catch (ex: Exception){ false }
                }
                .sequential()
                .buffer(10)
                .filter { it.reduce { acc, curr -> acc || curr } }
                .doOnNext {
                    ctx.contentResolver.notifyChange(MEDIA_STORE_URI, null)
                }
    }

    private fun makeImage(genre: Genre, albumsId: List<Long>) : Boolean {
        assertBackgroundThread()
        val folderName = ImagesFolderUtils.getFolderName(ImagesFolderUtils.GENRE)
        return MergedImagesCreator.makeImages(ctx, albumsId, folderName, "${genre.id}")
    }

}
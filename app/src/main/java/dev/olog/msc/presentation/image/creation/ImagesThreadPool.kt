package dev.olog.msc.presentation.image.creation

import dev.olog.msc.utils.k.extension.clamp
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object ImagesThreadPool{

    private val threads = Runtime.getRuntime().availableProcessors()
    private val threadPoolExecutor by lazy { Executors.newFixedThreadPool(clamp(threads / 2, 1, 2)) }
    val scheduler by lazy { Schedulers.from(threadPoolExecutor) }
}
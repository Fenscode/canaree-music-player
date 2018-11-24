package dev.olog.msc.utils.k.extension

import android.util.LongSparseArray
import androidx.core.util.forEach

fun <T> LongSparseArray<T>.toList(): List<T>{
    val list = mutableListOf<T>()

    this.forEach { _, value -> list.add(value) }

    return list
}

fun <T> LongSparseArray<T>.toggle(key: Long, item: T){
    val current = this.get(key)
    if (current == null){
        this.append(key, item)
    } else {
        this.remove(key)
    }
}

fun <T> List<T>.toSparseArray(keySelector: ((T) -> Long)): LongSparseArray<T> {
    val result = LongSparseArray<T>(this.size)
    for (item in this){
        result.append(keySelector(item), item)
    }
    return result
}
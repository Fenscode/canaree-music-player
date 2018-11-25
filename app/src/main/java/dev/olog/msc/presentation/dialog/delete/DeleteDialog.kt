package dev.olog.msc.presentation.dialog.delete

import android.content.Context
import android.content.DialogInterface
import dev.olog.msc.R
import dev.olog.msc.presentation.base.BaseDialog
import dev.olog.msc.presentation.utils.lazyFast
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.MediaIdCategory
import dev.olog.msc.utils.k.extension.asHtml
import dev.olog.msc.utils.k.extension.withArguments
import io.reactivex.Completable
import javax.inject.Inject

class DeleteDialog: BaseDialog() {

    companion object {
        const val TAG = "DeleteDialog"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"
        const val ARGUMENTS_LIST_SIZE = "$TAG.arguments.list_size"
        const val ARGUMENTS_ITEM_TITLE = "$TAG.arguments.item_title"

        @JvmStatic
        fun newInstance(mediaId: MediaId, listSize: Int, itemTitle: String): DeleteDialog {
            return DeleteDialog().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId.toString(),
                    ARGUMENTS_LIST_SIZE to listSize,
                    ARGUMENTS_ITEM_TITLE to itemTitle
            )
        }
    }

    private val listSize: Int by lazyFast { arguments!!.getInt(DeleteDialog.ARGUMENTS_LIST_SIZE) }
    private val mediaId: MediaId by lazyFast {
        val mediaId = arguments!!.getString(DeleteDialog.ARGUMENTS_MEDIA_ID)!!
        MediaId.fromString(mediaId)
    }
    private val title: String by lazyFast {
        arguments!!.getString(DeleteDialog.ARGUMENTS_ITEM_TITLE)!!
    }
    @Inject lateinit var presenter: DeleteDialogPresenter

    override fun title(context: Context): CharSequence {
        return context.getString(R.string.popup_delete)
    }

    override fun message(context: Context): CharSequence {
        return createMessage().asHtml()
    }

    override fun negativeButtonMessage(context: Context): Int {
        return R.string.popup_negative_no
    }

    override fun positiveButtonMessage(context: Context): Int {
        return R.string.popup_positive_delete
    }

    override fun successMessage(context: Context): CharSequence {
        return when (mediaId.category) {
            MediaIdCategory.PLAYLISTS -> context.getString(R.string.playlist_x_deleted, title)
            MediaIdCategory.SONGS -> context.getString(R.string.song_x_deleted, title)
            else -> context.resources.getQuantityString(R.plurals.xx_songs_added_to_playlist_y,
                    listSize, listSize, title)
        }
    }

    override fun failMessage(context: Context): CharSequence {
        return context.getString(R.string.popup_error_message)
    }

    override fun positiveAction(dialogInterface: DialogInterface, which: Int): Completable {
        return presenter.execute(mediaId)
    }

    private fun createMessage() : String {
        val itemTitle = arguments!!.getString(ARGUMENTS_ITEM_TITLE)

        return when {
            mediaId.isAll || mediaId.isLeaf -> getString(R.string.delete_song_y, itemTitle)
            mediaId.isPlaylist -> getString(R.string.delete_playlist_y, itemTitle)
            else -> context!!.resources.getQuantityString(R.plurals.delete_xx_songs_from_y, listSize, listSize)
        }
    }

}
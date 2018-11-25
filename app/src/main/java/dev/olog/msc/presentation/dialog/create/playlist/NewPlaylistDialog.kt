package dev.olog.msc.presentation.dialog.create.playlist

import android.content.Context
import dev.olog.msc.R
import dev.olog.msc.presentation.base.BaseEditTextDialog
import dev.olog.msc.presentation.utils.lazyFast
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.withArguments
import io.reactivex.Completable
import javax.inject.Inject

class NewPlaylistDialog : BaseEditTextDialog() {

    companion object {
        const val TAG = "NewPlaylistDialog"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"
        const val ARGUMENTS_LIST_SIZE = "$TAG.arguments.list_size"
        const val ARGUMENTS_ITEM_TITLE = "$TAG.arguments.item_title"

        @JvmStatic
        fun newInstance(mediaId: MediaId, listSize: Int, itemTitle: String): NewPlaylistDialog {
            return NewPlaylistDialog().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId.toString(),
                    ARGUMENTS_LIST_SIZE to listSize,
                    ARGUMENTS_ITEM_TITLE to itemTitle
            )
        }
    }

    @Inject lateinit var presenter: NewPlaylistDialogPresenter
    private val listSize: Int by lazyFast {
        arguments!!.getInt(NewPlaylistDialog.ARGUMENTS_LIST_SIZE)
    }
    private val mediaId: MediaId by lazyFast {
        val mediaId = arguments!!.getString(NewPlaylistDialog.ARGUMENTS_MEDIA_ID)!!
        MediaId.fromString(mediaId)
    }
    private val title: String by lazyFast {
        arguments!!.getString(NewPlaylistDialog.ARGUMENTS_ITEM_TITLE)!!
    }

    override fun title(): Int = R.string.popup_new_playlist

    override fun positiveButtonMessage(context: Context): Int {
        return R.string.popup_positive_create
    }

    override fun negativeButtonMessage(context: Context): Int {
        return R.string.popup_negative_cancel
    }

    override fun errorMessageForBlankForm(): Int = R.string.popup_playlist_name_not_valid

    override fun errorMessageForInvalidForm(currentValue: String): Int = R.string.popup_playlist_name_already_exist

    override fun positiveAction(currentValue: String): Completable {
        return presenter.execute(mediaId, currentValue)
    }

    override fun initialTextFieldValue(): String = ""

    override fun isStringValid(string: String): Boolean = presenter.isStringValid(mediaId, string)

    override fun successMessage(context: Context, currentValue: String): CharSequence {
        if (mediaId.isPlayingQueue){
            return context.getString(R.string.queue_saved_as_playlist, currentValue)
        }
        if (mediaId.isLeaf){
            return context.getString(R.string.added_song_x_to_playlist_y, title, currentValue)
        }
        return context.resources.getQuantityString(R.plurals.xx_songs_added_to_playlist_y,
                listSize, listSize, currentValue)
    }

    override fun negativeMessage(context: Context, currentValue: String): CharSequence {
        return context.getString(R.string.popup_error_message)
    }
}
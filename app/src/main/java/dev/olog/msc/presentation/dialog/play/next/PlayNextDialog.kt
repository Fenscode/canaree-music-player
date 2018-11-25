package dev.olog.msc.presentation.dialog.play.next

import android.content.Context
import android.content.DialogInterface
import android.support.v4.media.session.MediaControllerCompat
import dev.olog.msc.R
import dev.olog.msc.presentation.base.BaseDialog
import dev.olog.msc.presentation.utils.lazyFast
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.k.extension.asHtml
import dev.olog.msc.utils.k.extension.withArguments
import io.reactivex.Completable
import javax.inject.Inject

class PlayNextDialog : BaseDialog() {

    companion object {
        const val TAG = "PlayNextDialog"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"
        const val ARGUMENTS_LIST_SIZE = "$TAG.arguments.list_size"
        const val ARGUMENTS_ITEM_TITLE = "$TAG.arguments.item_title"

        @JvmStatic
        fun newInstance(mediaId: MediaId, listSize: Int, itemTitle: String): PlayNextDialog {
            return PlayNextDialog().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId.toString(),
                    ARGUMENTS_LIST_SIZE to listSize,
                    ARGUMENTS_ITEM_TITLE to itemTitle
            )
        }
    }

    private val mediaId: MediaId by lazyFast {
        val mediaId = arguments!!.getString(PlayNextDialog.ARGUMENTS_MEDIA_ID)!!
        MediaId.fromString(mediaId)
    }
    private val listSize: Int by lazyFast { arguments!!.getInt(PlayNextDialog.ARGUMENTS_LIST_SIZE) }
    private val title: String by lazyFast { arguments!!.getString(PlayNextDialog.ARGUMENTS_ITEM_TITLE) }
    @Inject lateinit var presenter: PlayNextDialogPresenter

    override fun title(context: Context): CharSequence {
        return context.getString(R.string.popup_play_next)
    }

    override fun message(context: Context): CharSequence {
        return createMessage().asHtml()
    }

    override fun negativeButtonMessage(context: Context): Int {
        return R.string.popup_negative_cancel
    }

    override fun positiveButtonMessage(context: Context): Int {
        return R.string.popup_positive_ok
    }

    override fun successMessage(context: Context): CharSequence {
        return if (mediaId.isLeaf){
            context.getString(R.string.song_x_added_to_play_next, title)
        } else context.resources.getQuantityString(R.plurals.xx_songs_added_to_play_next, listSize, listSize)
    }

    override fun failMessage(context: Context): CharSequence {
        return context.getString(R.string.popup_error_message)
    }

    override fun positiveAction(dialogInterface: DialogInterface, which: Int): Completable {
        val mediaController = MediaControllerCompat.getMediaController(activity!!)
        return presenter.execute(mediaId, mediaController)
    }

    private fun createMessage() : String {
        if (mediaId.isAll || mediaId.isLeaf){
            return getString(R.string.add_song_x_to_play_next, title)
        }
        return context!!.resources.getQuantityString(R.plurals.add_xx_songs_to_play_next, listSize, listSize)
    }

}
package dev.olog.presentation.activity_preferences.categories

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import dev.olog.presentation.R
import dev.olog.presentation._base.BaseDialogFragment
import dev.olog.presentation.utils.extension.makeDialog
import org.jetbrains.anko.toast
import javax.inject.Inject

class LibraryCategoriesFragment : BaseDialogFragment() {

    companion object {
        const val TAG = "LibraryCategoriesFragment"

        fun newInstance(): LibraryCategoriesFragment {
            return LibraryCategoriesFragment()
        }
    }

    @Inject lateinit var presenter: LibraryCategoriesFragmentPresenter
    private lateinit var adapter: LibraryCategoriesFragmentAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity!!)
        val view : View = inflater.inflate(R.layout.dialog_list, null, false)

        val builder = AlertDialog.Builder(context)
                .setTitle(R.string.prefs_library_categories_title)
                .setView(view)
                .setNeutralButton(R.string.popup_neutral_reset, null)
                .setNegativeButton(R.string.popup_negative_cancel, null)
                .setPositiveButton(R.string.popup_positive_save, null)

        val list = view.findViewById<RecyclerView>(R.id.list)
        adapter = LibraryCategoriesFragmentAdapter(activity!!, presenter.getDataSet().toMutableList())
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(context)
        adapter.touchHelper.attachToRecyclerView(list)

        val dialog = builder.makeDialog()

        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                    val defaultData = presenter.getDefaultDataSet()
                    adapter.updateDataSet(defaultData)
                }

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val allDisabled = adapter.data.all { !it.enabled }
                    if (allDisabled){
                        showErrorMessage()
                    } else {
                        presenter.setDataSet(adapter.data)
                        activity!!.setResult(Activity.RESULT_OK)
                        dismiss()
                    }
                }

        return dialog
    }

    private fun showErrorMessage(){
        activity!!.toast(R.string.prefs_library_categories_error)
    }

}
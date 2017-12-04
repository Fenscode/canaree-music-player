package dev.olog.presentation.fragment_recently_added

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.olog.presentation.R
import dev.olog.presentation._base.BaseFragment
import dev.olog.presentation.utils.extension.subscribe
import dev.olog.presentation.utils.extension.withArguments
import kotlinx.android.synthetic.main.fragment_recently_added.view.*
import javax.inject.Inject

class RecentlyAddedFragment : BaseFragment() {

    companion object {
        const val TAG = "RecentlyAddedFragment"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"


        fun newInstance(mediaId: String): RecentlyAddedFragment {
            return RecentlyAddedFragment().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId)
        }
    }

    @Inject lateinit var viewModel: RecentlyAddedFragmentViewModel
    @Inject lateinit var adapter: RecentlyAddedFragmentAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.data.subscribe(this, adapter::updateDataSet)
    }

    override fun onViewBound(view: View, savedInstanceState: Bundle?) {
        view.list.adapter = adapter
        view.list.layoutManager = LinearLayoutManager(context!!)
    }

    override fun provideView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_recently_added, container, false)
    }
}
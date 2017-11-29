package dev.olog.presentation._base

import android.arch.lifecycle.Lifecycle
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dev.olog.presentation.model.DisplayableItem

abstract class BaseMapAdapter<E: Enum<E>, Model> (
        lifecycle: Lifecycle

) : RecyclerView.Adapter<DataBoundViewHolder<*>>() {

    private val controller = BaseMapAdapterController(this)

    init {
        lifecycle.addObserver(controller)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, parent, false)
        val viewHolder = DataBoundViewHolder(binding)
        initViewHolderListeners(viewHolder, viewType)
        return viewHolder
    }

    protected abstract fun initViewHolderListeners(viewHolder: DataBoundViewHolder<*>, viewType: Int)

    override fun onBindViewHolder(holder: DataBoundViewHolder<*>, position: Int) {
        bind(holder.binding, controller[position], position)
        holder.binding.executePendingBindings()
    }

    protected abstract fun bind(binding: ViewDataBinding,
                                item: DisplayableItem,
                                position: Int)

}
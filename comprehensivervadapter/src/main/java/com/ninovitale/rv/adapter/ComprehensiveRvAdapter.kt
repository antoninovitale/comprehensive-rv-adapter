package com.ninovitale.rv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.DEFAULT
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.FAILURE
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.LOADING
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.SUCCESS
import com.ninovitale.rv.adapter.item.BaseRecyclerViewItem
import com.ninovitale.rv.adapter.item.EmptyRecyclerViewItem
import com.ninovitale.rv.adapter.item.FooterRecyclerViewItem
import com.ninovitale.rv.adapter.item.HeaderRecyclerViewItem
import com.ninovitale.rv.adapter.item.LoadingMoreRecyclerViewItem
import com.ninovitale.rv.adapter.item.LoadingRecyclerViewItem
import com.ninovitale.rv.adapter.vh.BaseViewHolder
import com.ninovitale.rv.adapter.vh.LoadingMoreViewHolder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * RecyclerView.Adapter that can handle click, single selection, multiple view types, nested items and loading more content.
 */
abstract class ComprehensiveRvAdapter() : RecyclerView.Adapter<BaseViewHolder>() {
    private var mutableItems: MutableList<BaseRecyclerViewItem> = mutableListOf()
    private var onItemClickListener: OnItemClickListener? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var selectedPosition = NO_SELECTION
    private var loadMoreThreshold = LOAD_MORE_THRESHOLD
    private var loadingMore = AtomicBoolean(false)
    private val headerRecyclerViewItem = HeaderRecyclerViewItem()
    private val footerRecyclerViewItem = FooterRecyclerViewItem()
    private val emptyRecyclerViewItem = EmptyRecyclerViewItem()
    private val loadingMoreRecyclerViewItem = LoadingMoreRecyclerViewItem()
    private val loadingRecyclerViewItem = LoadingRecyclerViewItem()
    private var selectable = false
    private var hasHeader = false
    private var hasFooter = false
    private val lock = Any()

    constructor(items: List<BaseRecyclerViewItem>) : this() {
        initItems(items)
    }

    protected fun addHeader() {
        synchronized(lock) {
            val indexOfHeader = mutableItems.indexOf(headerRecyclerViewItem)
            if (indexOfHeader == -1) {
                mutableItems.add(0, headerRecyclerViewItem)
                notifyItemInserted(0)
            }
        }
        hasHeader = true
    }

    protected fun removeHeader() {
        synchronized(lock) {
            val indexOfHeader = mutableItems.indexOf(headerRecyclerViewItem)
            if (indexOfHeader != -1) {
                mutableItems.remove(headerRecyclerViewItem)
                notifyItemRemoved(indexOfHeader)
            }
        }
        hasHeader = false
    }

    protected fun addFooter() {
        synchronized(lock) {
            val indexOfFooter = indexOf(footerRecyclerViewItem)
            if (indexOfFooter == -1) {
                val indexOfLoadingMoreView = indexOf(loadingMoreRecyclerViewItem)
                if (indexOfLoadingMoreView == -1) {
                    mutableItems.add(footerRecyclerViewItem)
                    notifyItemInserted(mutableItems.size - 1)
                } else {
                    val index = indexOfLoadingMoreView - 1
                    mutableItems.add(index, footerRecyclerViewItem)
                    notifyItemInserted(index)
                }
            }
        }
        hasFooter = true
    }

    protected fun removeFooter() {
        synchronized(lock) {
            val indexOfFooter = indexOf(footerRecyclerViewItem)
            if (indexOfFooter != -1) {
                mutableItems.remove(footerRecyclerViewItem)
                notifyItemRemoved(indexOfFooter)
            }
        }
        hasFooter = false
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener?) {
        this.onLoadMoreListener = onLoadMoreListener
        if (onLoadMoreListener != null) {
            synchronized(lock) {
                mutableItems.add(loadingMoreRecyclerViewItem)
                notifyItemInserted(mutableItems.size - 1)
            }
        }
    }

    fun setLoadingMoreThreshold(threshold: Int) {
        loadMoreThreshold = threshold
    }

    /**
     * Make items selectable or not.
     */
    fun setSelectable(selectable: Boolean) {
        this.selectable = selectable
    }

    /**
     * Select a view at a specific [position].
     *
     * @param position The position of the item.
     * @param notify true if the adapter has to be notified.
     */
    fun select(position: Int, notify: Boolean = true) {
        if (selectedPosition != -1 && notify) notifyItemChanged(selectedPosition)
        selectedPosition = position
        if (notify) notifyItemChanged(selectedPosition)
    }

    /**
     * Set the loading state for the adapter.
     *
     * @param isLoading true if the adapter is in the loading state.
     */
    fun setLoading(isLoading: Boolean) {
        synchronized(lock) {
            mutableItems.clear()
            if (hasHeader) mutableItems.add(0, headerRecyclerViewItem)
            if (isLoading) mutableItems.add(loadingRecyclerViewItem)
            if (hasFooter) mutableItems.add(footerRecyclerViewItem)
            onLoadMoreListener?.let {
                setLoadMoreStatus(DEFAULT)
                mutableItems.add(loadingMoreRecyclerViewItem)
            }
            notifyDataSetChanged()
        }
    }

    /**
     * Get position of selected view.
     *
     * @return [selectedPosition] or -1 if no view has been selected yet
     */
    fun getSelectedPosition() = selectedPosition

    protected fun getHeaderItemCount(): Int = if (hasHeader) 1 else 0

    protected fun getFooterItemCount(): Int = if (hasFooter) 1 else 0

    protected fun getLoadingItemCount(): Int = if (mutableItems.contains(
                    loadingRecyclerViewItem)) 1 else 0

    protected fun getEmptyItemCount(): Int = if (mutableItems.contains(
                    emptyRecyclerViewItem)) 1 else 0

    protected fun getLoadingMoreItemCount(): Int = if (onLoadMoreListener != null) 1 else 0

    /**
     * Get the real position of the item in the adapter data set.
     */
    protected fun getRealItemPosition(position: Int): Int {
        return position + getHeaderItemCount()
    }

    /**
     * Get the position of the item in the adapter data set excluding fixed view types (ie. header).
     */
    protected fun getItemPosition(position: Int): Int {
        return position - getHeaderItemCount()
    }

    /**
     * ID for an XML layout resource per [viewType] (0 if custom view to build)
     *
     * @param viewType The view type of the ViewHolder
     */
    abstract fun getLayoutRes(viewType: Int): Int

    /**
     * Get item View (according to [viewType]).
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position
     *
     * @param viewType The view type of the ViewHolder
     */
    abstract fun getItemView(parent: ViewGroup, viewType: Int): View

    /**
     * Do the actual binding (use [holder].itemViewType to handle multiple view types)
     */
    abstract fun doBindViewHolder(holder: BaseViewHolder, position: Int)

    /**
     * Returns the index of the first occurrence of the specified [item] in the list, or -1 if the specified element is not contained in the list.
     */
    fun indexOf(item: BaseRecyclerViewItem): Int {
        synchronized(lock) {
            return mutableItems.indexOf(item)
        }
    }

    /**
     * Get [BaseRecyclerViewItem] at [position].
     */
    fun getItem(position: Int): BaseRecyclerViewItem? {
        synchronized(lock) {
            return if (mutableItems.size > position) mutableItems[position] else null
        }
    }

    /**
     * Add [item] at [position] in the list and notify adapter.
     */
    fun addItem(item: BaseRecyclerViewItem, position: Int) {
        synchronized(lock) {
            removeEmptyViewItem()
            val index = position + getHeaderItemCount()
            mutableItems.add(index, item)
            notifyItemInserted(index)
        }
    }

    /**
     * Add [item] to the end of the list and notify adapter.
     */
    fun addItem(item: BaseRecyclerViewItem) {
        synchronized(lock) {
            removeEmptyViewItem()
            val index = mutableItems.size - getLoadingMoreItemCount()
            mutableItems.add(index, item)
            notifyItemInserted(index)
        }
    }

    /**
     * Add [newItems] to the end of the list and notify adapter.
     */
    fun addItems(newItems: List<BaseRecyclerViewItem>) {
        synchronized(lock) {
            removeEmptyViewItem()
            val position = mutableItems.size - getLoadingMoreItemCount()
            mutableItems.addAll(position, newItems)
            notifyItemRangeInserted(position, newItems.size)
        }
    }

    private fun removeEmptyViewItem() {
        if (getEmptyItemCount() > 0) {
            mutableItems.remove(emptyRecyclerViewItem)
            notifyItemRemoved(getHeaderItemCount())
        }
    }

    /**
     * Remove item at [position] in the list and notify adapter.
     */
    fun removeItem(position: Int) {
        synchronized(lock) {
            mutableItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Remove [item] in the list and notify adapter.
     */
    fun removeItem(item: BaseRecyclerViewItem) {
        synchronized(lock) {
            val position = mutableItems.indexOf(item)
            if (position == -1) return
            mutableItems.remove(item)
            notifyItemRemoved(position)
        }
    }

    /**
     * Remove [items] in the list and notify adapter.
     */
    fun removeItems(items: List<BaseRecyclerViewItem>) {
        synchronized(lock) {
            val position = mutableItems.indexOf(items.first())
            if (position == -1) return
            mutableItems.removeAll(items)
            notifyItemRangeRemoved(position, items.size)
        }
    }

    /**
     * Update [item] at [position] and notify adapter.
     */
    fun updateItem(item: BaseRecyclerViewItem, position: Int) {
        synchronized(lock) {
            if (mutableItems.size > position) {
                mutableItems[position] = item
                notifyItemChanged(position)
            }
        }
    }

    /**
     * Set new [items] and notify adapter that data set has changed.
     */
    fun setItems(items: List<BaseRecyclerViewItem>) {
        synchronized(lock) {
            initItems(items)
            notifyDataSetChanged()
        }
    }

    private fun initItems(items: List<BaseRecyclerViewItem>) {
        if (mutableItems.isNotEmpty()) mutableItems.clear()
        items.forEach { recyclerViewItem ->
            mutableItems.add(recyclerViewItem)
            recyclerViewItem.getNestedViewItems()?.let { mutableItems.addAll(it) }
        }
        if (hasHeader) mutableItems.add(0, headerRecyclerViewItem)
        if (mutableItems.isEmpty()) {
            mutableItems.add(emptyRecyclerViewItem)
        }
        if (hasFooter) mutableItems.add(footerRecyclerViewItem)
        onLoadMoreListener?.let {
            setLoadMoreStatus(if (items.isNotEmpty()) SUCCESS else DEFAULT)
            mutableItems.add(loadingMoreRecyclerViewItem)
        }
    }

    /**
     * Clear data and notify adapter.
     */
    fun clear() {
        synchronized(lock) {
            mutableItems.clear()
            if (hasHeader) mutableItems.add(0, headerRecyclerViewItem)
            mutableItems.add(emptyRecyclerViewItem)
            if (hasFooter) mutableItems.add(footerRecyclerViewItem)
            onLoadMoreListener?.let {
                setLoadMoreStatus(DEFAULT)
                mutableItems.add(loadingMoreRecyclerViewItem)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        synchronized(lock) {
            return mutableItems.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        synchronized(lock) {
            return if (mutableItems.size > position) {
                mutableItems[position].getRecyclerViewItemType()
            } else super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            when (viewType) {
                LOADING_VIEW_TYPE -> {
                    BaseViewHolder(LayoutInflater.from(parent.context).inflate(
                            R.layout.recyclerview_loading_layout, parent, false))
                }
                LOADING_MORE_VIEW_TYPE -> {
                    LoadingMoreViewHolder(
                            LayoutInflater.from(parent.context).inflate(
                                    R.layout.recyclerview_loading_more_layout, parent,
                                    false)
                    ).apply {
                        itemView.setOnClickListener {
                            if (loadingMoreRecyclerViewItem.status == FAILURE) loadMore(
                                    notify = true)
                        }
                    }
                }
                else -> {
                    if (getLayoutRes(viewType) == 0) {
                        BaseViewHolder(getItemView(parent, viewType))
                    } else {
                        BaseViewHolder(
                                LayoutInflater.from(parent.context).inflate(getLayoutRes(viewType),
                                        parent, false))
                    }.apply {
                        if (isNotFixedType(viewType)) {
                            itemView.setOnClickListener {
                                onItemClickListener?.onItemClick(adapterPosition)
                            }
                        }
                    }
                }
            }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (val viewType = getItemViewType(position)) {
            LOADING_MORE_VIEW_TYPE -> {
                (holder as LoadingMoreViewHolder).bind(loadingMoreRecyclerViewItem.status)
            }
            else -> {
                doBindViewHolder(holder, position)
                if (isNotFixedType(viewType)) {
                    if (selectable) setSelectedView(holder, position)
                    onLoadMoreListener?.let { notifyLoadMore(position) }
                }
            }
        }
    }

    private fun isNotFixedType(viewType: Int): Boolean {
        return viewType != HEADER_VIEW_TYPE && viewType != EMPTY_VIEW_TYPE && viewType != LOADING_VIEW_TYPE && viewType != LOADING_MORE_VIEW_TYPE
    }

    private fun notifyLoadMore(position: Int) {
        if (position < itemCount - loadMoreThreshold) return
        loadMore()
    }

    private fun loadMore(notify: Boolean = false) {
        if (!loadingMore.get()) {
            setLoadMoreStatus(LOADING)
            if (notify) notifyItemChanged(itemCount - 1)
            onLoadMoreListener?.onLoadMore()
        }
    }

    private fun setLoadMoreStatus(status: LoadMoreStatus) {
        loadingMoreRecyclerViewItem.status = status
        when (status) {
            DEFAULT, FAILURE, SUCCESS -> loadingMore.set(false)
            LOADING -> loadingMore.set(true)
        }
    }

    private fun setSelectedView(holder: BaseViewHolder, position: Int) {
        holder.itemView.isSelected = selectedPosition == position
    }

    /**
     * Notify adapter that loading more has completed.
     */
    fun loadingMoreCompleted() {
        setLoadMoreStatus(SUCCESS)
        notifyItemChanged(itemCount - 1)
    }

    /**
     * Notify adapter that loading more has failed.
     */
    fun loadingMoreFailed() {
        setLoadMoreStatus(FAILURE)
        notifyItemChanged(itemCount - 1)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnLoadMoreListener {
        fun onLoadMore()

        enum class LoadMoreStatus {
            DEFAULT, LOADING, FAILURE, SUCCESS
        }
    }

    companion object {
        private const val NO_SELECTION = -1
        private const val LOAD_MORE_THRESHOLD = 2
        const val HEADER_VIEW_TYPE = 100
        const val EMPTY_VIEW_TYPE = 200
        const val FOOTER_VIEW_TYPE = 500
        internal const val LOADING_VIEW_TYPE = 300
        internal const val LOADING_MORE_VIEW_TYPE = 400
    }
}
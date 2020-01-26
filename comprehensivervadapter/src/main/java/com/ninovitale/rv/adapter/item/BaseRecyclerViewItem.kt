package com.ninovitale.rv.adapter.item

/**
 * Interface to be implemented by RecyclerView items used by [ComprehensiveRvAdapter].
 */
interface BaseRecyclerViewItem {
    /**
     * Return the view type to be used while creating and binding the view holder.
     * [LOADING_MORE_VIEW_TYPE] and [HEADER_VIEW_TYPE] should not be used.
     */
    fun getRecyclerViewItemType(): Int = 0

    /**
     * Return a list of nested items (if any).
     */
    fun getNestedViewItems(): List<BaseRecyclerViewItem>? = null
}
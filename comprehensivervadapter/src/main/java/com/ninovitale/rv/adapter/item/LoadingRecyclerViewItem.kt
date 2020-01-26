package com.ninovitale.rv.adapter.item

import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.Companion.LOADING_VIEW_TYPE

internal class LoadingRecyclerViewItem : BaseRecyclerViewItem {
    override fun getRecyclerViewItemType(): Int = LOADING_VIEW_TYPE
}
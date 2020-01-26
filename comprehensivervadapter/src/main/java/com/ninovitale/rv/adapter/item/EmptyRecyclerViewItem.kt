package com.ninovitale.rv.adapter.item

import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.Companion.EMPTY_VIEW_TYPE

internal class EmptyRecyclerViewItem : BaseRecyclerViewItem {
    override fun getRecyclerViewItemType() = EMPTY_VIEW_TYPE
}
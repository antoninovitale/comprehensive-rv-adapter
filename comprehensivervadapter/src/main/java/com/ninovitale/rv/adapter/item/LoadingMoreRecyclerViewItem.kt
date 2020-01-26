package com.ninovitale.rv.adapter.item

import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.Companion.LOADING_MORE_VIEW_TYPE
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.DEFAULT

internal class LoadingMoreRecyclerViewItem : BaseRecyclerViewItem {
    var status: LoadMoreStatus = DEFAULT

    override fun getRecyclerViewItemType() = LOADING_MORE_VIEW_TYPE
}
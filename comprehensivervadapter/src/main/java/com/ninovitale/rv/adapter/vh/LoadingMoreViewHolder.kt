package com.ninovitale.rv.adapter.vh

import android.view.View
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.DEFAULT
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.FAILURE
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.LOADING
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter.OnLoadMoreListener.LoadMoreStatus.SUCCESS
import kotlinx.android.synthetic.main.recyclerview_loading_more_layout.view.load_more_end_view
import kotlinx.android.synthetic.main.recyclerview_loading_more_layout.view.load_more_fail_view
import kotlinx.android.synthetic.main.recyclerview_loading_more_layout.view.load_more_loading_view

internal class LoadingMoreViewHolder(view: View) : BaseViewHolder(view) {
    fun bind(status: LoadMoreStatus) {
        when (status) {
            DEFAULT -> {
                itemView.load_more_loading_view.visibility = View.GONE
                itemView.load_more_fail_view.visibility = View.GONE
                itemView.load_more_end_view.visibility = View.GONE
            }
            LOADING -> {
                itemView.load_more_loading_view.visibility = View.VISIBLE
                itemView.load_more_fail_view.visibility = View.GONE
                itemView.load_more_end_view.visibility = View.GONE
            }
            FAILURE -> {
                itemView.load_more_loading_view.visibility = View.GONE
                itemView.load_more_fail_view.visibility = View.VISIBLE
                itemView.load_more_end_view.visibility = View.GONE
            }
            SUCCESS -> {
                itemView.load_more_loading_view.visibility = View.GONE
                itemView.load_more_fail_view.visibility = View.GONE
                itemView.load_more_end_view.visibility = View.VISIBLE
            }
        }
    }
}
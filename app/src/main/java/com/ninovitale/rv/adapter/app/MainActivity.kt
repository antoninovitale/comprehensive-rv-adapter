package com.ninovitale.rv.adapter.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ninovitale.rv.adapter.ComprehensiveRvAdapter
import com.ninovitale.rv.adapter.item.BaseRecyclerViewItem
import com.ninovitale.rv.adapter.vh.BaseViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.item_view.view.*

/**
 * Sample activity to show a list of selectable items endlessly scrollable.
 */
class MainActivity : AppCompatActivity(), ComprehensiveRvAdapter.OnLoadMoreListener,
    ComprehensiveRvAdapter.OnItemClickListener {
    private val adapter = ItemAdapter(ItemGenerator.getItems())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        adapter.setSelectable(true)
        adapter.setOnItemClickListener(this)
        adapter.setOnLoadMoreListener(this)
    }

    override fun onLoadMore() {
        recycler_view.postDelayed({
            adapter.addItems(ItemGenerator.getItems(5))
            adapter.loadingMoreCompleted()
        }, 500)
    }

    override fun onItemClick(position: Int) {
        adapter.select(position)
    }
}

class ItemAdapter(items: List<Item>) : ComprehensiveRvAdapter(items) {
    override fun getLayoutRes(viewType: Int): Int {
        return 0
    }

    override fun getItemView(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(parent.context).inflate(
            R.layout.item_view,
            parent,
            false
        )
    }

    override fun doBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Item -> {
                holder.itemView.item_name.text = item.name
                holder.itemView.item_description.text = item.description
            }
            else -> {
            }
        }
    }
}

data class Item(val name: String, val description: String) : BaseRecyclerViewItem

object ItemGenerator {
    fun getItems(count: Int = 30): List<Item> {
        val items = mutableListOf<Item>()
        for (i in 1..count) {
            items.add(Item(name = "item $i", description = "this is the item $i"))
        }
        return items
    }
}
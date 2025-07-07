package com.tzt.custompopupwindow.low_attension

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tzt.custompopupwindow.R

class ExampleRecyclerViewAdapter(val context: Context, val data: List<String>) : RecyclerView.Adapter<ExampleRecyclerViewAdapter.ExampleViewHolder>() {

    open class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView? = null

        fun onBind() {
            textView = itemView.findViewById(R.id.description)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        return when(viewType) {
            0 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.list_item, null)
                ExampleViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.list_item_empty_line, null)
                ExampleViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        holder.onBind()
        holder.textView?.also {
            it.text = data[position]
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        /*return super.getItemViewType(position)*/
        if (data[position] == "gap") {
            return 1;
        }
        return 0;
    }
}
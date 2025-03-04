package com.huyuhui.utils_kotlin.demo.placeholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huyuhui.utils_kotlin.demo.databinding.PlaceholderItemBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderContent.PlaceholderItem].
 */
class PlaceholderViewAdapter(
    private val values: List<PlaceholderContent.PlaceholderItem>
) : RecyclerView.Adapter<PlaceholderViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PlaceholderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(binding: PlaceholderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        init {
            binding.root.setOnClickListener {

            }
        }
        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}
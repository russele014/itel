package com.example.grocelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grocelist.HomeFragment.GroceryItem

class GroceryAdapter(
    private var groceryItems: List<GroceryItem>,
    private val onItemClick: (GroceryItem) -> Unit
) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemName: TextView = itemView.findViewById(R.id.itemName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grocery_item_layout, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val item = groceryItems[position]

        // Check if we're using a remote image or local resource
        if (item.imageUrl.isNotEmpty()) {
            // Load image from URL using Glide
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.itemImage)
        } else if (item.imageResource != 0) {
            // Use local resource
            holder.itemImage.setImageResource(item.imageResource)
        }

        holder.itemName.text = item.name

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = groceryItems.size

    fun updateList(filteredList: List<GroceryItem>) {
        groceryItems = filteredList
        notifyDataSetChanged()
    }
}
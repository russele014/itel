package com.example.grocelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroceryAdapter
    private lateinit var searchView: SearchView
    private lateinit var categoryAll: TextView
    private lateinit var categoryVegetables: TextView
    private lateinit var categoryFruits: TextView
    private lateinit var categoryMeat: TextView
    private lateinit var categoryBeverages: TextView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var menuIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout

    // Sample data - replace with actual images from your drawable resources
    private val allItems = listOf(
        GroceryItem(1, "Orange", R.drawable.orange, "Vegetables"),
        GroceryItem(2, "Turnip", R.drawable.turnip, "Vegetables"),
        GroceryItem(3, "Banana", R.drawable.banana, "Fruits"),
        GroceryItem(4, "Pear", R.drawable.pear, "Fruits"),
        GroceryItem(5, "Sweet Potato", R.drawable.potato, "Vegetables"),
        GroceryItem(6, "Onion", R.drawable.onion, "Vegetables"),
        GroceryItem(7, "Chicken Breast", R.drawable.chicken, "Meat"),
        GroceryItem(8, "Beef", R.drawable.baka, "Meat"),
        GroceryItem(9, "C2", R.drawable.ctwo, "Beverages"),
    )

    data class GroceryItem(
        val id: Int,
        val name: String,
        val imageResource: Int,
        val category: String
    )

    private var currentItems = allItems

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.groceryRecyclerView)
        searchView = view.findViewById(R.id.searchView)
        categoryAll = view.findViewById(R.id.categoryAll)
        categoryVegetables = view.findViewById(R.id.categoryVegetables)
        categoryFruits = view.findViewById(R.id.categoryFruits)
        categoryMeat = view.findViewById(R.id.categoryMeat)
        categoryBeverages = view.findViewById(R.id.categoryBeverages)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        menuIcon = view.findViewById(R.id.menuIcon)

        // Get drawer layout from activity
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)

        // Setup drawer toggle with hamburger icon
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = GroceryAdapter(currentItems) { item ->
            Toast.makeText(context, "${item.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // Setup search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText ?: "")
                return true
            }
        })

        // Setup category filters
        setupCategoryFilters()

        // Setup add button
        fabAddItem.setOnClickListener {
            Toast.makeText(context, "Add new item", Toast.LENGTH_SHORT).show()
            // You could navigate to a new fragment or show a dialog here
        }
    }

    private fun setupCategoryFilters() {
        val categories = listOf(categoryAll, categoryVegetables, categoryFruits, categoryMeat, categoryBeverages)

        fun updateCategorySelection(selectedCategory: TextView) {
            categories.forEach { it.setBackgroundResource(android.R.color.transparent) }
            selectedCategory.setBackgroundResource(R.drawable.selected_category_background)
        }

        categoryAll.setOnClickListener {
            updateCategorySelection(categoryAll)
            currentItems = allItems
            adapter.updateList(currentItems)
        }

        categoryVegetables.setOnClickListener {
            updateCategorySelection(categoryVegetables)
            currentItems = allItems.filter { it.category == "Vegetables" }
            adapter.updateList(currentItems)
        }

        categoryFruits.setOnClickListener {
            updateCategorySelection(categoryFruits)
            currentItems = allItems.filter { it.category == "Fruits" }
            adapter.updateList(currentItems)
        }

        categoryMeat.setOnClickListener {
            updateCategorySelection(categoryMeat)
            currentItems = allItems.filter { it.category == "Meat" }
            adapter.updateList(currentItems)
        }

        categoryBeverages.setOnClickListener {
            updateCategorySelection(categoryBeverages)
            currentItems = allItems.filter { it.category == "Beverages" }
            adapter.updateList(currentItems)
        }
    }

    private fun filterItems(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(currentItems)
            return
        }

        val filteredList = currentItems.filter {
            it.name.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }
}
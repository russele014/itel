package com.example.grocelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var menuIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout

    private val categoryManager: CategoryManager by lazy {
        CategoryManager(requireContext())
    }

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
        categoriesContainer = view.findViewById(R.id.categoriesContainer)
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

        // Setup long press on category manager icon to navigate to category management
        view.findViewById<ImageView>(R.id.menuIcon).setOnLongClickListener {
            navigateToCategoryManagement()
            true
        }

        view.findViewById<ImageView>(R.id.categoryManagerIcon).setOnClickListener {
            navigateToCategoryManagement()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh categories when returning to this fragment
        setupCategoryFilters()
    }

    private fun setupCategoryFilters() {
        // Clear existing category views
        categoriesContainer.removeAllViews()

        // Get categories from manager
        val categories = categoryManager.getCategories()

        // Track the currently selected category TextView
        var selectedCategoryView: TextView? = null

        // Inflate and add category TextViews
        categories.forEach { category ->
            val categoryView = LayoutInflater.from(context).inflate(
                R.layout.category_item, categoriesContainer, false
            ) as TextView

            // Set properties
            categoryView.text = category.name
            categoryView.id = View.generateViewId()

            // Set as initially selected if it's the "All" category
            if (category.name == "All") {
                categoryView.setBackgroundResource(R.drawable.selected_category_background)
                selectedCategoryView = categoryView
            }

            // Set click listener
            categoryView.setOnClickListener {
                // Update selection UI
                selectedCategoryView?.setBackgroundResource(android.R.color.transparent)
                categoryView.setBackgroundResource(R.drawable.selected_category_background)
                selectedCategoryView = categoryView

                // Filter items by category
                if (category.name == "All") {
                    currentItems = allItems
                } else {
                    currentItems = allItems.filter { it.category == category.name }
                }
                adapter.updateList(currentItems)
            }

            // Add to container
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.category_margin)
            categoryView.layoutParams = layoutParams

            categoriesContainer.addView(categoryView)
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

    private fun navigateToCategoryManagement() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, CategoryFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class HomeFragment : Fragment() {

    companion object {
        private const val PHP_API_URL = "http://grocelist123.x10.mx/"
    }

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

    data class GroceryItem(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val imageResource: Int = 0, // For backward compatibility
        val category: String
    )

    private var allItems = listOf<GroceryItem>()
    private var currentItems = listOf<GroceryItem>()

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
        adapter = GroceryAdapter(emptyList()) { item ->
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
            navigateToAddItem()
        }

        // Setup category manager icon
        view.findViewById<ImageView>(R.id.categoryManagerIcon).setOnClickListener {
            navigateToCategoryManagement()
        }

        // Load items from server
        fetchItemsFromServer()
    }

    override fun onResume() {
        super.onResume()
        // Refresh categories and items when returning to this fragment
        setupCategoryFilters()
        fetchItemsFromServer()
    }

    private fun fetchItemsFromServer() {
        val url = "${PHP_API_URL}get_items.php"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val itemsArray = response.getJSONArray("items")
                        parseItems(itemsArray)
                    } else {
                        Toast.makeText(context, "Failed to load items", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseItems(itemsArray: JSONArray) {
        val items = mutableListOf<GroceryItem>()

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            items.add(
                GroceryItem(
                    id = item.getInt("id"),
                    name = item.getString("name"),
                    imageUrl = "${PHP_API_URL}uploads/${item.getString("image")}",
                    category = item.getString("category")
                )
            )
        }

        allItems = items
        currentItems = items
        adapter.updateList(currentItems)
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

    private fun navigateToAddItem() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, ItemFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
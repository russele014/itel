package com.example.grocelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable

class CategoryFragment : Fragment() {

    private lateinit var menuIcon: ImageView
    private lateinit var categoryNameEditText: TextInputEditText
    private lateinit var addCategoryButton: Button
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var drawerLayout: DrawerLayout

    // Use shared preferences to store categories
    private val categoryManager: CategoryManager by lazy {
        CategoryManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        menuIcon = view.findViewById(R.id.menuIcon)
        categoryNameEditText = view.findViewById(R.id.categoryNameEditText)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)

        // Get drawer layout from activity
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)

        // Setup drawer toggle with hamburger icon
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup RecyclerView
        categoriesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Get saved categories
        val categories = categoryManager.getCategories()

        // Setup adapter with delete functionality
        categoryAdapter = CategoryAdapter(categories.toMutableList()) { position ->
            deleteCategory(position)
        }
        categoriesRecyclerView.adapter = categoryAdapter

        // Add category button click listener
        addCategoryButton.setOnClickListener {
            val categoryName = categoryNameEditText.text.toString().trim()

            if (categoryName.isEmpty()) {
                Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check for duplicates
            if (categories.any { it.name.equals(categoryName, ignoreCase = true) }) {
                Toast.makeText(context, "Category already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add new category
            val newCategory = Category(
                id = System.currentTimeMillis().toInt(),
                name = categoryName
            )

            // Add to local list and save
            categoryManager.addCategory(newCategory)

            // Update adapter
            categoryAdapter.addCategory(newCategory)

            // Clear input field
            categoryNameEditText.text?.clear()

            Toast.makeText(context, "Category added successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCategory(position: Int) {
        val category = categoryAdapter.getCategory(position)
        categoryManager.removeCategory(category)
        categoryAdapter.removeCategory(position)
        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
    }

    // Adapter for categories with delete functionality
    private inner class CategoryAdapter(
        private val categories: MutableList<Category>,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val categoryName: TextView = itemView.findViewById(R.id.categoryName)
            val deleteButton: ImageView = itemView.findViewById(R.id.deleteCategory)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = categories[position]
            holder.categoryName.text = category.name

            holder.deleteButton.setOnClickListener {
                onDeleteClick(position)
            }
        }

        override fun getItemCount() = categories.size

        fun addCategory(category: Category) {
            categories.add(category)
            notifyItemInserted(categories.size - 1)
        }

        fun removeCategory(position: Int) {
            if (position >= 0 && position < categories.size) {
                categories.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        fun getCategory(position: Int): Category {
            return categories[position]
        }
    }
}

// Data class for Category
data class Category(
    val id: Int,
    val name: String
) : Serializable
package com.example.grocelist

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "grocery_app_preferences", Context.MODE_PRIVATE
    )

    private val gson = Gson()
    private val categoryListType = object : TypeToken<List<Category>>() {}.type

    companion object {
        private const val KEY_CATEGORIES = "categories"

        // Default categories
        private val DEFAULT_CATEGORIES = listOf(
            Category(1, "All"),
            Category(2, "Vegetables"),
            Category(3, "Fruits"),
            Category(4, "Meat"),
            Category(5, "Beverages")
        )
    }

    // Get all categories, initializing with defaults if empty
    fun getCategories(): List<Category> {
        val categoriesJson = sharedPreferences.getString(KEY_CATEGORIES, null)

        return if (categoriesJson.isNullOrEmpty()) {
            // Initialize with default categories
            saveCategories(DEFAULT_CATEGORIES)
            DEFAULT_CATEGORIES
        } else {
            gson.fromJson(categoriesJson, categoryListType)
        }
    }

    // Add a new category
    fun addCategory(category: Category) {
        val categories = getCategories().toMutableList()
        categories.add(category)
        saveCategories(categories)
    }

    // Remove a category
    fun removeCategory(category: Category) {
        val categories = getCategories().toMutableList()
        categories.removeAll { it.id == category.id }
        saveCategories(categories)
    }

    // Update a category
    fun updateCategory(category: Category) {
        val categories = getCategories().toMutableList()
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            categories[index] = category
            saveCategories(categories)
        }
    }

    // Save categories to shared preferences
    private fun saveCategories(categories: List<Category>) {
        val categoriesJson = gson.toJson(categories)
        sharedPreferences.edit().putString(KEY_CATEGORIES, categoriesJson).apply()
    }
}
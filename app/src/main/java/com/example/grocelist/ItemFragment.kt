package com.example.grocelist

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.util.Base64
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ItemFragment : Fragment() {

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PHP_API_URL = "http://grocelist123.x10.mx/add_item.php/"
    }

    private lateinit var itemNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var itemImagePreview: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var saveItemButton: Button
    private lateinit var backButton: ImageView

    private var selectedImageBitmap: Bitmap? = null
    private val categoryManager: CategoryManager by lazy {
        CategoryManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        itemNameInput = view.findViewById(R.id.itemNameInput)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        itemImagePreview = view.findViewById(R.id.itemImagePreview)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        saveItemButton = view.findViewById(R.id.saveItemButton)
        backButton = view.findViewById(R.id.backButton)

        // Setup category spinner
        setupCategorySpinner()

        // Setup image selection
        selectImageButton.setOnClickListener {
            openGallery()
        }

        // Setup save button
        saveItemButton.setOnClickListener {
            saveItem()
        }

        // Setup back button
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupCategorySpinner() {
        val categories = categoryManager.getCategories()
            .filter { it.name != "All" } // Exclude "All" from item categories
            .map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver, uri
                    )
                    itemImagePreview.setImageBitmap(selectedImageBitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveItem() {
        val itemName = itemNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""

        // Validate inputs
        if (itemName.isEmpty()) {
            Toast.makeText(context, "Please enter item name", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageBitmap == null) {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert bitmap to base64 string
        val imageBase64 = convertBitmapToBase64(selectedImageBitmap!!)

        // Send data to server
        saveItemToServer(itemName, category, imageBase64)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveItemToServer(name: String, category: String, imageBase64: String) {
        // Show loading indicator
        saveItemButton.isEnabled = false
        saveItemButton.text = "Saving..."

        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "${PHP_API_URL}add_item.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                saveItemButton.isEnabled = true
                saveItemButton.text = "Save Item"

                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")

                    if (success) {
                        Toast.makeText(context, "Item saved successfully", Toast.LENGTH_SHORT).show()
                        // Return to home fragment
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                saveItemButton.isEnabled = true
                saveItemButton.text = "Save Item"
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["category"] = category
                params["image"] = imageBase64
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
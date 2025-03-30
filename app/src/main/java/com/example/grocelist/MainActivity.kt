package com.example.grocelist

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val homeFragment = HomeFragment()
    val categoryFragment = CategoryFragment()
    val itemFragment = ItemFragment()
    val costsFragment = CostsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        setCurrentFragment(homeFragment)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> setCurrentFragment(homeFragment)
            R.id.nav_category -> setCurrentFragment(categoryFragment)
            R.id.nav_item -> setCurrentFragment(itemFragment)
            R.id.nav_costs -> setCurrentFragment(costsFragment)
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }


    private fun onLogout(){
        // Create the object of AlertDialog Builder class
        val builder = AlertDialog.Builder(this)

        // Set the message show for the Alert time
        builder.setMessage("Do you want to Logout?")

        // Set Alert Title
        builder.setTitle("Alert !")

        // Set Cancelable false for when the user clicks
        // on the outside the Dialog Box then it will remain show
        builder.setCancelable(false)

        // Set the positive button with yes name Lambda
        // OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes") { _, _ ->
            // When the user click yes button then app will close
            finish()
        }

        // Set the Negative button with No name Lambda
        // OnClickListener method is use of DialogInterface interface.
        builder.setNegativeButton("No") { dialog, _ ->
            // If user click no then dialog box is canceled.
            dialog.cancel()
        }

        // Create the Alert dialog
        val alertDialog = builder.create()

        // Show the Alert Dialog box
        alertDialog.show()
    }
    // Handle back button press
    override fun onBackPressed() {
        // If drawer is open, close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Show exit confirmation dialog
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure to Exit?")
        builder.setTitle("Exit Application")
        builder.setCancelable(false)

        builder.setPositiveButton("Yes") { _, _ ->
            // If user clicks Yes, exit the app
            finishAffinity() // This will close all activities of your app
        }

        builder.setNegativeButton("No") { dialog, _ ->
            // If user clicks No, dismiss the dialog and stay in the app
            dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}

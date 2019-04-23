package com.example.ginold.reportmap

import android.app.Activity
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import  android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.widget.ImageButton
import com.example.ginold.reportmap.fragments.GalleryFragment
import com.example.ginold.reportmap.fragments.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity(){

    private var mDrawerLayout: DrawerLayout? = null
    private val reportFragment: com.example.ginold.reportmap.fragments.ReportFragment = com.example.ginold.reportmap.fragments.ReportFragment()
    private val mapFragment: com.example.ginold.reportmap.fragments.MapFragment = com.example.ginold.reportmap.fragments.MapFragment()
    private val loginFragment:  LoginFragment = LoginFragment()

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)

        val fm = fragmentManager
        fm.beginTransaction().replace(R.id.main_fragment_content, this.loginFragment).commit()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set the hamburger icon as Home
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)

        mDrawerLayout = findViewById(R.id.drawer_layout)

        val logoutButton = findViewById<ImageButton>(R.id.logoutButtonNav)
        logoutButton.setOnClickListener {
            signOut()
        }

        // handle the drawer menu item clicks
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_map -> {
                    fm.beginTransaction().replace(R.id.main_fragment_content, mapFragment).addToBackStack(null).commit()
                }
                R.id.nav_gallery -> {
                    fm.beginTransaction().replace(R.id.main_fragment_content, GalleryFragment()).addToBackStack(null).commit()
                }
                R.id.nav_report -> {
                    fm.beginTransaction().replace(R.id.main_fragment_content, reportFragment).addToBackStack(null).commit()
                }
            }
            clearBackStack()
            item.setChecked(true)
            mDrawerLayout!!.closeDrawers()
            true
        }
    }
    private fun signOut() {
        mAuth.signOut()
        val fm = fragmentManager
        fm.beginTransaction().replace(R.id.main_fragment_content, this.loginFragment).addToBackStack(null).commit()
    }
    private fun clearBackStack() {
        val manager = getSupportFragmentManager()
        if (manager.getBackStackEntryCount() > 0) {
            var first = manager.getBackStackEntryAt(0) as FragmentManager.BackStackEntry
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /*
        open the drawer when clicked on the hamburger button
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

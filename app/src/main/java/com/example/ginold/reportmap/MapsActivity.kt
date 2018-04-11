package com.example.ginold.reportmap

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import  android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import com.google.android.gms.maps.MapFragment
import android.support.v4.view.GravityCompat
import android.util.Log
import com.example.ginold.reportmap.fragments.GalleryFragment


class MapsActivity : AppCompatActivity() {

    private var mDrawerLayout: DrawerLayout? = null
    private val reportFragment: com.example.ginold.reportmap.fragments.ReportFragment = com.example.ginold.reportmap.fragments.ReportFragment()
    private val mapFragment: com.example.ginold.reportmap.fragments.MapFragment = com.example.ginold.reportmap.fragments.MapFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fm = fragmentManager
        fm.beginTransaction().replace(R.id.main_fragment_content, reportFragment).commit()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set the hamburger icon as Home
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)

        mDrawerLayout = findViewById(R.id.drawer_layout)

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

    private fun clearBackStack() {
        val manager = getSupportFragmentManager()
        Log.i("asd", manager.getBackStackEntryCount().toString())
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

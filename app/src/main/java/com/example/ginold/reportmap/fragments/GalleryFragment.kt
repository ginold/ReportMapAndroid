package com.example.ginold.reportmap.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.ginold.reportmap.IssueItemOnClickListener
import com.example.ginold.reportmap.IssueRecyclerViewAdapter

import com.example.ginold.reportmap.R
import com.example.ginold.reportmap.Utils
import com.example.ginold.reportmap.models.Issue
import java.io.StringReader

class GalleryFragment : Fragment(), IssueItemOnClickListener {

    private var productsRecyclerView: RecyclerView? = null
    private var recyclerAdapter: IssueRecyclerViewAdapter? = null
    private var recyclerLayoutManager: RecyclerView.LayoutManager? = null
    private var issues: ArrayList<Issue> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getIssues()

        this.productsRecyclerView = view.findViewById(R.id.issue_list_recycler_view) as RecyclerView
        this.productsRecyclerView!!.setHasFixedSize(true)

        // use a grid layout manager -> 2 columns
        this.recyclerLayoutManager = GridLayoutManager(context, 2) as RecyclerView.LayoutManager?
        this.productsRecyclerView!!.layoutManager = this.recyclerLayoutManager

        this.recyclerAdapter = IssueRecyclerViewAdapter(this.issues, this)
        this.productsRecyclerView!!.adapter = recyclerAdapter
    }
    override fun onIssueItemClick(pos: Int, issue: Issue) {
        val args = Bundle()
        val detailsFragment = IssueDetailsFragment()
        args.putDouble("lat", issue.lat)
        args.putDouble("lng", issue.lng)
        args.putString("description", issue.description)
        args.putString("name", issue.name)
        args.putString("type", issue.type)
        args.putString("imgUrl", issue.imgUrl)
        detailsFragment.arguments = args

        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment_content, detailsFragment)
                .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null).commit()
    }
    private fun getIssues() {
//        val stream = resources.openRawResource(R.raw.markers)
//        val inputString = stream.bufferedReader().use {
//            it.readText()
//        }
//        val klaxon = Klaxon()
//        JsonReader(StringReader(inputString)).use { reader -> // requires kotlin > 1.2 !!
//            reader.beginArray {
//                while (reader.hasNext()) { // requires "[]" array and not {"something": []}
//                    val issue = klaxon.parse<Issue>(reader)
//                    this.issues!!.add(issue!!)
//                }
//            }
//        }
        this.issues = Utils.markers
    }
}// Required empty public constructor

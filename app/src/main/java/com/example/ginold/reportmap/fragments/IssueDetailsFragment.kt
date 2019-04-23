package com.example.ginold.reportmap.fragments

import android.os.Bundle
import android.app.Fragment
import android.content.ContentValues.TAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.ginold.reportmap.R
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.*
import com.google.firebase.storage.FirebaseStorage


class IssueDetailsFragment : Fragment(), OnMapReadyCallback {

    private var mMapView: MapView? = null
    private var map: GoogleMap? = null
    private var lat: Double? = null
    private var lng: Double? = null

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v =  inflater.inflate(R.layout.fragment_issue_details, container, false)

        val description = arguments.getString("description")
        val type = arguments.getString("type")
        val name = arguments.getString("name")
        val imgUrl = arguments.getString("imgUrl")
        this.lat = arguments.getDouble("lat")
        this.lng = arguments.getDouble("lng")

        val btn = v.findViewById<Button>(R.id.submitBtn)
        btn.setOnClickListener{
            submitSolvedIssue()
        }
        val progressDetail = v.findViewById<ProgressBar>(R.id.progressDetail)
        var img = v.findViewById<ImageView>(R.id.issuePicture)
        var descrptionText = v.findViewById<TextView>(R.id.issueDescriptionText)
        var nameText = v.findViewById<TextView>(R.id.issueTitleText)
        descrptionText.text = description
        nameText.text = name

        if (imgUrl != null && imgUrl != "") {
            val imgRef = storageRef.child(imgUrl)
            imgRef.getBytes(1024 * 1024).addOnSuccessListener { bytes ->// 1 MB max
                // convert the bytearray into bitmap
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                img.setImageBitmap(bmp)
                progressDetail.visibility = View.GONE
                img.visibility = View.VISIBLE
            }.addOnFailureListener {
                Toast.makeText(context, "Error while loading image", Toast.LENGTH_LONG)
            }
        } else {
            progressDetail.visibility = View.GONE
            img.visibility = View.VISIBLE
            img.setImageResource(resources.getIdentifier(type, "drawable", activity.packageName))
        }


        initMap(v, savedInstanceState)

        return v
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(this.lat!!, this.lng!!)
        this.map = googleMap
        this.map!!.addMarker(MarkerOptions().position(position))
        this.map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
    }

    private fun initMap(view: View, savedInstanceState: Bundle?) {
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.activity)
            this.mMapView = view.findViewById(R.id.mapViewDetails)
            this.mMapView!!.onCreate(savedInstanceState)
            this.mMapView!!.onResume() //without this, map showed but was empty
            this.mMapView!!.getMapAsync(this)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }
    fun submitSolvedIssue() {
        Toast.makeText(context, "Not working yet!", Toast.LENGTH_SHORT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
        according to android developers:
        Users of the MapView class must forward all the life cycle methods from the Activity or Fragment
        containing this view to the corresponding ones in this class.
     */
    override fun onResume() {
        super.onResume()
        mMapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mMapView!!.onStart()
    }
    override fun onPause() {
        super.onPause()
        mMapView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory();
        mMapView!!.onLowMemory();
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView!!.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDetach() {
        super.onDetach()
    }
}// Required empty public constructor

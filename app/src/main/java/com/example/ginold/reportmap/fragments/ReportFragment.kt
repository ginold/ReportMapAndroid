package com.example.ginold.reportmap.fragments


import android.Manifest
import android.os.Bundle
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup

import com.example.ginold.reportmap.R
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.ginold.reportmap.CameraActivity
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.vision.CameraSource


/**
 * A simple [Fragment] subclass.
 */
class  ReportFragment : Fragment(), OnMapReadyCallback {


    private var mMapView: MapView? = null
    private var map: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.activity)
            mMapView = view.findViewById<MapView>(R.id.mapView) as MapView
            mMapView!!.onCreate(savedInstanceState)
            mMapView!!.onResume() //without this, map showed but was empty
            mMapView!!.getMapAsync(this)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

        val spinner = view.findViewById<Spinner>(R.id.object_types_spinner) as Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(context, R.array.object_types, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        return view
    }

    fun takePicture() {
        val intent = Intent(context, CameraActivity::class.java)
        startActivity(intent)
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        this.map = googleMap
    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onResume() {
        super.onResume()
        mMapView!!.onResume()
    }

}// Required empty public constructor

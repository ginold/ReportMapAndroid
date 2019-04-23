//package com.example.ginold.reportmap
//
//import android.app.Activity
//import android.location.LocationManager
//import android.os.Bundle
//import android.view.View
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.MapView
//import com.google.android.gms.maps.MapsInitializer
//import com.google.android.gms.maps.OnMapReadyCallback
//
///**
// * Created by ginold on 13.04.2018.
// */
//class MapElement(activity: Activity, view: View, savedInstanceState: Bundle?) : OnMapReadyCallback {
//
//
//    private var mMapView: MapView? = null
//    private var map: GoogleMap? = null
//    private var locationManager : LocationManager? = null
//
//    init {
//        MapsInitializer.initialize(activity)
//        mMapView = view.findViewById<MapView>(R.id.mapView) as MapView
//        mMapView!!.onCreate(savedInstanceState)
//        mMapView!!.onResume() //without this, map showed but was empty
//        mMapView!!.getMapAsync(this)
//    }
//
//    /**
//    according to android developers:
//    Users of the MapView class must forward all the life cycle methods from the Activity or Fragment
//    containing this view to the corresponding ones in this class.
//     */
//    override fun onResume() {
//        activity.super.onResume()
//        mMapView!!.onResume()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        mMapView!!.onStart()
//    }
//    override fun onPause() {
//        super.onPause()
//        mMapView!!.onPause()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mMapView!!.onDestroy()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory();
//        mMapView!!.onLowMemory();
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        mMapView!!.onSaveInstanceState(outState)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//    }
//}
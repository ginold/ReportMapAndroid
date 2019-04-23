package com.example.ginold.reportmap.fragments


import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.app.Fragment
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.ginold.reportmap.R
import com.google.android.gms.maps.*
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapView
import android.support.v4.app.ActivityCompat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.Toast
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.ginold.reportmap.Utils
import com.example.ginold.reportmap.models.Issue
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.marker.*
import java.io.StringReader

class MapFragment : Fragment(), OnMapReadyCallback, OnMarkerClickListener,
        OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private var GPS_PERMISSION_CODE = 100
    private var CAMERA_ZOOM = 12f
    private var mMapView: MapView? = null
    private var map: GoogleMap? = null
    private var markerView: View? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationManager : LocationManager? = null
    private var googleApiClient: GoogleApiClient? = null
    private var markers: ArrayList<Issue> = ArrayList()
    private var currentLocation: Location? = null
    private var allMarkersMap: HashMap<Marker, Issue> = HashMap() // for extending the marker properties

    private var database: FirebaseDatabase? = null
    private var markersRef: DatabaseReference? = null
    private var markerChildEventListener: ChildEventListener? = null
    private var markersListener: ValueEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maps, container, false) as View

        database = FirebaseDatabase.getInstance()
        markersRef = database!!.getReference("markers")

        // set in onmapready
        this.markersListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("onDataChange", "onDataChange")
                getIssueMarkers(dataSnapshot)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        // when a property : value is added/changed in an objecT?
        this.markerChildEventListener = object: ChildEventListener {
            override fun onChildAdded(ds: DataSnapshot, prevChildKey: String?) {
                Log.i(TAG, " child added")
                if(markers.size == 0) {
                    pushIssueMarker(ds)
                }
            }
            override fun onChildChanged(ds: DataSnapshot, prevChildKey:String?) {
                Log.i(TAG, " child changed")
            }
            override fun onChildRemoved(ds: DataSnapshot) {
                Log.i(TAG, " child removed")
            }
            override fun onChildMoved(ds: DataSnapshot, prevChildKey:String?) {}
            override fun onCancelled(databaseError:DatabaseError) {}
        }

        this.markerView = activity.layoutInflater.inflate(R.layout.marker, null)
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        initMap(view, savedInstanceState)

        // used for location services
        this.googleApiClient = GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        return view
    }

    private fun pushIssueMarker(ds: DataSnapshot) {
        val issue = ds.getValue(Issue::class.java)
        this.markers!!.add(issue!!)
        addIssueMarkersToTheMap()
    }

    /**
     * get markers from raw/markers.json -> this.markers: ArrayList<Issue>
     */
    private fun getIssueMarkers(dataSnapshot: DataSnapshot) {
//        val stream = resources.openRawResource(R.raw.markers)
//        val inputString = stream.bufferedReader().use {
//            it.readText()
//        }
//        val klaxon = Klaxon()
//        JsonReader(StringReader(inputString)).use { reader -> // requires kotlin > 1.2 !!
//            reader.beginArray {
//                while (reader.hasNext()) { // requires "[]" array and not {"something": []}
//                    val issue = klaxon.parse<Issue>(reader)
//                    this.markers!!.add(issue!!)
//                }
//            }
//        }
       // this.allMarkersMap.clear()
        this.markers.clear()
        Utils.markers.clear()

        if(this.markers.size == 0) {
            for (ds: DataSnapshot in  dataSnapshot.children) {
                val issue = ds.getValue(Issue::class.java)
                markers.add(issue!!)
            }
        }
        Utils.markers = markers
        addIssueMarkersToTheMap()
    }

    private fun initMap(view: View, savedInstanceState: Bundle?) {
        Log.i("Markers size", this.markers.size.toString())
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.activity)
            this.mMapView = view.findViewById(R.id.map) as MapView
            this.mMapView!!.onCreate(savedInstanceState)
            this.mMapView!!.onResume() //without this, map showed but was empty
            this.mMapView!!.getMapAsync(this)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onLocationChanged(location: Location) {
            currentLocation = location
            moveToMyLocation()
        }
    }

    /**
     * setup the map
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.i("MAP READY", "READY")
        val position = if (currentLocation != null) LatLng(currentLocation!!.latitude, currentLocation!!.longitude) else Utils.vienna
        this.map = googleMap
        this.map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 8f)) // Vienna
        getFineLocationPermission()
        this.map!!.setOnMarkerClickListener(this)
        this.map!!.uiSettings.isRotateGesturesEnabled = true
        this.map!!.uiSettings.isZoomGesturesEnabled = true
        this.map!!.setOnInfoWindowClickListener(this)
        this.map!!.setOnMapLongClickListener(this)

        markersRef!!.addChildEventListener(markerChildEventListener)
        markersRef!!.addValueEventListener(markersListener) // keeps listening vs addListenerForSingleValueEvent only once
    }

    /**
        add the markers (Issues) to the map with the appropriate image
     */
    private fun addIssueMarkersToTheMap() {
        if (this.map != null) {
            Log.i("this.markers.size", this.markers.size.toString())
            for(marker in this.markers!!) {
                val id = resources.getIdentifier(marker.type, "drawable", activity.packageName)
                var newMarker = this.map!!.addMarker(MarkerOptions().title(marker.name).snippet(marker.description)
                        .position(LatLng(marker.lat, marker.lng))
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView!!, id))))
                this.allMarkersMap[newMarker] = marker
            }
        }
    }

    /**
     * go to the details view of the issue on windowClick
     */
    override fun onInfoWindowClick(marker: Marker) {
        val ft = fragmentManager.beginTransaction()
        val args = Bundle()
        val issueMarker = this.allMarkersMap[marker]

        Log.i("info window", "all markers: " + this.allMarkersMap.size.toString())
        if (marker.title != "Create issue here") { //if its not the one created by long click
            val detailsFragment = IssueDetailsFragment()
            args.putDouble("lat", issueMarker!!.lat)
            args.putDouble("lng", issueMarker!!.lng)
            args.putString("description", issueMarker.description)
            args.putString("name", issueMarker.name)
            args.putString("type", issueMarker.type)
            args.putString("imgUrl", issueMarker!!.imgUrl)

            detailsFragment.arguments = args
            ft.replace(R.id.main_fragment_content, detailsFragment)
                    .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null).commit()
        } else {
            val reportFragment = ReportFragment()
            args.putDouble("lat", marker.position.latitude)
            args.putDouble("lng", marker.position.longitude)
            reportFragment.arguments = args

            ft.replace(R.id.main_fragment_content, reportFragment)
                    .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("backstack").commit()        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        moveToMyLocation()
        return false
    }
    override fun onMapLongClick(latLng: LatLng) {
        Log.i(TAG, "long click")
        this.map!!.addMarker(MarkerOptions()
                    .position(latLng)
                    .title("Create issue here")
                    .snippet("Click me!"))
    }

    override fun onConnectionFailed(p0: ConnectionResult) {return}
    override fun onConnectionSuspended(p0: Int) {return}
    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "Please allow ACCESS_COARSE_LOCATION persmission.", Toast.LENGTH_LONG).show()
            return
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        moveToMyLocation()
    }

    @SuppressWarnings("MissingPermission")
    fun moveToMyLocation() {
        if (currentLocation != null) {
            val position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            Utils(position)
            this.map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(position, CAMERA_ZOOM), null)
        } else {
            val position = this.locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            Utils(LatLng(position.latitude, position.longitude))
            map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(position.latitude, position.longitude), CAMERA_ZOOM))
        }
    }

    /**
     * @param view is custom marker layout which we will convert into bitmap.
     * @param resId is the drawable which you want to show in marker.
     * @return bitmap
     */
    private fun getMarkerBitmapFromView(view: View, @DrawableRes resId: Int): Bitmap {
        var mMarkerImageView = this.markerView!!.findViewById(R.id.marker_image) as ImageView
        mMarkerImageView.setImageResource(resId)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = view.background
        drawable?.draw(canvas)
        view.draw(canvas)
        return returnedBitmap
    }

    private fun getFineLocationPermission() {
        val permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            this.map!!.isMyLocationEnabled = true
            if (isGPSEnabled()){
                this.locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
                this.locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, locationListener)
                val lastKnownLocation = this.locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnownLocation != null) {
                    map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude), CAMERA_ZOOM))
                }
            } else {
                showGPSDisabledAlertToUser()
            }
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GPS_PERMISSION_CODE)
        }
    }

    /**
     * check if gps is enabled.
     * GPS_PROVIDER AND NETWORK_PROVIDER have to be checked - one can have gps enabled on 'battery saving' = gps_provider turned off
     */
    private fun isGPSEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun showGPSDisabledAlertToUser(){
        var alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton("Enable GPS",
                    { dialog, i ->
                        val callGPSSettingIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(callGPSSettingIntent)
                    })
        alertDialogBuilder.setNegativeButton("Cancel", { dialog, i -> dialog.cancel() })
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            200 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFineLocationPermission()
                }
            }
        }
    }

     /** Called when the user clicks a marker.*/
     override fun onMarkerClick(marker: Marker):Boolean {
         // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
     }
    private fun isInBounds (position: LatLng , latLngBounds: LatLngBounds): Boolean {
        if (latLngBounds == null) {
            return this.map!!.projection.visibleRegion.latLngBounds.contains(position)
        } else {
            return latLngBounds.contains(position)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
        according to android developers:
        Users of the MapView class must forward all the life cycle methods from the Activity or Fragment
        containing this view to the corresponding ones in this class.
    */

    @SuppressWarnings("MissingPermission")
    override fun onResume() {
        super.onResume()
        Log.i("mapp", "on resume")
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, locationListener)
        mMapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        if(mMapView != null) mMapView!!.onStart()
    }
    override fun onPause() {
        super.onPause()
        Log.i("mapp", "on pause")
        locationManager?.removeUpdates(locationListener)
        mMapView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("mapp", "on destroy")
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
}

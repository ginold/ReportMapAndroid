package com.example.ginold.reportmap.fragments


import android.Manifest
import android.os.Bundle
import android.app.Fragment
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.support.v4.content.PermissionChecker.checkSelfPermission
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.ginold.reportmap.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapView
import android.support.v4.app.ActivityCompat
import android.graphics.drawable.Drawable
import android.graphics.PorterDuff
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.Image
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class MapFragment : Fragment(), OnMapReadyCallback, OnMarkerClickListener {

    private var mMapView: MapView? = null
    private var map: GoogleMap? = null
    private var markerView: View? = null
    private var locationManager : LocationManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maps, container, false) as View
        this.markerView = activity.getLayoutInflater().inflate(R.layout.marker, null)

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.activity)
            mMapView = view.findViewById<MapView>(R.id.map) as MapView
            mMapView!!.onCreate(savedInstanceState)
            mMapView!!.onResume() //without this, map showed but was empty
            mMapView!!.getMapAsync(this)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onLocationChanged(location: Location) {
            Log.i("LOCATION", location.latitude.toString() + " " + location.longitude.toString())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
        getFineLocationPermission()
        this.map!!.setOnMarkerClickListener(this)
        this.map!!.uiSettings.isMyLocationButtonEnabled = true
        this.map!!.uiSettings.isRotateGesturesEnabled = true
        this.map!!.uiSettings.isZoomGesturesEnabled = true
        val marker = LatLng(-33.867, 151.206)
        this.map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13f))
        this.map!!.addMarker(MarkerOptions().title("Hello Google Maps!").snippet("Population: 4,627,300")
                .position(marker)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView!!,R.drawable.ic_menu_black_24dp))))
    }

    /**
     * @param view is custom marker layout which we will convert into bitmap.
     * @param resId is the drawable which you want to show in marker.
     * @return bitmap
     */
    private fun getMarkerBitmapFromView(view: View, @DrawableRes resId: Int): Bitmap {
        var mMarkerImageView = this.markerView!!.findViewById<ImageView>(R.id.marker_image) as ImageView
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
            this.map!!.setMyLocationEnabled(true)
            this.locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
            this.locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, locationListener)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            200 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // {Some Code}
                }
            }
        }
    }

     /** Called when the user clicks a marker.*/
     override fun onMarkerClick(marker:Marker):Boolean {
         // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
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
}

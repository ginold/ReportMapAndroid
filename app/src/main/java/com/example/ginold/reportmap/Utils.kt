package com.example.ginold.reportmap

import android.location.Location
import com.example.ginold.reportmap.models.Issue
import com.google.android.gms.maps.model.LatLng

class Utils (location: LatLng){
    companion object {
        lateinit var currentLocation: LatLng
        var vienna= LatLng(48.191376, 16.389672)
        var markers = ArrayList<Issue>()
    }

    init {
        vienna = LatLng(48.191376, 16.389672)
        currentLocation = location
        markers = ArrayList()
    }
}
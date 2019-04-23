package com.example.ginold.reportmap.models

/**
 * Default values against error ->   does not define a no-argument constructor
 */
data class Issue(val name: String = "", val type: String = "", val lat: Double = 0.00, val lng: Double = 0.00, val description: String = "",
                 val imgUrl: String? = "", val id: Int? = 0) {

}
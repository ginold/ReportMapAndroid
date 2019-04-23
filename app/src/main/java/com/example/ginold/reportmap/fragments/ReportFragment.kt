package com.example.ginold.reportmap.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.example.ginold.reportmap.R
import com.example.ginold.reportmap.Utils
import com.example.ginold.reportmap.models.Issue
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.jvm.internal.impl.resolve.constants.IntValue


class  ReportFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnCameraIdleListener {
    private var mMapView: MapView? = null
    private var map: GoogleMap? = null
    val CAMERA_PERMISSION_CODE = 0
    val CAMERA_REQUEST_CODE = 10
    lateinit var imageFilePath: String
    private var imgThumb: ImageView? = null
    private var spinner: Spinner? = null
    private var currentLocation: LatLng? = null  // TODO: get current location as static variable
    private var midLatLng: LatLng? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private var objectValues: Array<String>? = null
    private var imageUri: Uri? = null
    private var pictureTaken: Boolean = false
    private var progress: ProgressBar? = null

    private val database: FirebaseDatabase? = FirebaseDatabase.getInstance()
    private val markersRef: DatabaseReference? = database!!.getReference("markers")
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    private var imagesRef: StorageReference? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        if (arguments != null && arguments.getDouble("lat") != null) {
            this.lat = arguments.getDouble("lat")
            this.lng = arguments.getDouble("lng")
        }
        this.objectValues = resources.getStringArray(R.array.object_values) // for dropdown

        // TODO       this.currentLocation = Utils.currentLocation
        this.progress = view.findViewById(R.id.progressBar) as ProgressBar

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            initMap(view, savedInstanceState)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
        this.imgThumb = view.findViewById(R.id.takePictureThumbnail) as ImageButton
        val camBtn = view.findViewById(R.id.takePictureBtn) as ImageButton
        val createBtn = view.findViewById(R.id.createIssueBtn) as Button
        camBtn.setOnClickListener{
            takePicture()
        }
        this.imgThumb!!.setOnClickListener{
            takePicture()
        }

        this.spinner = view.findViewById<Spinner>(R.id.object_types_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(context, R.array.object_types, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.spinner!!.adapter = adapter

        createBtn.setOnClickListener{
            createIssue(view)
        }

        return view
    }

    fun createIssue(view: View) {
        this.progress!!.visibility = View.VISIBLE
        val layout = view.findViewById<LinearLayout>(R.id.reportLinearLayout)
        layout.alpha = 0.4f

        val random = UUID.randomUUID().toString()
        val imgUrl = "images/issue$random+.jpg"
        val name = view.findViewById<EditText>(R.id.issueNameEditText).text.toString()
        val desc = view.findViewById<EditText>(R.id.issueDescriptionEditText).text.toString()
        val type = this.objectValues!![this.spinner!!.selectedItemPosition]
        var issue = Issue(name, type, this.midLatLng!!.latitude, this.midLatLng!!.longitude, desc, imgUrl, null)

        this.imagesRef = storageRef.child(imgUrl)

        var uploadTask: UploadTask = this.imagesRef!!.putBytes(putImgToBytearray())

        uploadTask.addOnSuccessListener { taskSnapshot ->
            this.progress!!.visibility = View.GONE
            Toast.makeText(context,"upload Done",Toast.LENGTH_LONG).show()
            markersRef!!.child(random).setValue(issue)
            goToDetailsView(issue)
        }.addOnFailureListener {
            this.progress!!.visibility = View.GONE
            Toast.makeText(context,"upload failed",Toast.LENGTH_LONG).show()
        }.addOnProgressListener {
            var prog: Double  = (100.0 * it.bytesTransferred )/ it.totalByteCount
            Log.i("transfer", prog.toString())
            //this.progress!!.setProgress(prog.toInt(), true)
        }
    }

    private fun goToDetailsView(issue: Issue) {
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
    private fun putImgToBytearray(): ByteArray {
        val stream = ByteArrayOutputStream()
        val drawable = this.imgThumb!!.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    private fun initMap(view: View, savedInstanceState: Bundle?) {
        MapsInitializer.initialize(this.activity)
        mMapView = view.findViewById(R.id.mapView)
        mMapView!!.onCreate(savedInstanceState)
        mMapView!!.onResume() //without this, map showed but was empty
        mMapView!!.getMapAsync(this)
    }
    override fun onMapClick(latLan: LatLng) {
        this.map!!.addMarker(MarkerOptions().position(latLan).title("Marker"))
    }

    /**
     * Open a camera activity with the picture returned as a result to onActivityResult
     */
    fun openCamera() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(callCameraIntent.resolveActivity(activity.packageManager) != null) {
                val authorities = activity.packageName + ".fileprovider"
                this.imageUri = FileProvider.getUriForFile(context, authorities, imageFile)
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Could not create file!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * take a picture but check for camera permissions first
     */
    fun takePicture() {
        val permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if  (permissionGranted) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    /**
     * Open the camera after obtaining the permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("CAMERA", "Permission has been denied by user")
                } else {
                    openCamera()
                    Log.i("CAMERA", "Permission has been granted by user")
                }
            }
        }
    }

    /**
     * Save the picture to the thumbnail after taking it
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                //Getting the Bitmap from Gallery
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, this.imageUri) as Bitmap
                this.imgThumb!!.setImageBitmap(bitmap)
                this.pictureTaken = true
            } catch (e:IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Error loading image", Toast.LENGTH_LONG)
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "_"
        val storageDir: File = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(!storageDir.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    /**
     * update the the coordinated of the map center when moving the map
     */
    override fun onCameraIdle() {
        this.midLatLng = this.map!!.cameraPosition.target
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.map = googleMap
        this.map!!.setOnCameraIdleListener(this)
        this.map!!.setOnMapClickListener(this)
        if (this.lat != null) {
            this.map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(this.lat!!, this.lng!!), 8f))
        } else if(this.currentLocation != null) { // TODO
            this.map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(this.currentLocation, 8f))
        } else {
            this.map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(Utils.vienna, 8f)) // Vienna
        }
    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
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

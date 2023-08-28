package com.example.mytracking.activity.home

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mytracking.GeofenceBroadcastReceiver
import com.example.mytracking.R
import com.example.mytracking.databinding.FragmentHomeBinding
import com.example.mytracking.models.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    GoogleMap.OnMarkerDragListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocClient: FusedLocationProviderClient
    private var dbReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Driver")
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false
    private lateinit var auth: FirebaseAuth
    private val geofenceRadius = 500.0
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var mMarker: Marker
    private lateinit var geocoder: Geocoder



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Notifications permission rejected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= 33) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        auth = FirebaseAuth.getInstance()
        setupLocClient()
        geocoder = Geocoder(requireContext())
        dbReference = Firebase.database.reference
        dbReference.addValueEventListener(locListener)


    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setOnMapLongClickListener(this)
        mMap.setOnMarkerDragListener(this)

        getMyLocation()
        createLocationRequest()
        createLocationCallback()


    }

    private fun setupLocClient() {
        fusedLocClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


    @TargetApi(Build.VERSION_CODES.Q)
    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (runningQOrLater) {
                    requestBackgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    getMyLocation()
                }
            }
        }
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun checkForegroundAndBackgroundLocationPermission(): Boolean {
        val foregroundLocationApproved = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }
    @SuppressLint("MissingPermission")
    private val locListener = object : ValueEventListener {


        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                val rootRef = FirebaseDatabase.getInstance().reference
                val usersRef = rootRef.child("Driver")
                usersRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (userSnapshot in task.result!!.children) {
                            val lat = userSnapshot.child("location/latitude")
                                .getValue(Double::class.java)
                            val lng = userSnapshot.child("location/longitude")
                                .getValue(Double::class.java)

                            if (lat != null && lng != null) {

                                val latLng = LatLng(lat, lng)
                                mMap.addMarker(
                                    MarkerOptions().position(latLng)
                                        .title("${userSnapshot.child("name").value}")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_BLUE))
                                )


                                val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                                //update the camera with the CameraUpdate object
                                mMap.moveCamera(update)

                                geofencingClient = LocationServices.getGeofencingClient(requireContext())

                                val geofence = Geofence.Builder()
                                    .setRequestId("penumpang")
                                    .setCircularRegion(
                                        lat,
                                        lng,
                                        geofenceRadius.toFloat()
                                    )
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_ENTER)
                                    .setLoiteringDelay(1000)
                                    .build()

                                val geofencingRequest = GeofencingRequest.Builder()
                                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                                    .addGeofence(geofence)
                                    .build()

                                geofencingClient.removeGeofences(geofencePendingIntent).run {
                                    addOnCompleteListener {
                                        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                                            addOnSuccessListener {
                                                Toast.makeText(context, "Geofencing added", Toast.LENGTH_SHORT).show()
                                            }
                                            addOnFailureListener {
                                                Toast.makeText(context, "Geofencing not added : ${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "No Location Found",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }


            }

            else {
                // if location is null , log an error message
                Log.e(TAG, "driver location cannot be found")
                Toast.makeText(context, "driver location cannot be found", Toast.LENGTH_SHORT).show()
            }


        }
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(context, "Could not read from database", Toast.LENGTH_LONG).show()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        if (checkForegroundAndBackgroundLocationPermission()) {
            mMap.isMyLocationEnabled = true
                fusedLocClient.lastLocation.addOnCompleteListener {
                    val location = it.result //obtain location
                    val user = FirebaseAuth.getInstance().currentUser
                    val databaseRef: DatabaseReference = Firebase.database.reference
                    val locationlogging = Location(location.latitude, location.longitude)
                    databaseRef.child("Users").child(user!!.uid).child("userlocation").setValue(locationlogging)
                    if (location != null) {

                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(latLng)
                                .title("You are currently here")
                                .rotation(location.bearing)
                        )
                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                        //update the camera with the CameraUpdate object
                        mMap.moveCamera(update)

                    } else {
                        Toast.makeText(context, "No Location Found", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
    }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i(TAG, "onActivityResult: All location settings are satisfied.")
                RESULT_CANCELED ->
                    Toast.makeText(
                        context,
                        "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }




    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getMyLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(context, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation!!
                for (location in locationResult.locations) {
                    Log.d(TAG, "onLocationResult: " + location.latitude + ", " + location.longitude)

                    if (mMap != null) {
                        updateMarker(locationResult.lastLocation!!)
                    }


                }
            }
        }
    }

    private fun updateMarker(location: android.location.Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (mMarker == null) {
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
                .rotation(location.bearing)
            mMarker = mMap.addMarker(markerOptions)!!
            val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
            //update the camera with the CameraUpdate object
            mMap.moveCamera(update)

        } else {
            mMarker.position = latLng
            mMarker.rotation = location.bearing
            val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
            //update the camera with the CameraUpdate object
            mMap.moveCamera(update)

        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e(TAG, "Error : " + exception.message)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocClient.removeLocationUpdates(locationCallback)
    }

    @Suppress("DEPRECATION")
    override fun onMapLongClick(p0: LatLng) {
        Log.d(TAG, "onMapLongClick: $p0")

        try {
            val addresses = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
            if (addresses != null) {
                if (addresses.size > 0) {
                    val address = addresses[0]
                    val streetAddress = address.getAddressLine(0)
                    mMap.addMarker(
                        MarkerOptions().position(p0)
                            .title(streetAddress)
                            .draggable(true)
                    )
                }
            }
        }
        catch (e: IOException) {
                e.printStackTrace()
        }
    }

    override fun onMarkerDrag(p0: Marker) {
        Log.d(TAG, "onMarkerDrag: ")
    }

    @Suppress("DEPRECATION")
    override fun onMarkerDragEnd(p0: Marker) {
        Log.d(TAG, "onMarkerDragEnd: ")
        val latLng = p0.position
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null) {
                if (addresses.size > 0) {
                    val address = addresses[0]
                    val streetAddress = address.getAddressLine(0)
                    mMap.addMarker(
                        MarkerOptions().position(latLng)
                            .title(streetAddress)
                    )
                }
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onMarkerDragStart(p0: Marker) {
        Log.d(TAG, "onMarkerDragStart: ")
    }

    override fun onResume() {
        super.onResume()
        if (isTracking) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }


    companion object {
        private const val TAG = "HomeFragment"
    }



    }





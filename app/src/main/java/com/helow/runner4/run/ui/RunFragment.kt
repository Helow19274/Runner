package com.helow.runner4.run.ui

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.helow.runner4.Feedback
import com.helow.runner4.R
import com.helow.runner4.databinding.FragmentRunBinding
import com.helow.runner4.locationRequest
import com.helow.runner4.run.RunService
import com.helow.runner4.run.RunUtils

class RunFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: RunService
    private lateinit var polyline: Polyline
    private lateinit var marker: Marker
    private lateinit var map: GoogleMap
    private var isFollowing = true
    private var markerSet = false

    private var connection: ServiceConnection? = null

    private val enableGpsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                ensureGPSEnabled()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRunBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.followButton.setOnClickListener {
            isFollowing = if (isFollowing) {
                binding.followButton.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(android.R.color.holo_red_dark))
                false
            } else {
                binding.followButton.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(android.R.color.holo_green_dark))
                true
            }
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        binding.root.findViewWithTag<View>("GoogleWatermark").visibility = View.GONE
        map.uiSettings.isMapToolbarEnabled = false

        if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true

            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener {
                if (it != null)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.latitude,
                                it.longitude
                            ), 14f))
            }

            polyline = map.addPolyline(PolylineOptions().color(requireContext().getColor(android.R.color.holo_green_dark)))

            binding.startButton.setOnClickListener {
                if (isServiceRunning()) {
                    service.duration.removeObservers(viewLifecycleOwner)
                    service.points.removeObservers(viewLifecycleOwner)

                    unbindRunService()

                    val intent = Intent(requireContext(), RunService::class.java).apply { action =
                        RunService.ACTION_STOP
                    }
                    requireContext().startService(intent)

                    binding.startButton.setText(R.string.start)
                    markerSet = false

                    if ((1..100).random() <= 5) {
                        showFeedbackDialog()
                    }
                } else {
                    ensureGPSEnabled()
                }
            }

            if (isServiceRunning())  {
                bindRunService()
            }
        }
    }

    private fun bindRunService() {
        if (connection != null) {
            return
        }

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                this@RunFragment.service = (service as RunService.RunBinder).getService()
                startRun()
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
        connection = conn

        requireContext().bindService(
            Intent(requireContext(), RunService::class.java), conn,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindRunService() {
        if (connection == null) {
            return
        }

        requireContext().unbindService(connection!!)
        connection = null
    }

    private fun startRun() {
        service.duration.observe(viewLifecycleOwner) { diffTime ->
            binding.startButton.text = RunUtils.formatTimer(diffTime.toLong())
        }

        service.points.observe(viewLifecycleOwner) { points ->
            if (points.isNotEmpty()) {
                if (!markerSet) {
                    if (this::marker.isInitialized) {
                        marker.remove()
                    }
                    marker = map.addMarker(MarkerOptions().title(getString(R.string.start)).position(points.first()))!!
                    markerSet = true
                }
                polyline.points = points
                if (isFollowing) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.last(), 16f))
                }
            }
        }
    }

    fun ensureGPSEnabled() {
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                requireContext().startForegroundService(
                    Intent(
                        requireContext(),
                        RunService::class.java
                    ).apply { action = RunService.ACTION_START })
                bindRunService()
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                    enableGpsLauncher.launch(intentSenderRequest)
                }
            }
    }

    private fun isServiceRunning() = (requireContext().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Int.MAX_VALUE).any { it.service.className == RunService::class.java.name }

    fun showFeedbackDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val commentEt = view.findViewById<TextInputEditText>(R.id.commentEditText)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setCancelable(true)
            .setTitle(R.string.send_feedback)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.send, null)
            .create()

        dialog.setOnShowListener {
            val sendBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            sendBtn.isEnabled = false

            fun updateSendEnabled() {
                sendBtn.isEnabled = ratingBar.rating >= 1f
            }

            ratingBar.setOnRatingBarChangeListener { _, _, _ -> updateSendEnabled() }
            updateSendEnabled()

            sendBtn.setOnClickListener {
                val rating = ratingBar.rating.toInt().coerceIn(1, 5)
                val comment = commentEt.text?.toString()?.trim().orEmpty()

                Firebase.firestore.collection("users").document(Firebase.auth.uid!!).collection("feedback").add(
                    Feedback(rating, comment)
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindRunService()
        _binding = null
    }
}

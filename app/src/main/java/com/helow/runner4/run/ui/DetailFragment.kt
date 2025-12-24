package com.helow.runner4.run.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.helow.runner4.R
import com.helow.runner4.databinding.FragmentDetailBinding
import com.helow.runner4.run.Run
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class DetailFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<DetailFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(map: GoogleMap) {
        binding.root.findViewWithTag<View>("GoogleWatermark").visibility = View.GONE

        map.uiSettings.isMapToolbarEnabled = false
        if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            map.isMyLocationEnabled = true

        lifecycleScope.launch {
            val run = Firebase.firestore.collection("users").document(Firebase.auth.uid!!).collection("runs").document(args.runId).get().await().toObject<Run>()!!
            val route = run.route.map { LatLng(it.latitude, it.longitude) }

            map.addPolyline(
                PolylineOptions()
                    .addAll(route)
                    .color(requireContext().getColor(android.R.color.holo_green_dark))
            )
            map.addMarker(
                MarkerOptions()
                    .position(route.first())
                    .title("${getString(R.string.start)} (${
                        SimpleDateFormat(
                            "dd.MM.y HH:mm:ss",
                            Locale.getDefault()
                        ).format(run.startTime.toDate())})")
            )
            map.addMarker(
                MarkerOptions()
                    .position(route.last())
                    .title("${getString(R.string.finish)} (${
                        SimpleDateFormat(
                            "dd.MM.y HH:mm:ss",
                            Locale.getDefault()
                        ).format(run.finishTime.toDate())})")
            )

            val bounds = LatLngBounds.Builder()
            route.forEach { bounds.include(it) }
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150))
        }
    }
}
package com.helow.runner4.run

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint

@Keep
data class Run(
    @get:Exclude
    var id: String = "",
    val name: String = "Забег",
    val startTime: Timestamp = Timestamp.now(),
    val finishTime: Timestamp = Timestamp.now(),
    val distance: Double = 0.0,
    val route: List<GeoPoint> = listOf()
)
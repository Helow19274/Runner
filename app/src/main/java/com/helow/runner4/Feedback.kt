package com.helow.runner4

import androidx.annotation.Keep

@Keep
data class Feedback(
    val rating: Int,
    val comment: String
)
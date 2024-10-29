package com.willowtree.energychart.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnergyUsageSnapshot(
    val amount: Float,
    val unit: String,
) : Parcelable

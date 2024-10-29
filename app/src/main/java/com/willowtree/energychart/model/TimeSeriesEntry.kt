package com.willowtree.energychart.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class TimeSeriesEntry(
    val timestamp: Instant,
    val summary: EnergyUsageSummary
) : Parcelable

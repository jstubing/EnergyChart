package com.willowtree.energychart.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnergyUsageSummary(
    val usage: EnergyUsageSnapshot,
    val cost: EnergyCostSnapshot,
) : Parcelable

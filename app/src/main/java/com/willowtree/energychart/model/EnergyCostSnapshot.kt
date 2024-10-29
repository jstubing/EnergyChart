package com.willowtree.energychart.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnergyCostSnapshot(
    val amount: Float,
    val currencyCode: String,
) : Parcelable

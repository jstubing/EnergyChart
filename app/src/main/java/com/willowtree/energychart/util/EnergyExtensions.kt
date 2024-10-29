package com.willowtree.energychart.util

import com.willowtree.energychart.model.EnergyCostSnapshot
import com.willowtree.energychart.model.EnergyUsageSnapshot
import kotlin.math.roundToInt

val EnergyUsageSnapshot.formattedString get() = "${amount.roundToInt()} $unit"

val EnergyCostSnapshot.formattedString get() = amount.toCurrencyString(currencyCode)

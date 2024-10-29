package com.willowtree.energychart.model

data class DeviceEnergyUsageData(
    val entry: TimeSeriesEntry,
    val usagePercentage: Float,
    val runtime: Int
)

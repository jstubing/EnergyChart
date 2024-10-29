package com.willowtree.energychart.model

data class EnergyUsageDevice(
    val id: String,
    val name: String,
    val type: OcfDeviceType,
    val image: String?,
    val usagePercentage: Float,
    val summary: EnergyUsageSummary,
    val runtime: Int,
    val suggestion: String?,
    val usageData: List<DeviceEnergyUsageData>
)

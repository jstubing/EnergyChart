package com.willowtree.energychart.model

enum class OcfDeviceType(val ocfType: String) {

    LIGHT("oic.d.light"),
    SMART_LIGHT("oic.d.light.smart"),
    THERMOSTAT("oic.d.thermostat"),
    PLUG("oic.d.smartplug"),
    DOORBELL("telus.d.doorbell"),
    SENSOR("oic.d.sensor"),
    LOCK("oic.d.smartlock");

    companion object {
        fun fromString(type: String): OcfDeviceType {
            return entries.first { it.ocfType == type }
        }
    }
}

package com.aarilabs.autohmi.domain

import com.aarilabs.autohmi.data.model.TelemetrySource

data class VehicleState(
    val speedKph: Int = 0,
    val gear: String = "P",
    val batteryPercent: Int = 85,
    val rangeKm: Int = 320,
    val temperatureCelsius: Int = 22,
    val fanSpeed: Int = 2,
    val isHvacOn: Boolean = true,
    val speedSource: TelemetrySource = TelemetrySource.SIMULATED,
    val batterySource: TelemetrySource = TelemetrySource.SIMULATED,
    val rangeSource: TelemetrySource = TelemetrySource.SIMULATED,
    val gearSource: TelemetrySource = TelemetrySource.SIMULATED,
    val climateSource: TelemetrySource = TelemetrySource.SIMULATED,
    val rpmSource: TelemetrySource = TelemetrySource.SIMULATED,
    val speedLastUpdatedAtMillis: Long? = null
)

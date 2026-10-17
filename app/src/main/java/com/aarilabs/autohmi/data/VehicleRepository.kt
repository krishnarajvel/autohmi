package com.aarilabs.autohmi.data

import com.aarilabs.autohmi.domain.VehicleState
import kotlinx.coroutines.flow.StateFlow

interface VehicleRepository {
    val vehicleState: StateFlow<VehicleState>
    val isSimulationMode: Boolean
    fun setSpeed(speedKph: Int)
    fun setTemperature(temperatureCelsius: Int)
    fun setFanSpeed(level: Int)
    fun toggleHvac()
    fun updateGear(gear: String)
}

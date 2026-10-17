package com.aarilabs.autohmi.data

import com.aarilabs.autohmi.domain.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeVehicleRepository : VehicleRepository {

    override val isSimulationMode: Boolean = true
    private val _vehicleState = MutableStateFlow(VehicleState())
    override val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    override fun setSpeed(speedKph: Int) {
        val clampedSpeed = speedKph.coerceIn(0, 220)
        val newGear = if (clampedSpeed > 0) "D" else "P"
        _vehicleState.update { current ->
            current.copy(
                speedKph = clampedSpeed,
                gear = newGear,
            )
        }
    }

    override fun setTemperature(temperatureCelsius: Int) {
        val clampedTemp = temperatureCelsius.coerceIn(16, 30)
        _vehicleState.update { current ->
            current.copy(
                temperatureCelsius = clampedTemp,
                isHvacOn = true,
            )
        }
    }

    override fun setFanSpeed(level: Int) {
        val clampedLevel = level.coerceIn(0, 5)
        _vehicleState.update { current ->
            current.copy(
                fanSpeed = clampedLevel,
                isHvacOn = clampedLevel > 0,
            )
        }
    }

    override fun toggleHvac() {
        _vehicleState.update { current ->
            val nextHvacState = !current.isHvacOn
            val nextFanSpeed = if (nextHvacState && (current.fanSpeed == 0)) 2 else if (!nextHvacState) 0 else current.fanSpeed
            current.copy(
                isHvacOn = nextHvacState,
                fanSpeed = nextFanSpeed,
            )
        }
    }

    override fun updateGear(gear: String) {
        _vehicleState.update { current ->
            current.copy(gear = gear)
        }
    }
}

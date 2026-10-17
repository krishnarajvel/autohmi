package com.aarilabs.autohmi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aarilabs.autohmi.data.FakeVehicleRepository
import com.aarilabs.autohmi.data.VehicleRepository
import com.aarilabs.autohmi.data.automotive.AaosVehicleRepository
import com.aarilabs.autohmi.domain.VehicleState
import kotlinx.coroutines.flow.StateFlow

class AutoHmiViewModel(
    val repository: VehicleRepository = FakeVehicleRepository(),
) : ViewModel() {

    val isSimulationMode: Boolean = repository.isSimulationMode
    val uiState: StateFlow<VehicleState> = repository.vehicleState

    fun connectIfAaos() {
        (repository as? AaosVehicleRepository)?.connect()
    }

    fun setSpeed(speedKph: Int) {
        repository.setSpeed(speedKph)
    }

    fun setTemperature(temperatureCelsius: Int) {
        repository.setTemperature(temperatureCelsius)
    }

    fun setFanSpeed(level: Int) {
        repository.setFanSpeed(level)
    }

    fun toggleHvac() {
        repository.toggleHvac()
    }

    fun updateGear(gear: String) {
        repository.updateGear(gear)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("AutoHMI-Lifecycle", "ViewModel cleared")

        if (!isSimulationMode) {
            (repository as? AaosVehicleRepository)?.disconnect()
        }
    }
}

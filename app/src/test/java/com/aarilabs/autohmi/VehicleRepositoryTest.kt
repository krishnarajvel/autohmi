package com.aarilabs.autohmi

import com.aarilabs.autohmi.data.FakeVehicleRepository
import com.aarilabs.autohmi.viewmodel.AutoHmiViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VehicleRepositoryTest {

    private lateinit var repository: FakeVehicleRepository
    private lateinit var viewModel: AutoHmiViewModel

    @Before
    fun setUp() {
        repository = FakeVehicleRepository()
        viewModel = AutoHmiViewModel(repository)
    }

    @Test
    fun initialSpeed_isZeroAndGearIsParked() {
        val state = viewModel.uiState.value
        assertEquals(0, state.speedKph)
        assertEquals("P", state.gear)
    }

    @Test
    fun setSpeed_increasesSpeedAndSetsDriveGear() {
        viewModel.setSpeed(5)
        val state = viewModel.uiState.value
        assertEquals(5, state.speedKph)
        assertEquals("D", state.gear)
    }

    @Test
    fun setSpeed_clampsUpperLimitTo220() {
        viewModel.setSpeed(250)
        val state = viewModel.uiState.value
        assertEquals(220, state.speedKph)
        assertEquals("D", state.gear)
    }

    @Test
    fun setSpeed_clampsLowerLimitToZeroAndSetsParkGear() {
        viewModel.setSpeed(10)
        assertEquals(10, viewModel.uiState.value.speedKph)

        viewModel.setSpeed(-10)
        val state = viewModel.uiState.value
        assertEquals(0, state.speedKph)
        assertEquals("P", state.gear)
    }

    @Test
    fun setTemperature_updatesTemperatureAndActivatesHvac() {
        viewModel.setTemperature(24)
        val state = viewModel.uiState.value
        assertEquals(24, state.temperatureCelsius)
        assertTrue(state.isHvacOn)
    }

    @Test
    fun setFanSpeed_updatesLevelAndHvacState() {
        viewModel.setFanSpeed(3)
        assertTrue(viewModel.uiState.value.isHvacOn)
        assertEquals(3, viewModel.uiState.value.fanSpeed)

        viewModel.setFanSpeed(0)
        assertFalse(viewModel.uiState.value.isHvacOn)
        assertEquals(0, viewModel.uiState.value.fanSpeed)
    }
}

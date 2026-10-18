package com.aarilabs.autohmi

import android.content.Context
import android.content.ContextWrapper
import com.aarilabs.autohmi.data.automotive.AaosVehicleRepository
import com.aarilabs.autohmi.data.model.TelemetrySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class TestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}

class AaosVehicleRepositoryTest {

    private lateinit var repository: AaosVehicleRepository

    @Before
    fun setUp() {
        val context = TestContext()
        repository = AaosVehicleRepository(context)
    }

    @Test
    fun initialState_isUnavailable() {
        val state = repository.vehicleState.value
        assertEquals(TelemetrySource.UNAVAILABLE, state.speedSource)
        assertEquals(0, state.speedKph)
        assertNull(state.speedLastUpdatedAtMillis)
    }

    @Test
    fun initialRead_updatesSpeedAndSourceToLive() {
        repository.onVhalSpeedReceived(0)

        val state = repository.vehicleState.value
        assertEquals(0, state.speedKph)
        assertEquals(TelemetrySource.LIVE, state.speedSource)
        assertNotNull(state.speedLastUpdatedAtMillis)
    }

    @Test
    fun speedChanged_updatesSpeedAndKeepsSourceLive() {
        repository.onVhalSpeedReceived(0)
        val initialTimestamp = repository.vehicleState.value.speedLastUpdatedAtMillis

        Thread.sleep(10)
        repository.onVhalSpeedReceived(19)

        val state = repository.vehicleState.value
        assertEquals(19, state.speedKph)
        assertEquals(TelemetrySource.LIVE, state.speedSource)
        assertNotNull(state.speedLastUpdatedAtMillis)
        assert(state.speedLastUpdatedAtMillis!! >= initialTimestamp!!)
    }

    @Test
    fun unchangedSpeed_remainsLiveWithoutFalseStaleTimeout() {
        repository.onVhalSpeedReceived(0)
        assertEquals(TelemetrySource.LIVE, repository.vehicleState.value.speedSource)

        // Simulate time passing with no speed callback (stationary vehicle)
        Thread.sleep(50)

        // Telemetry must REMAIN TelemetrySource.LIVE because SENSOR_RATE_ONCHANGE does not fire when unchanged
        val state = repository.vehicleState.value
        assertEquals(0, state.speedKph)
        assertEquals(TelemetrySource.LIVE, state.speedSource)
    }

    @Test
    fun disconnection_marksSpeedSourceUnavailable() {
        repository.onVhalSpeedReceived(12)
        assertEquals(TelemetrySource.LIVE, repository.vehicleState.value.speedSource)

        repository.handleCarServiceDisconnected()

        val state = repository.vehicleState.value
        assertEquals(TelemetrySource.UNAVAILABLE, state.speedSource)
    }

    @Test
    fun reconnection_restoresSpeedSourceToLive() {
        repository.onVhalSpeedReceived(12)
        repository.handleCarServiceDisconnected()
        assertEquals(TelemetrySource.UNAVAILABLE, repository.vehicleState.value.speedSource)

        // Reconnection initial read
        repository.onVhalSpeedReceived(0)

        val state = repository.vehicleState.value
        assertEquals(0, state.speedKph)
        assertEquals(TelemetrySource.LIVE, state.speedSource)
    }

    @Test
    fun propertyError_marksSpeedSourceUnavailable() {
        repository.onVhalSpeedReceived(25)
        assertEquals(TelemetrySource.LIVE, repository.vehicleState.value.speedSource)

        repository.handleCarPropertyError()

        val state = repository.vehicleState.value
        assertEquals(TelemetrySource.UNAVAILABLE, state.speedSource)
    }
}


package com.aarilabs.autohmi.data.automotive

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.aarilabs.autohmi.data.VehicleRepository
import com.aarilabs.autohmi.data.model.TelemetrySource
import com.aarilabs.autohmi.domain.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * AAOS vehicle-data adapter implementing [VehicleRepository].
 */
class AaosVehicleRepository(
    context: Context
) : VehicleRepository {

    override val isSimulationMode: Boolean = false

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var car: Car? = null
    private var propertyManager: CarPropertyManager? = null

    private val _vehicleState = MutableStateFlow(
        VehicleState(
            speedSource = TelemetrySource.UNAVAILABLE
        )
    )

    override val vehicleState: StateFlow<VehicleState> =
        _vehicleState.asStateFlow()

    fun connect() {
        if (car != null) {
            Log.d(TAG, "Car connection already initialized")
            return
        }

        Log.d(TAG, "Connecting to AAOS Car service...")

        car = Car.createCar(
            appContext,
            mainHandler,
            Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT
        ) { carInstance, ready ->
            handleCarServiceState(carInstance, ready)
        }
    }

    internal fun handleCarServiceState(
        carInstance: Car?,
        ready: Boolean
    ) {
        Log.d(TAG, "Car service ready=$ready")

        if (!ready) {
            handleCarServiceDisconnected()
            return
        }

        val manager = carInstance?.getCarManager(
            Car.PROPERTY_SERVICE
        ) as? CarPropertyManager

        if (manager == null) {
            Log.e(TAG, "CarPropertyManager unavailable")
            handleCarServiceDisconnected()
            return
        }

        // Clean up the previous manager before replacing it.
        val previousManager = propertyManager

        if (previousManager != null && previousManager !== manager) {
            try {
                previousManager.unregisterCallback(callback)
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    "Error unregistering previous callback: ${e.message}"
                )
            }
        }

        propertyManager = manager
        Log.d(TAG, "CarPropertyManager connected")

        val config = manager.getCarPropertyConfig(
            VehiclePropertyIds.PERF_VEHICLE_SPEED
        )

        Log.d(
            TAG,
            "Vehicle speed property available=${config != null}"
        )

        if (config == null) {
            handleCarPropertyError()
            return
        }

        try {
            // Avoid duplicate registration on the same manager.
            manager.unregisterCallback(callback)
        } catch (_: Exception) {
            // No existing registration is also a valid case.
        }

        val registered = manager.registerCallback(
            callback,
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        )

        if (!registered) {
            Log.e(TAG, "Failed to register vehicle speed callback")
            handleCarPropertyError()
            return
        }

        refreshSpeed(manager)
    }

    internal fun handleCarServiceDisconnected() {
        Log.w(TAG, "AAOS Car service disconnected or unavailable")

        try {
            propertyManager?.unregisterCallback(callback)
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Error unregistering callback during disconnect: ${e.message}"
            )
        }

        propertyManager = null

        _vehicleState.update { current ->
            current.copy(
                speedSource = TelemetrySource.UNAVAILABLE
            )
        }
    }

    private val callback =
        object : CarPropertyManager.CarPropertyEventCallback {

            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (
                    value.propertyId !=
                    VehiclePropertyIds.PERF_VEHICLE_SPEED
                ) {
                    return
                }

                if (
                    value.status !=
                    CarPropertyValue.STATUS_AVAILABLE
                ) {
                    Log.w(
                        TAG,
                        "Vehicle speed property unavailable; " +
                                "status=${value.status}"
                    )
                    handleCarPropertyError()
                    return
                }

                val metresPerSecond = parseSpeed(value.value)

                if (metresPerSecond == null) {
                    Log.w(
                        TAG,
                        "Invalid vehicle speed value: ${value.value}"
                    )
                    handleCarPropertyError()
                    return
                }

                onVhalSpeedReceived(
                    (metresPerSecond * 3.6f).toInt()
                )
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.e(
                    TAG,
                    "CarPropertyManager error event " +
                            "for propId=$propId, zone=$zone"
                )

                if (propId == VehiclePropertyIds.PERF_VEHICLE_SPEED) {
                    handleCarPropertyError()
                }
            }
        }

    internal fun handleCarPropertyError() {
        _vehicleState.update { current ->
            current.copy(
                speedSource = TelemetrySource.UNAVAILABLE
            )
        }
    }

    private fun refreshSpeed(manager: CarPropertyManager) {
        try {
            val carPropValue: CarPropertyValue<*>? =
                manager.getProperty<CarPropertyValue<*>>(
                    VehiclePropertyIds.PERF_VEHICLE_SPEED,
                    0
                )

            val speedValue = carPropValue

            if (
                speedValue == null ||
                speedValue.status != CarPropertyValue.STATUS_AVAILABLE
            ) {
                Log.w(
                    TAG,
                    "Initial vehicle speed property is unavailable"
                )
                handleCarPropertyError()
                return
            }

            val metresPerSecond = parseSpeed(speedValue.value)

            if (metresPerSecond == null) {
                Log.w(
                    TAG,
                    "Initial vehicle speed value is invalid: " +
                            speedValue.value
                )
                handleCarPropertyError()
                return
            }

            val kph = (metresPerSecond * 3.6f).toInt()

            Log.d(
                TAG,
                "Initial vehicle speed read: " +
                        "$kph km/h ($metresPerSecond m/s)"
            )

            onVhalSpeedReceived(kph)

        } catch (exception: SecurityException) {
            Log.w(
                TAG,
                "SecurityException reading initial speed: " +
                        exception.message
            )
            handleCarPropertyError()

        } catch (exception: IllegalArgumentException) {
            Log.w(
                TAG,
                "IllegalArgumentException reading initial speed: " +
                        exception.message
            )
            handleCarPropertyError()

        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Error reading initial vehicle speed: " +
                        exception.message,
                exception
            )
            handleCarPropertyError()
        }
    }

    private fun parseSpeed(rawValue: Any?): Float? {
        val speed = when (rawValue) {
            is Number -> rawValue.toFloat()
            else -> return null
        }

        return speed.takeIf { it.isFinite() }

    }

    internal fun onVhalSpeedReceived(kph: Int) {
        updateSpeedState(kph)
    }

    override fun setSpeed(speedKph: Int) {
        Log.w(
            TAG,
            "Ignoring manual speed input in vehicle mode: " +
                    "$speedKph km/h"
        )
    }

    private fun updateSpeedState(speedKph: Int) {
        val clampedSpeed = speedKph.coerceIn(0, 220)
        val current = _vehicleState.value
        val speedChanged = current.speedKph != clampedSpeed

        _vehicleState.update {
            it.copy(
                speedKph = clampedSpeed,
                speedSource = TelemetrySource.LIVE,
                speedLastUpdatedAtMillis = System.currentTimeMillis()
            )
        }

        if (speedChanged) {
            Log.d(
                TAG,
                "Vehicle telemetry speed updated: " +
                        "$clampedSpeed km/h"
            )
        }
    }

    override fun setTemperature(temperatureCelsius: Int) {
        val clampedTemp = temperatureCelsius.coerceIn(16, 30)

        if (_vehicleState.value.temperatureCelsius != clampedTemp) {
            Log.d(TAG, "Temperature updated: $clampedTemp °C")

            _vehicleState.update { current ->
                current.copy(
                    temperatureCelsius = clampedTemp,
                    isHvacOn = true
                )
            }
        }
    }

    override fun setFanSpeed(level: Int) {
        val clampedLevel = level.coerceIn(0, 5)

        if (_vehicleState.value.fanSpeed != clampedLevel) {
            Log.d(TAG, "Fan speed updated: Level $clampedLevel")

            _vehicleState.update { current ->
                current.copy(
                    fanSpeed = clampedLevel,
                    isHvacOn = clampedLevel > 0
                )
            }
        }
    }

    override fun toggleHvac() {
        _vehicleState.update { current ->
            val nextHvacState = !current.isHvacOn
            val nextFan = when {
                nextHvacState && current.fanSpeed == 0 -> 2
                !nextHvacState -> 0
                else -> current.fanSpeed
            }

            Log.d(
                TAG,
                "HVAC toggled: isHvacOn=$nextHvacState, " +
                        "fanSpeed=$nextFan"
            )

            current.copy(
                isHvacOn = nextHvacState,
                fanSpeed = nextFan
            )
        }
    }

    override fun updateGear(gear: String) {
        if (_vehicleState.value.gear != gear) {
            Log.d(TAG, "Gear updated: $gear")

            _vehicleState.update { current ->
                current.copy(gear = gear)
            }
        }
    }

    fun disconnect() {
        handleCarServiceDisconnected()

        car?.disconnect()
        car = null
    }

    private companion object {
        const val TAG = "AutoHMI-AAOS"
    }
}

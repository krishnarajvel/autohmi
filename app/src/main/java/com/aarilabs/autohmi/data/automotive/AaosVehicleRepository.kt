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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    private val _vehicleState = MutableStateFlow(VehicleState(speedSource = TelemetrySource.UNAVAILABLE))
    override val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var staleMonitorJob: Job? = null

    private val staleTimeoutMs = 3_000L

    fun connect() {
        if (car != null) {
            Log.d("AutoHMI-AAOS", "Car connection already initialized")
            return
        }

        Log.d("AutoHMI-AAOS", "Connecting to AAOS Car service...")

        car = Car.createCar(
            appContext,
            mainHandler,
            Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT
        ) { carInstance, ready ->

            Log.d("AutoHMI-AAOS", "Car service ready=$ready")

            if (!ready) return@createCar

            val manager = carInstance.getCarManager(
                Car.PROPERTY_SERVICE
            ) as? CarPropertyManager

            if (manager == null) {
                Log.e("AutoHMI-AAOS", "CarPropertyManager unavailable")
                return@createCar
            }

            propertyManager = manager
            Log.d("AutoHMI-AAOS", "CarPropertyManager connected")

            val config = manager.getCarPropertyConfig(
                VehiclePropertyIds.PERF_VEHICLE_SPEED
            )

            Log.d(
                "AutoHMI-AAOS",
                "Vehicle speed property available=${config != null}"
            )

            if (config != null) {
                manager.registerCallback(
                    callback,
                    VehiclePropertyIds.PERF_VEHICLE_SPEED,
                    CarPropertyManager.SENSOR_RATE_ONCHANGE
                )

                refreshSpeed(manager)
            }
        }
    }

    private val callback =
        object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.propertyId == VehiclePropertyIds.PERF_VEHICLE_SPEED) {
                    val rawValue = value.value
                    val metresPerSecond = when (rawValue) {
                        is Float -> rawValue
                        is Number -> rawValue.toFloat()
                        else -> null
                    }
                    if (metresPerSecond != null) {
                        val kph = (metresPerSecond * 3.6f).toInt()
                        onVhalSpeedReceived(kph)
                    }
                }
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.e("AutoHMI-AAOS", "CarPropertyManager error event for propId=$propId, zone=$zone")
            }
        }

    private fun refreshSpeed(manager: CarPropertyManager) {
        try {
            val carPropValue: CarPropertyValue<*>? = manager.getProperty<CarPropertyValue<*>>(
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                0
            )
            val rawValue = carPropValue?.value
            val metresPerSecond = when (rawValue) {
                is Float -> rawValue
                is Number -> rawValue.toFloat()
                else -> null
            }

            if (metresPerSecond != null) {
                val kph = (metresPerSecond * 3.6f).toInt()
                Log.d("AutoHMI-AAOS", "Initial vehicle speed read: $kph km/h ($metresPerSecond m/s)")
                onVhalSpeedReceived(kph)
            } else {
                Log.w("AutoHMI-AAOS", "Initial vehicle speed value is null or unsupported type: $rawValue")
            }
        } catch (exception: SecurityException) {
            Log.w("AutoHMI-AAOS", "SecurityException reading initial speed: ${exception.message}")
        } catch (exception: IllegalArgumentException) {
            Log.w("AutoHMI-AAOS", "IllegalArgumentException reading initial speed: ${exception.message}")
        } catch (exception: Exception) {
            Log.e("AutoHMI-AAOS", "Error reading initial vehicle speed: ${exception.message}", exception)
        }
    }

    private fun onVhalSpeedReceived(kph: Int) {
        updateSpeedState(kph)
    }

    override fun setSpeed(speedKph: Int) {
        Log.w(
            "AutoHMI-AAOS",
            "Ignoring manual speed input in vehicle mode: $speedKph km/h"
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
                "AutoHMI-AAOS",
                "Vehicle telemetry speed updated: $clampedSpeed km/h"
            )
        }
    }

    override fun setTemperature(temperatureCelsius: Int) {
        val clampedTemp = temperatureCelsius.coerceIn(16, 30)
        if (_vehicleState.value.temperatureCelsius != clampedTemp) {
            Log.d("AutoHMI-AAOS", "Temperature updated: $clampedTemp °C")
            _vehicleState.update { current ->
                current.copy(temperatureCelsius = clampedTemp, isHvacOn = true)
            }
        }
    }

    override fun setFanSpeed(level: Int) {
        val clampedLevel = level.coerceIn(0, 5)
        if (_vehicleState.value.fanSpeed != clampedLevel) {
            Log.d("AutoHMI-AAOS", "Fan speed updated: Level $clampedLevel")
            _vehicleState.update { current ->
                current.copy(fanSpeed = clampedLevel, isHvacOn = clampedLevel > 0)
            }
        }
    }

    override fun toggleHvac() {
        _vehicleState.update { current ->
            val nextHvacState = !current.isHvacOn
            val nextFan = if (nextHvacState && current.fanSpeed == 0) 2 else if (!nextHvacState) 0 else current.fanSpeed
            Log.d("AutoHMI-AAOS", "HVAC toggled: isHvacOn=$nextHvacState, fanSpeed=$nextFan")
            current.copy(isHvacOn = nextHvacState, fanSpeed = nextFan)
        }
    }

    override fun updateGear(gear: String) {
        if (_vehicleState.value.gear != gear) {
            Log.d("AutoHMI-AAOS", "Gear updated: $gear")
            _vehicleState.update { current ->
                current.copy(gear = gear)
            }
        }
    }

    fun disconnect() {
        propertyManager?.unregisterCallback(callback)
        propertyManager = null

        car?.disconnect()
        car = null
    }
}

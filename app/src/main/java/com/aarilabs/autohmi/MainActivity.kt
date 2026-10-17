package com.aarilabs.autohmi

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aarilabs.autohmi.ui.dashboard.DashboardScreen
import com.aarilabs.autohmi.ui.theme.AutoHMITheme
import com.aarilabs.autohmi.viewmodel.AutoHmiViewModel
import com.aarilabs.autohmi.viewmodel.AutoHmiViewModelFactory

class MainActivity : ComponentActivity() {
    private val useAaos = true

    private val viewModel: AutoHmiViewModel by viewModels {
        AutoHmiViewModelFactory(
            context = applicationContext,
            useAaos = useAaos,
        )
    }

    private val speedPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                Log.d("AutoHMI-AAOS", "Vehicle speed permission granted")
                connectAaosRepository()
            } else {
                Log.w(
                    "AutoHMI-AAOS",
                    "Vehicle speed permission denied",
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AutoHMITheme {
                AutoHmiApp(viewModel)
            }
        }

        if (useAaos) {
            checkSpeedPermission()
        }
    }

    private fun checkSpeedPermission() {
        val permission = "android.car.permission.CAR_SPEED"

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(
                    "AutoHMI-AAOS",
                    "Vehicle speed permission granted"
                )
                connectAaosRepository()
            }

            else -> {
                speedPermissionLauncher.launch(permission)
            }
        }
    }

    private fun connectAaosRepository() {
        viewModel.connectIfAaos()
    }
}

@Composable
fun AutoHmiApp(viewModel: AutoHmiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        vehicleState = uiState,
        isSimulationMode = viewModel.isSimulationMode,
        onSpeedChange = viewModel::setSpeed,
        onTemperatureChange = viewModel::setTemperature,
        onFanSpeedChange = viewModel::setFanSpeed,
        onToggleHvac = viewModel::toggleHvac,
    )
}

package com.aarilabs.autohmi.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aarilabs.autohmi.data.FakeVehicleRepository
import com.aarilabs.autohmi.data.VehicleRepository
import com.aarilabs.autohmi.data.automotive.AaosVehicleRepository

class AutoHmiViewModelFactory(
    context: Context,
    private val useAaos: Boolean
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AutoHmiViewModel::class.java)) {
            val repository: VehicleRepository =
                if (useAaos) {
                    AaosVehicleRepository(appContext)
                } else {
                    FakeVehicleRepository()
                }

            @Suppress("UNCHECKED_CAST")
            return AutoHmiViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

package com.aarilabs.autohmi.ui.climate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ClimateBackground = Color(0xFF10141B)
private val ClimateCard = Color(0xFF1D2633)
private val ClimateAccent = Color(0xFF50E3C2)
private val ClimateMuted = Color(0xFFAAB6C5)

@Composable
fun ClimateScreen(
    temperature: Int,
    onTemperatureChange: (Int) -> Unit,
    fanSpeed: Int,
    onFanSpeedChange: (Int) -> Unit,
    hvacOn: Boolean,
    onHvacChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ClimateBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "CLIMATE CONTROL",
                color = ClimateAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Make yourself comfortable",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    color = ClimateCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TEMPERATURE",
                            color = ClimateMuted,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "$temperature°C",
                            color = Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Light
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onTemperatureChange(
                                        (temperature - 1).coerceAtLeast(16)
                                    )
                                }
                            ) {
                                Text("−")
                            }

                            Button(
                                onClick = {
                                    onTemperatureChange(
                                        (temperature + 1).coerceAtMost(30)
                                    )
                                }
                            ) {
                                Text("+")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "16°C – 30°C",
                            color = ClimateMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = ClimateCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "FAN SPEED",
                                color = ClimateMuted,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = fanSpeed.toString(),
                                color = ClimateAccent,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onFanSpeedChange(
                                            (fanSpeed - 1).coerceAtLeast(0)
                                        )
                                    }
                                ) {
                                    Text("−")
                                }

                                Button(
                                    onClick = {
                                        onFanSpeedChange(
                                            (fanSpeed + 1).coerceAtMost(4)
                                        )
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            Text(
                                text = "Level 0 – 4",
                                color = ClimateMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = ClimateCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "AIR CONDITIONING",
                                color = ClimateMuted,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (hvacOn) "ON" else "OFF",
                                color = if (hvacOn) ClimateAccent else Color.Gray,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onHvacChange(!hvacOn)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (hvacOn) "Turn OFF" else "Turn ON"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.aarilabs.autohmi.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarilabs.autohmi.data.model.TelemetrySource
import com.aarilabs.autohmi.domain.VehicleState
import com.aarilabs.autohmi.ui.theme.HmiBackground
import com.aarilabs.autohmi.ui.theme.HmiBlueGradientEnd
import com.aarilabs.autohmi.ui.theme.HmiCardBackground
import com.aarilabs.autohmi.ui.theme.HmiCardStroke
import com.aarilabs.autohmi.ui.theme.HmiMintAccent
import com.aarilabs.autohmi.ui.theme.HmiMintDark
import com.aarilabs.autohmi.ui.theme.HmiPanelBackground
import com.aarilabs.autohmi.ui.theme.HmiSkyBlue
import com.aarilabs.autohmi.ui.theme.HmiTextMuted
import com.aarilabs.autohmi.ui.theme.HmiTextPrimary
import com.aarilabs.autohmi.ui.theme.HmiTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Track(
    val title: String,
    val artist: String,
    val durationSec: Int
)

private val playlist = listOf(
    Track("Uyire Un Uyirena", "Anirudh Ravichander", 225),
    Track("Ponsththui (From Roongi)", "C. Sathya, Chinmayi", 252),
    Track("Hukum - Jailer", "Anirudh Ravichander", 200),
    Track("Naa Ready - Leo", "Anirudh Ravichander", 248)
)

@Composable
fun DashboardScreen(
    vehicleState: VehicleState = VehicleState(),
    isSimulationMode: Boolean = true,
    onSpeedChange: (Int) -> Unit = {},
    onTemperatureChange: (Int) -> Unit = {},
    onFanSpeedChange: (Int) -> Unit = {},
    onToggleHvac: () -> Unit = {}
) {
    var selectedDockTab by remember { mutableIntStateOf(1) } // 1 = Home

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HmiBackground)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 68.dp) // Space for bottom dock
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            VehicleModeLabel(
                isSimulationMode = isSimulationMode
            )

            // 1. Top Bar
            HmiTopHeaderBar()

            // 2. Main Grid Layout (3 Columns: Left Instrument, Center Controls, Right Map & Weather)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(580.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Column 1: Left Instrument Cluster
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    VehicleInstrumentClusterCard(
                        speedKph = vehicleState.speedKph,
                        isSimulationMode = isSimulationMode,
                        gear = vehicleState.gear,
                        batteryPercent = vehicleState.batteryPercent,
                        rangeKm = vehicleState.rangeKm,
                        onSpeedChange = onSpeedChange,
                        speedSource = vehicleState.speedSource,
                    )
                }

                // Column 2: Center Smart Widgets & Real Media Player
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Air Conditioner Widget
                    AirConditionerWidget(
                        temperature = vehicleState.temperatureCelsius,
                        fanSpeed = vehicleState.fanSpeed,
                        isHvacOn = vehicleState.isHvacOn,
                        onTemperatureChange = onTemperatureChange,
                        onFanSpeedChange = onFanSpeedChange,
                        onToggleHvac = onToggleHvac
                    )

                    // Real Interactive Media Player
                    MediaPlayerWidget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                // Column 3: Right Interactive Navigation & Weather
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Interactive Map Navigation Card
                    MapNavigationWidget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.6f)
                    )

                    // Weather & Air Quality Card
                    WeatherWidget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                    )
                }
            }
        }

        // 3. Floating Bottom Navigation Dock
        HmiBottomDockBar(
            selectedTab = selectedDockTab,
            onTabSelected = { selectedDockTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun VehicleModeLabel(isSimulationMode: Boolean) {
    Text(
        text = if (isSimulationMode) {
            "SIMULATION MODE"
        } else {
            "AAOS VEHICLE MODE"
        },
        style = MaterialTheme.typography.labelSmall,
        color = if (isSimulationMode) {
            Color(0xFFFFC857)
        } else {
            Color(0xFF69D6A5)
        }
    )
}

// ==========================================
// TOP HEADER BAR
// ==========================================
@Composable
private fun HmiTopHeaderBar() {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE MMM dd, yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000L)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(HmiPanelBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Dynamic Time & Date
        Column {
            Text(
                text = currentTimeString.ifEmpty { "10:00 am" },
                color = HmiTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentDateString.ifEmpty { "Sunday Oct 20, 2024" },
                color = HmiTextMuted,
                fontSize = 10.sp
            )
        }

        // Search Bar Pill
        Row(
            modifier = Modifier
                .width(180.dp)
                .height(30.dp)
                .background(HmiCardBackground, RoundedCornerShape(15.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = HmiTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Search",
                color = HmiTextMuted,
                fontSize = 11.sp
            )
        }

        // Status Actions & User Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(HmiCardBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = HmiMintAccent,
                    modifier = Modifier.size(15.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.SignalCellular4Bar,
                contentDescription = "Signal",
                tint = HmiTextSecondary,
                modifier = Modifier.size(16.dp)
            )

            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wifi",
                tint = HmiTextSecondary,
                modifier = Modifier.size(16.dp)
            )

            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = HmiMintAccent,
                modifier = Modifier.size(16.dp)
            )

            Row(
                modifier = Modifier
                    .background(HmiCardBackground, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = HmiTextPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .background(HmiMintDark, CircleShape)
                        .padding(2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sumi_imma",
                    color = HmiTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==========================================
// LEFT INSTRUMENT CLUSTER
// ==========================================
@Composable
private fun VehicleInstrumentClusterCard(
    speedKph: Int,
    gear: String,
    speedSource: TelemetrySource,
    batteryPercent: Int,
    rangeKm: Int,
    isSimulationMode: Boolean,
    onSpeedChange: (Int) -> Unit
) {
    val dynamicRpm = if (speedKph == 0) 800 else (800 + speedKph * 32).coerceAtMost(6500)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, HmiCardStroke, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = HmiPanelBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(HmiCardBackground, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                VehicleGraphicCanvas()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerticalGaugeBar(
                    label = "$batteryPercent%",
                    progress = (batteryPercent / 100f).coerceIn(0f, 1f),
                    activeColor = HmiMintAccent,
                    subLabel = "Bat"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$speedKph",
                            color = HmiTextPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "km/h",
                            color = HmiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = "GEAR $gear",
                        color = HmiMintAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (speedSource) {
                            TelemetrySource.LIVE -> "LIVE"
                            TelemetrySource.SIMULATED -> "DEMO"
                            TelemetrySource.UNAVAILABLE -> "N/A"
                        },
                        color = when (speedSource) {
                            TelemetrySource.LIVE -> HmiMintAccent
                            TelemetrySource.SIMULATED -> Color(0xFFFFC857)
                            TelemetrySource.UNAVAILABLE -> HmiTextMuted
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(2.dp)
                            .background(HmiMintAccent)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CompassRpmGraphCanvas()

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$dynamicRpm RPM",
                        color = HmiTextMuted,
                        fontSize = 10.sp
                    )

                    val speedControlsEnabled = isSimulationMode

                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(HmiCardBackground, CircleShape)
                                .clickable(enabled = speedControlsEnabled) {
                                    onSpeedChange((speedKph - 5).coerceAtLeast(0))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "-5",
                                color = if (speedControlsEnabled) HmiTextPrimary else HmiTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(HmiCardBackground, CircleShape)
                                .clickable(enabled = speedControlsEnabled) {
                                    onSpeedChange((speedKph + 5).coerceAtMost(220))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+5",
                                color = if (speedControlsEnabled) HmiTextPrimary else HmiTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                VerticalGaugeBar(
                    label = "Range",
                    progress = (rangeKm / 500f).coerceIn(0f, 1f),
                    activeColor = HmiMintAccent,
                    subLabel = "$rangeKm km"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                SemiCircularGaugeCanvas(displayValue = speedKph)
            }
        }
    }
}

@Composable
private fun VehicleGraphicCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = HmiMintAccent.copy(alpha = 0.08f),
            radius = w * 0.35f,
            center = Offset(w / 2f, h * 0.65f)
        )

        val carBody = Path().apply {
            moveTo(w * 0.25f, h * 0.75f)
            cubicTo(w * 0.20f, h * 0.70f, w * 0.22f, h * 0.50f, w * 0.32f, h * 0.38f)
            cubicTo(w * 0.40f, h * 0.28f, w * 0.60f, h * 0.28f, w * 0.68f, h * 0.38f)
            cubicTo(w * 0.78f, h * 0.50f, w * 0.80f, h * 0.70f, w * 0.75f, h * 0.75f)
            close()
        }

        drawPath(path = carBody, color = Color(0xFF2B3240))

        val windshield = Path().apply {
            moveTo(w * 0.35f, h * 0.40f)
            lineTo(w * 0.65f, h * 0.40f)
            lineTo(w * 0.60f, h * 0.52f)
            lineTo(w * 0.40f, h * 0.52f)
            close()
        }
        drawPath(path = windshield, color = Color(0xFF13171F))

        drawCircle(
            color = Color(0xFFE2F3FF),
            radius = 6.dp.toPx(),
            center = Offset(w * 0.28f, h * 0.58f)
        )
        drawCircle(
            color = Color(0xFFE2F3FF),
            radius = 6.dp.toPx(),
            center = Offset(w * 0.72f, h * 0.58f)
        )

        drawRoundRect(
            color = Color(0xFF12151B),
            topLeft = Offset(w * 0.42f, h * 0.62f),
            size = Size(w * 0.16f, h * 0.10f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
    }
}

@Composable
private fun VerticalGaugeBar(
    label: String,
    progress: Float,
    activeColor: Color,
    subLabel: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, color = HmiTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier
                .width(12.dp)
                .height(110.dp)
                .background(HmiCardBackground, RoundedCornerShape(6.dp))
                .padding(2.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .background(activeColor, RoundedCornerShape(4.dp))
            )
        }

        Text(text = subLabel, color = HmiTextMuted, fontSize = 9.sp)
    }
}

@Composable
private fun CompassRpmGraphCanvas() {
    Canvas(
        modifier = Modifier
            .width(100.dp)
            .height(26.dp)
    ) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w * 0.2f, h * 0.3f)
            lineTo(w * 0.4f, h * 0.7f)
            lineTo(w * 0.6f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.6f)
            lineTo(w, h * 0.5f)
        }

        drawPath(
            path = path,
            color = HmiMintAccent,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun SemiCircularGaugeCanvas(displayValue: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * 0.7f)
            val radius = w * 0.38f

            drawArc(
                color = HmiCardBackground,
                startAngle = 160f,
                sweepAngle = 220f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            val sweep = ((displayValue.coerceIn(0, 220)) / 220f) * 220f
            drawArc(
                color = HmiMintAccent,
                startAngle = 160f,
                sweepAngle = sweep.coerceAtLeast(1f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text(
                text = "$displayValue",
                color = HmiTextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SPEED (km/h)",
                color = HmiTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// CENTER COLUMN: SMART WIDGETS
// ==========================================
@Composable
private fun AirConditionerWidget(
    temperature: Int,
    fanSpeed: Int,
    isHvacOn: Boolean,
    onTemperatureChange: (Int) -> Unit,
    onFanSpeedChange: (Int) -> Unit,
    onToggleHvac: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(1.dp, HmiCardStroke, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = HmiPanelBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = "Air Conditioner",
                    color = HmiTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeCircleIconButton(
                        icon = Icons.Default.AcUnit,
                        isSelected = isHvacOn,
                        onClick = onToggleHvac
                    )
                    ModeCircleIconButton(
                        icon = Icons.Default.Air,
                        isSelected = fanSpeed > 0,
                        onClick = {
                            val nextFan = if (fanSpeed >= 5) 0 else fanSpeed + 1
                            onFanSpeedChange(nextFan)
                        }
                    )
                    ModeCircleIconButton(
                        icon = Icons.Default.Cloud,
                        isSelected = false,
                        onClick = {}
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fan:",
                        color = HmiTextMuted,
                        fontSize = 9.sp
                    )
                    (0..5).forEach { level ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    if (fanSpeed == level) HmiMintAccent else HmiCardBackground,
                                    CircleShape
                                )
                                .clickable { onFanSpeedChange(level) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (level == 0) "O" else "$level",
                                color = if (fanSpeed == level) HmiBackground else HmiTextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(HmiCardBackground, CircleShape)
                        .clickable { onTemperatureChange((temperature - 1).coerceAtLeast(16)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = HmiTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(HmiCardBackground, CircleShape)
                        .clickable {
                            val nextTemp = if (temperature >= 30) 16 else temperature + 1
                            onTemperatureChange(nextTemp)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    RadialTempDialCanvas(temperature = temperature, isHvacOn = isHvacOn)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isHvacOn) "$temperature°C" else "OFF",
                            color = HmiTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cooling to",
                            color = HmiTextMuted,
                            fontSize = 8.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(HmiCardBackground, CircleShape)
                        .clickable { onTemperatureChange((temperature + 1).coerceAtMost(30)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = HmiTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ModeCircleIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                if (isSelected) HmiMintAccent else HmiCardBackground,
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) HmiBackground else HmiTextPrimary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun RadialTempDialCanvas(temperature: Int, isHvacOn: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawArc(
            color = HmiCardStroke,
            startAngle = 130f,
            sweepAngle = 280f,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx())
        )

        if (isHvacOn) {
            val sweep = ((temperature - 16) / 14f) * 280f
            drawArc(
                color = HmiMintAccent,
                startAngle = 130f,
                sweepAngle = sweep.coerceIn(10f, 280f),
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

// REAL INTERACTIVE MEDIA PLAYER
@Composable
private fun MediaPlayerWidget(
    modifier: Modifier = Modifier
) {
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(true) }
    var currentPositionSec by remember { mutableIntStateOf(35) }

    val currentTrack = playlist[currentTrackIndex]
    val nextTrack = playlist[(currentTrackIndex + 1) % playlist.size]

    // Timer simulation for active audio playback progress
    LaunchedEffect(isPlaying, currentTrackIndex) {
        while (isPlaying) {
            delay(1000L)
            currentPositionSec++
            if (currentPositionSec >= currentTrack.durationSec) {
                currentPositionSec = 0
                currentTrackIndex = (currentTrackIndex + 1) % playlist.size
            }
        }
    }

    Card(
        modifier = modifier
            .border(1.dp, HmiCardStroke, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = HmiPanelBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Track Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Cover Container
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(HmiMintDark, HmiCardBackground)
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Album",
                        tint = HmiMintAccent,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        color = HmiTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentTrack.artist,
                        color = HmiTextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) HmiMintAccent else HmiTextMuted
                    )
                }
            }

            // Playback Progress Slider & Time Readout
            Column {
                Slider(
                    value = currentPositionSec.toFloat(),
                    onValueChange = { currentPositionSec = it.toInt() },
                    valueRange = 0f..currentTrack.durationSec.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = HmiMintAccent,
                        activeTrackColor = HmiMintAccent,
                        inactiveTrackColor = HmiCardBackground
                    ),
                    modifier = Modifier.height(20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPositionSec),
                        color = HmiTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatTime(currentTrack.durationSec),
                        color = HmiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentPositionSec = 0
                    currentTrackIndex = if (currentTrackIndex == 0) playlist.size - 1 else currentTrackIndex - 1
                }) {
                    Icon(Icons.Default.SkipPrevious, null, tint = HmiTextPrimary)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(HmiMintAccent, CircleShape)
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = HmiBackground
                    )
                }

                IconButton(onClick = {
                    currentPositionSec = 0
                    currentTrackIndex = (currentTrackIndex + 1) % playlist.size
                }) {
                    Icon(Icons.Default.SkipNext, null, tint = HmiTextPrimary)
                }
            }

            // Up Next Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(HmiCardBackground, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: ${nextTrack.title}",
                        color = HmiTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = HmiTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

// ==========================================
// RIGHT COLUMN: INTERACTIVE MAP & WEATHER
// ==========================================
@Composable
private fun MapNavigationWidget(modifier: Modifier = Modifier) {
    var zoomLevel by remember { mutableIntStateOf(2) } // 1..4

    Card(
        modifier = modifier.border(1.dp, HmiCardStroke, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = HmiPanelBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Interactive Map Canvas
            MapVectorCanvas(zoomLevel = zoomLevel)

            // Turn-by-Turn Guidance Overlay Box
            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd),
                colors = CardDefaults.cardColors(containerColor = HmiCardBackground.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = HmiMintAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("26 min", color = HmiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("In 200m turn right", color = HmiTextMuted, fontSize = 9.sp)
                    }
                }
            }

            // Zoom Controls & Recenter Button on Left
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(HmiCardBackground, CircleShape)
                        .clickable { zoomLevel = (zoomLevel + 1).coerceAtMost(4) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, "Zoom In", tint = HmiTextPrimary, modifier = Modifier.size(16.dp))
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(HmiCardBackground, CircleShape)
                        .clickable { zoomLevel = (zoomLevel - 1).coerceAtLeast(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, "Zoom Out", tint = HmiTextPrimary, modifier = Modifier.size(16.dp))
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(HmiCardBackground, CircleShape)
                        .clickable { zoomLevel = 2 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MyLocation, "Recenter", tint = HmiMintAccent, modifier = Modifier.size(16.dp))
                }
            }

            // Destination Badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Besant Nagar", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom Navigation Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .height(30.dp)
                    .background(HmiCardBackground.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Guindy", color = HmiTextMuted, fontSize = 10.sp)
                    Text("8.5 / 33 km (26 mins)", color = HmiMintAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MapVectorCanvas(zoomLevel: Int) {
    val scale = when (zoomLevel) {
        1 -> 0.75f
        2 -> 1.0f
        3 -> 1.3f
        else -> 1.6f
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width * scale
        val h = size.height * scale

        // Background terrain styling
        drawRect(color = Color(0xFF141922))

        // Grid map lines
        for (i in 1..8) {
            drawLine(
                color = Color(0xFF1C2330),
                start = Offset(0f, (h / 8) * i),
                end = Offset(size.width, (h / 8) * i),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Main road network
        val road1 = Path().apply {
            moveTo(0f, h * 0.35f)
            cubicTo(w * 0.3f, h * 0.2f, w * 0.5f, h * 0.65f, size.width, h * 0.45f)
        }
        drawPath(road1, color = Color(0xFF2B3445), style = Stroke(width = 12.dp.toPx()))

        val road2 = Path().apply {
            moveTo(w * 0.35f, 0f)
            lineTo(w * 0.65f, size.height)
        }
        drawPath(road2, color = Color(0xFF2B3445), style = Stroke(width = 8.dp.toPx()))

        // Active Route Polyline (Bright Blue/Teal)
        val routePath = Path().apply {
            moveTo(w * 0.1f, h * 0.45f)
            lineTo(w * 0.3f, h * 0.32f)
            lineTo(w * 0.55f, h * 0.45f)
            lineTo(w * 0.82f, h * 0.48f)
        }
        drawPath(
            path = routePath,
            color = HmiSkyBlue,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pulsing Vehicle Location Pin
        drawCircle(
            color = HmiMintAccent.copy(alpha = 0.25f),
            radius = 16.dp.toPx(),
            center = Offset(w * 0.3f, h * 0.32f)
        )
        drawCircle(
            color = HmiMintAccent,
            radius = 6.dp.toPx(),
            center = Offset(w * 0.3f, h * 0.32f)
        )
    }
}

@Composable
private fun WeatherWidget(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, HmiCardStroke, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = HmiPanelBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HmiSkyBlue, HmiBlueGradientEnd)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Guindy",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "90°",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Column {
                    Text(
                        text = "Haze 77/93",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Air quality: 121 - Moderate",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// FLOATING BOTTOM DOCK BAR
// ==========================================
@Composable
private fun HmiBottomDockBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(30.dp))
            .background(HmiPanelBackground, RoundedCornerShape(30.dp))
            .border(1.dp, HmiCardStroke, RoundedCornerShape(30.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockIconButton(icon = Icons.Default.GridView, isSelected = selectedTab == 0) { onTabSelected(0) }
            DockIconButton(icon = Icons.Default.Home, isSelected = selectedTab == 1) { onTabSelected(1) }
            DockIconButton(icon = Icons.Default.LocationOn, isSelected = selectedTab == 2) { onTabSelected(2) }
            DockIconButton(icon = Icons.Default.DirectionsCar, isSelected = selectedTab == 3) { onTabSelected(3) }
            DockIconButton(icon = Icons.Default.MusicNote, isSelected = selectedTab == 4) { onTabSelected(4) }
            DockIconButton(icon = Icons.Default.Phone, isSelected = selectedTab == 5) { onTabSelected(5) }
            DockIconButton(icon = Icons.Default.Settings, isSelected = selectedTab == 6) { onTabSelected(6) }
        }
    }
}

@Composable
private fun DockIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                if (isSelected) HmiMintAccent else Color.Transparent,
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) HmiBackground else HmiTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

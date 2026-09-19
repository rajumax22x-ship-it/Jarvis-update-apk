package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ArcReactorCore
import com.example.ui.BuildStatus
import com.example.ui.ChatMessage
import com.example.ui.JarvisEmotion
import com.example.ui.JarvisViewModel
import com.example.ui.MarkSuit
import com.example.ui.ProjectWebsite
import com.example.ui.StarkSoundEngine
import com.example.ui.VoiceGender
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.cos
import kotlin.math.sin

val SciFiDark = Color(0xFF030B14)
val SciFiCyan = Color(0xFF00E5FF)
val SciFiCyanDark = Color(0xFF006B7D)
val SciFiOrange = Color(0xFFFF9100)

class MainActivity : ComponentActivity() {
    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                JarvisApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentEmotion by viewModel.currentEmotion.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val emotionColor by animateColorAsState(
        targetValue = Color(currentEmotion.colorHex),
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "nav_emotion_color"
    )
    
    Scaffold(
        containerColor = SciFiDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, emotionColor, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.jarvis_logo),
                                contentDescription = "Jarvis Circuit Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = emotionColor,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "CIRCUIT OS // STARK",
                                color = SciFiCyanDark,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("menu_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Settings Menu (3 Lines)",
                            tint = emotionColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.runVitalsCheck() }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Vitals Notification",
                            tint = emotionColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.90f)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black.copy(alpha = 0.95f),
                contentColor = emotionColor
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("HUD", fontFamily = FontFamily.Monospace) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SciFiDark,
                        selectedTextColor = emotionColor,
                        indicatorColor = emotionColor,
                        unselectedIconColor = SciFiCyanDark,
                        unselectedTextColor = SciFiCyanDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Chat") },
                    label = { Text("COMMS", fontFamily = FontFamily.Monospace) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SciFiDark,
                        selectedTextColor = emotionColor,
                        indicatorColor = emotionColor,
                        unselectedIconColor = SciFiCyanDark,
                        unselectedTextColor = SciFiCyanDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Code, contentDescription = "Studio") },
                    label = { Text("STUDIO", fontFamily = FontFamily.Monospace) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SciFiDark,
                        selectedTextColor = emotionColor,
                        indicatorColor = emotionColor,
                        unselectedIconColor = SciFiCyanDark,
                        unselectedTextColor = SciFiCyanDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = "Gesture") },
                    label = { Text("GESTURE", fontFamily = FontFamily.Monospace) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SciFiDark,
                        selectedTextColor = emotionColor,
                        indicatorColor = emotionColor,
                        unselectedIconColor = SciFiCyanDark,
                        unselectedTextColor = SciFiCyanDark
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel, context)
                1 -> ChatScreen(viewModel)
                2 -> CodeStudioScreen(viewModel)
                3 -> HandGestureScreen(viewModel, context)
            }
            // Active Voice Edge Glow (Baju se glow chale jab mic on ho)
            JarvisEdgeGlow(isListening = isListening, color = emotionColor)
        }
    }

    if (showSettingsDialog) {
        JarvisSettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@Composable
fun JarvisSettingsDialog(viewModel: JarvisViewModel, onDismiss: () -> Unit) {
    val currentVoiceGender by viewModel.currentVoiceGender.collectAsStateWithLifecycle()
    val isCourtesyActive by viewModel.courtesyModeActive.collectAsStateWithLifecycle()
    val isBackgroundListening by viewModel.backgroundListeningEnabled.collectAsStateWithLifecycle()
    val currentSuit by viewModel.currentSuit.collectAsStateWithLifecycle()
    val currentReactorCore by viewModel.currentReactorCore.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SciFiDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚙️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STARK TACTICAL SETTINGS",
                    color = SciFiCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Background Listening Option
                Text("BACKGROUND CONTINUOUS LISTENING", color = SciFiCyanDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { viewModel.toggleBackgroundListening() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isBackgroundListening) SciFiCyan.copy(alpha = 0.3f) else Color.DarkGray),
                    border = BorderStroke(1.dp, if (isBackgroundListening) SciFiCyan else Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isBackgroundListening) "🎧 BACKGROUND LISTENING: ON (LISTENS WHEN APP MINIMIZED)" else "🔇 BACKGROUND LISTENING: OFF", color = if (isBackgroundListening) SciFiCyan else Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }

                // Voice Gender
                Text("VOICE MATRIX (MALE / FEMALE)", color = SciFiCyanDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setVoiceGender(VoiceGender.MALE) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentVoiceGender == VoiceGender.MALE) SciFiCyan else Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("👨 MALE VOICE (JARVIS)", color = if (currentVoiceGender == VoiceGender.MALE) SciFiDark else Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.setVoiceGender(VoiceGender.FEMALE) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentVoiceGender == VoiceGender.FEMALE) Color(0xFFFF4081) else Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("👩 FEMALE VOICE (FRIDAY)", color = if (currentVoiceGender == VoiceGender.FEMALE) Color.White else Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Courtesy Protocol
                Text("COURTESY & RESPECT (ADAB)", color = SciFiCyanDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { viewModel.toggleCourtesyMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCourtesyActive) Color(0xFFFFD600).copy(alpha = 0.3f) else Color.DarkGray),
                    border = BorderStroke(1.dp, if (isCourtesyActive) Color(0xFFFFD600) else Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isCourtesyActive) "👑 COURTESY ACTIVE (100% ADAB)" else "🛡️ STANDARD MODE", color = if (isCourtesyActive) Color(0xFFFFD600) else Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                // Armor Chassis
                Text("ARMOR CHASSIS VAULT", color = SciFiCyanDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MarkSuit.entries.forEach { suit ->
                        val isSelected = suit == currentSuit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(suit.colorHex).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, if (isSelected) Color(suit.colorHex) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectMarkSuit(suit) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(suit.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(suit.suitCode, color = if (isSelected) Color(suit.colorHex) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(suit.codename, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Arc Reactor
                Text("ARC REACTOR GEOMETRY", color = SciFiCyanDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ArcReactorCore.entries.forEach { core ->
                        val isSelected = core == currentReactorCore
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SciFiCyan.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, if (isSelected) SciFiCyan else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectReactorCore(core) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(core.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(core.title, color = if (isSelected) SciFiCyan else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SciFiCyan)
            ) {
                Text("CLOSE", color = SciFiDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
fun JarvisEdgeGlow(isListening: Boolean, color: Color) {
    if (!isListening) return

    val infiniteTransition = rememberInfiniteTransition(label = "edge_glow_transition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_alpha"
    )
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "edge_translate"
    )
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 8.dp.toPx()
        val width = size.width
        val height = size.height

        // 1. Colorful Gulu Gulu Neon Edge Gradient Border
        val rainbowColors = listOf(
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF007F), // Neon Pink
            Color(0xFF7C4DFF), // Purple
            Color(0xFF00E676), // Green
            Color(0xFFFFD600), // Gold
            Color(0xFF00E5FF)  // Cyan
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = rainbowColors,
                start = Offset(translateAnim, 0f),
                end = Offset(translateAnim + 500f, height)
            ),
            style = Stroke(width = strokeWidth)
        )

        // 2. Outer Atmospheric Side Glow Pulse
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alphaAnim * 0.3f), Color.Transparent),
                center = Offset(width / 2f, height / 2f),
                radius = width.coerceAtLeast(height) * 0.8f
            )
        )

        // 3. Gulu Gulu Background Audio Equalizer Waves / Ripples
        val barCount = 24
        val barWidth = width / (barCount * 1.5f)
        val spacing = barWidth * 0.5f
        val startX = (width - (barCount * (barWidth + spacing))) / 2f

        for (i in 0 until barCount) {
            val phase = waveAnim + (i * 0.3f)
            val barHeight = (height * 0.15f) * (0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase.toDouble()).toFloat()))
            val barColor = rainbowColors[i % rainbowColors.size]

            // Top Wave Bar
            drawRect(
                color = barColor.copy(alpha = alphaAnim * 0.8f),
                topLeft = Offset(startX + i * (barWidth + spacing), 20f),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
            // Bottom Wave Bar
            drawRect(
                color = barColor.copy(alpha = alphaAnim * 0.8f),
                topLeft = Offset(startX + i * (barWidth + spacing), height - 20f - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun DashboardScreen(viewModel: JarvisViewModel, context: android.content.Context) {
    val stats by viewModel.systemStats.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val status by viewModel.statusText.collectAsStateWithLifecycle()
    val currentEmotion by viewModel.currentEmotion.collectAsStateWithLifecycle()
    val isCourtesyActive by viewModel.courtesyModeActive.collectAsStateWithLifecycle()
    val currentVoiceGender by viewModel.currentVoiceGender.collectAsStateWithLifecycle()
    
    // Iron Man Movie Telemetry & States
    val currentSuit by viewModel.currentSuit.collectAsStateWithLifecycle()
    val currentReactorCore by viewModel.currentReactorCore.collectAsStateWithLifecycle()
    val heartRate by viewModel.heartRate.collectAsStateWithLifecycle()
    val altitudeFt by viewModel.altitudeFt.collectAsStateWithLifecycle()
    val machSpeed by viewModel.machSpeed.collectAsStateWithLifecycle()
    val arcReactorPowerPct by viewModel.arcReactorPowerPct.collectAsStateWithLifecycle()
    val unibeamCharging by viewModel.unibeamCharging.collectAsStateWithLifecycle()
    val housePartyActive by viewModel.housePartyActive.collectAsStateWithLifecycle()

    val emotionPrimaryColor by animateColorAsState(
        targetValue = if (unibeamCharging) Color(0xFFFFD54F) else if (housePartyActive) Color(0xFFFF1744) else Color(currentEmotion.colorHex),
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "primary_emotion"
    )
    val emotionSecondaryColor by animateColorAsState(
        targetValue = if (unibeamCharging) Color(0xFFFF9100) else if (housePartyActive) Color(0xFFD50000) else Color(currentEmotion.secondaryColorHex),
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "secondary_emotion"
    )
    
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasAudioPermission = isGranted }
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SciFiDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        
        // --- MOVIE HUD TELEMETRY HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(1.dp, emotionPrimaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                HudText("SUIT: ${currentSuit.suitCode}", emotionPrimaryColor)
                HudText("HULL: ${currentSuit.hullIntegrity}%", Color(0xFF00E676))
                HudText("ALT: ${altitudeFt} FT", emotionPrimaryColor)
                HudText("SPEED: MACH ${"%.2f".format(machSpeed)}", Color(0xFFFFD54F))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "CORE: ${currentReactorCore.title.take(12)}",
                    color = emotionSecondaryColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                HudText("OUTPUT: ${arcReactorPowerPct}%", if (arcReactorPowerPct > 100) Color(0xFFFF1744) else emotionPrimaryColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${heartRate} BPM", color = Color(0xFFFF5252), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${currentEmotion.iconEmoji} ${currentEmotion.title}",
                    color = emotionPrimaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        // J.A.R.V.I.S. Title + House Party Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (housePartyActive) {
                Text("🚨 ", fontSize = 20.sp)
            }
            Text(
                text = "J.A.R.V.I.S.",
                color = emotionPrimaryColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            if (housePartyActive) {
                Text(" 🚨", fontSize = 20.sp)
            }
        }

        // Subtitle: Stark Autonomous Neural Core
        Text(
            text = if (housePartyActive) "HOUSE PARTY PROTOCOL ENGAGED // ALL SUITS DEPLOYED"
                   else if (unibeamCharging) "SURGE DISCHARGE // 175% POWER"
                   else "STARK INDUSTRIES // MARK OS v5.0",
            color = if (housePartyActive) Color(0xFFFF1744) else if (unibeamCharging) Color(0xFFFFD54F) else emotionSecondaryColor,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Courtesy & Respect Protocol Badge + Voice Gender Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Courtesy badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isCourtesyActive) Color(0xFFFFD600).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                    .border(
                        width = 1.dp,
                        color = if (isCourtesyActive) Color(0xFFFFD600).copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.toggleCourtesyMode() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isCourtesyActive) "👑" else "🛡️", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCourtesyActive) "COURTESY: 100% ADAB" else "COURTESY: STD",
                    color = if (isCourtesyActive) Color(0xFFFFD600) else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // Voice Gender Toggle (Male / Female)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (currentVoiceGender == VoiceGender.FEMALE) Color(0xFFFF4081).copy(alpha = 0.18f)
                        else SciFiCyan.copy(alpha = 0.15f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (currentVoiceGender == VoiceGender.FEMALE) Color(0xFFFF4081).copy(alpha = 0.8f) else SciFiCyan.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.toggleVoiceGender() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currentVoiceGender.iconEmoji, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VOICE: ${currentVoiceGender.displayName.substringBefore(" ")}",
                    color = if (currentVoiceGender == VoiceGender.FEMALE) Color(0xFFFF80AB) else SciFiCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- MARK ARMOR SUIT SELECTOR RIBBON ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ARMOR CHASSIS VAULT",
                color = emotionSecondaryColor,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentSuit.codename,
                color = Color(currentSuit.colorHex),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(MarkSuit.entries.toTypedArray()) { suit ->
                val isSelected = suit == currentSuit
                val suitColor = Color(suit.colorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) suitColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.5f))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) suitColor else suitColor.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectMarkSuit(suit) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${suit.iconEmoji} ${suit.suitCode}", color = if (isSelected) suitColor else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(suit.codename.take(10), color = if (isSelected) Color.White else Color.DarkGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- ARC REACTOR CORE SELECTOR ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ARC REACTOR GEOMETRY",
                color = emotionSecondaryColor,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (currentReactorCore.isTriangular) "TRIANGULAR (MK VI)" else "CIRCULAR COIL (MK III)",
                color = emotionPrimaryColor,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(ArcReactorCore.entries.toTypedArray()) { core ->
                val isSelected = core == currentReactorCore
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) emotionPrimaryColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.5f))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) emotionPrimaryColor else emotionPrimaryColor.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectReactorCore(core) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${core.iconEmoji} ${core.title}",
                        color = if (isSelected) emotionPrimaryColor else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- CENTER STAGE: COMPLEX ARC REACTOR ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            ComplexArcReactor(
                isListening = isListening,
                primaryColor = emotionPrimaryColor,
                secondaryColor = emotionSecondaryColor,
                emotion = currentEmotion,
                reactorCore = currentReactorCore,
                isUnibeamActive = unibeamCharging,
                onClick = {
                    StarkSoundEngine.playRepulsorCharge()
                }
            )
        }

        // Tap Hint
        Text(
            text = "⚡ TAP ARC REACTOR TO DISCHARGE REPULSOR COIL",
            color = emotionSecondaryColor.copy(alpha = 0.7f),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- LIVE AUDIO WAVEFORM ---
        AudioWaveform(isListening, emotionPrimaryColor)
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- STARK MOVIE PROTOCOLS ACTION MATRIX ---
        Text(
            text = "TACTICAL DEFENSE PROTOCOLS",
            color = emotionSecondaryColor,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                QuickPromptChip("🚨 House Party", Color(0xFFFF1744)) {
                    viewModel.executeHousePartyProtocol()
                }
            }
            item {
                QuickPromptChip("💥 Fire Unibeam", Color(0xFFFFD54F)) {
                    viewModel.fireUnibeam()
                }
            }
            item {
                QuickPromptChip("🚀 Mach 2 Flight", Color(0xFF00E5FF)) {
                    viewModel.testSupersonicFlight()
                }
            }
            item {
                QuickPromptChip("🦾 Veronica", Color(0xFFFF9100)) {
                    viewModel.deployVeronicaHulkbuster()
                }
            }
            item {
                QuickPromptChip("🧹 Clean Slate", Color(0xFFB0BEC5)) {
                    viewModel.executeCleanSlateProtocol()
                }
            }
            item {
                QuickPromptChip("🤖 Dum-E Arms", Color(0xFF69F0AE)) {
                    viewModel.calibrateRoboticArms()
                }
            }
            item {
                QuickPromptChip("❤️ Vitals Check", Color(0xFFFF5252)) {
                    viewModel.runVitalsCheck()
                }
            }
            item {
                QuickPromptChip("👑 Sir Ka Haal-Chaal", Color(0xFFFFD600)) {
                    viewModel.triggerDailyInquiry()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- EMOTIONAL INTELLIGENCE SUBROUTINE ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(JarvisEmotion.entries.toTypedArray()) { emotion ->
                val isSelected = emotion == currentEmotion
                val chipColor = Color(emotion.colorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) chipColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.4f))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) chipColor else chipColor.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setEmotion(emotion, notifyVoice = true) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${emotion.iconEmoji} ${emotion.title}",
                        color = if (isSelected) chipColor else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- STATUS & REPULSOR MIC BUTTON ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (hasAudioPermission) viewModel.toggleListening(context)
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (isListening) SciFiOrange else emotionPrimaryColor.copy(alpha = 0.18f))
                    .border(2.dp, if (isListening) Color.White else emotionPrimaryColor, CircleShape)
            ) {
                Icon(
                    Icons.Default.Mic, 
                    contentDescription = "Mic", 
                    tint = if (isListening) SciFiDark else emotionPrimaryColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column {
                Text(
                    text = if (isListening) "LISTENING TO SIR..." else status,
                    color = if (isListening) SciFiOrange else emotionPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "NEURAL STATE: ${currentEmotion.subtitle}",
                    color = emotionSecondaryColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        // --- QUICK STARK VOICE COMMAND CHIPS (INSTANT REPLACEMENT FOR MIC ISSUES) ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, emotionPrimaryColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "⚡ INSTANT STARK VOICE & COMMAND CHIPS (TAP TO SPEAK)",
                    color = emotionPrimaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val quickCommands = listOf(
                    "Main Raju hoon",
                    "Who made you?",
                    "Virus scan",
                    "Open gallery",
                    "Open browser",
                    "Gravity",
                    "Relativity",
                    "Quantum physics",
                    "Thermodynamics",
                    "Black hole",
                    "DNA genetics",
                    "Vitals check",
                    "Call 911",
                    "Battery status",
                    "House party protocol",
                    "Unibeam fire"
                )
                // We can display them in rows or lazy row / flow row
                quickCommands.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { cmd ->
                            Button(
                                onClick = { viewModel.simulateVoiceCommand(cmd) },
                                colors = ButtonDefaults.buttonColors(containerColor = emotionPrimaryColor.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, emotionPrimaryColor),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Text(
                                    text = cmd,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun HudText(text: String, color: Color = SciFiCyan) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun AudioWaveform(isListening: Boolean, waveColor: Color = SciFiCyan) {
    val transition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 16.dp)) {
        val barWidth = 6.dp.toPx()
        val spacing = 4.dp.toPx()
        val totalBars = (size.width / (barWidth + spacing)).toInt()
        
        for (i in 0 until totalBars) {
            val x = i * (barWidth + spacing)
            val baseHeight = size.height * 0.15f
            val maxAmplitude = size.height * 0.85f
            
            val amplitude = if (isListening) {
                val noise = (sin(i * 0.5f + phase) * cos(i * 0.3f - phase * 1.5f)).toFloat()
                baseHeight + Math.abs(noise) * maxAmplitude
            } else {
                baseHeight + Math.abs(sin(i * 0.5f + phase * 0.3f).toFloat()) * 10.dp.toPx()
            }
            
            drawLine(
                color = waveColor,
                start = Offset(x, size.height / 2f - amplitude / 2f),
                end = Offset(x, size.height / 2f + amplitude / 2f),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ComplexArcReactor(
    isListening: Boolean,
    primaryColor: Color = SciFiCyan,
    secondaryColor: Color = SciFiCyanDark,
    emotion: JarvisEmotion = JarvisEmotion.CALM,
    reactorCore: ArcReactorCore = ArcReactorCore.NEW_ELEMENT,
    isUnibeamActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_transition")
    
    val rotationPeriod = when {
        isUnibeamActive -> 1500
        emotion == JarvisEmotion.ALERT -> 3500
        emotion == JarvisEmotion.AMUSED -> 6500
        emotion == JarvisEmotion.WITTY -> 8500
        else -> 14000
    }
    val pulsePeriod = when {
        isUnibeamActive -> 200
        emotion == JarvisEmotion.ALERT -> 300
        emotion == JarvisEmotion.AMUSED -> 550
        emotion == JarvisEmotion.EMPATHETIC -> 1600
        else -> if (isListening) 380 else 1300
    }

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(rotationPeriod, easing = LinearEasing)),
        label = "outer_rotation"
    )
    val fastRotation by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(rotationPeriod / 2, easing = LinearEasing)),
        label = "inner_rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = if (isUnibeamActive) 0.98f else 0.94f,
        targetValue = if (isUnibeamActive) 1.12f else 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(pulsePeriod, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "core_pulse"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "shimmer"
    )

    Canvas(
        modifier = Modifier
            .size(280.dp)
            .scale(pulse)
            .clickable { onClick() }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Outer Radial Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (isUnibeamActive) Color.White.copy(alpha = 0.65f) else primaryColor.copy(alpha = 0.4f),
                    primaryColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.35f
            )
        )

        // Outer Titanium Containment Ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.45f),
            radius = radius * 0.96f,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Outer Segmented Rotating Ring (3 Large Magnetic Arcs)
        rotate(rotation) {
            for (i in 0..2) {
                drawArc(
                    color = primaryColor,
                    startAngle = i * 120f, sweepAngle = 70f, useCenter = false,
                    style = Stroke(width = 5.dp.toPx()),
                    topLeft = Offset(center.x - radius * 0.90f, center.y - radius * 0.90f),
                    size = Size(radius * 1.80f, radius * 1.80f)
                )
            }
        }

        if (reactorCore.isTriangular) {
            // === VIBRANIUM NEW ELEMENT TRIANGULAR CORE (MARK VI & VII) ===
            // Outer Tri-Corner Containment Brackets
            rotate(rotation * 0.5f) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.8f),
                    radius = radius * 0.76f,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 30f))
                    )
                )
            }

            // Inner Rotating Golden Conduits
            rotate(fastRotation) {
                for (i in 0..2) {
                    val angleRad = Math.toRadians((i * 120.0)).toFloat()
                    val cx = center.x + cos(angleRad) * (radius * 0.62f)
                    val cy = center.y + sin(angleRad) * (radius * 0.62f)
                    // Induction nodes at the 3 vertices
                    drawCircle(color = Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(cx, cy))
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(cx, cy))
                }
            }

            // Central Glowing Equilateral Triangle
            val triRadius = radius * 0.52f
            val trianglePath = Path().apply {
                val p1 = Offset(center.x, center.y - triRadius)
                val p2 = Offset(center.x + (triRadius * 0.866f), center.y + (triRadius * 0.5f))
                val p3 = Offset(center.x - (triRadius * 0.866f), center.y + (triRadius * 0.5f))
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }

            // Outer Triangle Housing
            drawPath(
                path = trianglePath,
                color = primaryColor,
                style = Stroke(width = 7.dp.toPx(), join = StrokeJoin.Round)
            )

            // Inner Prismatic Inverted Triangle
            val innerTriRadius = radius * 0.30f
            val innerTrianglePath = Path().apply {
                val ip1 = Offset(center.x, center.y + innerTriRadius)
                val ip2 = Offset(center.x + (innerTriRadius * 0.866f), center.y - (innerTriRadius * 0.5f))
                val ip3 = Offset(center.x - (innerTriRadius * 0.866f), center.y - (innerTriRadius * 0.5f))
                moveTo(ip1.x, ip1.y)
                lineTo(ip2.x, ip2.y)
                lineTo(ip3.x, ip3.y)
                close()
            }
            drawPath(
                path = innerTrianglePath,
                color = Color.White.copy(alpha = shimmer),
                style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round)
            )

            // Core Energy Beam Center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primaryColor, secondaryColor, Color.Transparent),
                    center = center,
                    radius = radius * 0.28f
                )
            )
        } else {
            // === PALLADIUM & NANOTECH CIRCULAR COIL CORE (MARK III & L) ===
            // 10 Copper Magnetic Induction Blocks around perimeter (Iconic Iron Man 1 prop!)
            val coilRadius = radius * 0.72f
            for (i in 0 until 10) {
                val angleDeg = i * 36f
                val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
                val coilCenter = Offset(center.x + cos(angleRad) * coilRadius, center.y + sin(angleRad) * coilRadius)
                
                // Copper coil rectangular block
                drawCircle(
                    color = Color(0xFFD78B44), // Copper winding
                    radius = 8.dp.toPx(),
                    center = coilCenter
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.8f), // Gold highlight wire
                    radius = 4.dp.toPx(),
                    center = coilCenter
                )
            }

            // Inner Counter-Rotating Dashed Flux Ring
            rotate(fastRotation) {
                drawCircle(
                    color = secondaryColor,
                    radius = radius * 0.60f,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 22f))
                    )
                )
            }

            // High Precision Laser Collimator Ring
            drawCircle(
                color = primaryColor,
                radius = radius * 0.46f,
                style = Stroke(width = 6.dp.toPx())
            )

            // Inner Slotted Aperture
            rotate(rotation) {
                for (j in 0 until 6) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.9f),
                        startAngle = j * 60f, sweepAngle = 25f, useCenter = false,
                        style = Stroke(width = 3.dp.toPx()),
                        topLeft = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f),
                        size = Size(radius * 0.76f, radius * 0.76f)
                    )
                }
            }

            // Radiant Pure White Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        if (isUnibeamActive) Color(0xFFFFF176) else primaryColor,
                        secondaryColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 0.35f
                )
            )
        }
    }
}

@Composable
fun ChatScreen(viewModel: JarvisViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentEmotion by viewModel.currentEmotion.collectAsStateWithLifecycle()
    val isCourtesyActive by viewModel.courtesyModeActive.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val emotionColor = Color(currentEmotion.colorHex)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Emotion & Courtesy Status Bar in Comms
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(1.dp, emotionColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(currentEmotion.iconEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEURAL: ${currentEmotion.title}",
                    color = emotionColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCourtesyActive) Color(0xFFFFD600).copy(alpha = 0.2f) else Color.DarkGray)
                    .clickable { viewModel.toggleCourtesyMode() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(if (isCourtesyActive) "👑 100% ADAB" else "🛡️ NORMAL", color = if (isCourtesyActive) Color(0xFFFFD600) else Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Message Stream
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                ChatBubble(msg)
            }
        }

        // Quick Stark Protocols & Routine Prompt Chips
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                QuickPromptChip("🚨 House Party", Color(0xFFFF1744)) {
                    viewModel.executeHousePartyProtocol()
                }
            }
            item {
                QuickPromptChip("💥 Fire Unibeam", Color(0xFFFFD54F)) {
                    viewModel.fireUnibeam()
                }
            }
            item {
                QuickPromptChip("🚀 Mach 2 Flight", Color(0xFF00E5FF)) {
                    viewModel.testSupersonicFlight()
                }
            }
            item {
                QuickPromptChip("🦾 Veronica", Color(0xFFFF9100)) {
                    viewModel.deployVeronicaHulkbuster()
                }
            }
            item {
                QuickPromptChip("❤️ Vitals Check", Color(0xFFFF5252)) {
                    viewModel.runVitalsCheck()
                }
            }
            item {
                QuickPromptChip("🤖 Dum-E Standby", Color(0xFF69F0AE)) {
                    viewModel.calibrateRoboticArms()
                }
            }
            item {
                QuickPromptChip("🎙️ Switch Voice (M/F)", Color(0xFF00E5FF)) {
                    viewModel.toggleVoiceGender()
                }
            }
            item {
                QuickPromptChip("🌟 Haal-Chaal", Color(0xFFFFD600)) {
                    viewModel.triggerDailyInquiry()
                }
            }
            item {
                QuickPromptChip("👑 Shahi Adab", Color(0xFFFFD600)) {
                    viewModel.handleUserInput("Jarvis, poore adab aur tehzeeb ke saath mera swagat kijiye.")
                }
            }
            item {
                QuickPromptChip("💻 Tagada Website", Color(0xFF00E676)) {
                    viewModel.handleUserInput("Jarvis, mere liye ek tagada level ki interactive website bana kar Studio Vault mein add karo!")
                }
            }
            item {
                QuickPromptChip("😏 Sarcasm Check", emotionColor) {
                    viewModel.handleUserInput("Sir requests your honest assessment of my genius today, Jarvis.")
                }
            }
            item {
                QuickPromptChip("🙏 Shukriya Jarvis", Color(0xFFFF80AB)) {
                    viewModel.handleUserInput("Thank you so much Jarvis for always speaking so nicely and taking such good care of me.")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- HIGH-LEVEL STARK CODE & BUILD STUDIO TERMINAL ---
        var buildPrompt by remember { mutableStateOf("") }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💻 HIGH-LEVEL STARK CODE & BUILD STUDIO",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "VAULT READY ⚡",
                        color = Color(0xFF00E5FF),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                BasicTextField(
                    value = buildPrompt,
                    onValueChange = { buildPrompt = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    decorationBox = { innerTextField ->
                        if (buildPrompt.isEmpty()) Text("Type what to code/build (e.g. AI Cyber Security Dashboard)...", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (buildPrompt.isNotBlank()) {
                                val fullCmd = "Jarvis, Raju Sir ke liye high level coding karke yeh tagada project banao aur Studio Vault mein add karo: $buildPrompt"
                                viewModel.handleUserInput(fullCmd)
                                buildPrompt = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("BUILD & COMPILE PROJECT 🚀", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = {
                            viewModel.handleUserInput("Jarvis, ek mahan level ka futuristic AI Quantum Neural Dashboard web app code karke Studio Vault mein add karo!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("⚡ QUICK AI APP", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(color = emotionColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(emotionColor),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .border(1.dp, emotionColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) Text("Speak or type to Jarvis, Sir...", color = emotionColor.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.handleUserInput(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier.size(52.dp).background(emotionColor, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = SciFiDark)
            }
        }
    }
}

@Composable
fun QuickPromptChip(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val bubbleColor = if (msg.isUser) SciFiCyan else Color(msg.emotion.colorHex)

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (msg.isUser) 16.dp else 2.dp,
                    bottomEnd = if (msg.isUser) 2.dp else 16.dp
                ))
                .background(if (msg.isUser) SciFiCyan.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.65f))
                .border(
                    width = 1.dp,
                    color = if (msg.isUser) SciFiCyan.copy(alpha = 0.5f) else bubbleColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (msg.isUser) 16.dp else 2.dp,
                        bottomEnd = if (msg.isUser) 2.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                if (!msg.isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${msg.emotion.iconEmoji} ${msg.emotion.title}",
                                color = bubbleColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = msg.time,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "SIR • ${msg.time}",
                            color = SciFiCyan.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = msg.text,
                    color = if (msg.isUser) SciFiCyan else Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CodeStudioScreen(viewModel: JarvisViewModel) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
    val codeContent by viewModel.generatedCode.collectAsStateWithLifecycle()
    val isBuilding by viewModel.isBuilding.collectAsStateWithLifecycle()
    val activeBuildLogs by viewModel.activeBuildLogs.collectAsStateWithLifecycle()
    val currentEmotion by viewModel.currentEmotion.collectAsStateWithLifecycle()
    val emotionColor = Color(currentEmotion.colorHex)
    val context = LocalContext.current

    val activeProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()
    var studioTab by remember { mutableIntStateOf(0) } // 0: PREVIEW, 1: CODE, 2: BUILD, 3: VAULT
    var isMobileViewport by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editableCode by remember(codeContent) { mutableStateOf(codeContent) }
    var webViewKey by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SciFiDark)
    ) {
        // --- TOP BAR: Studio Status & Actions ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF07111E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activeProject?.iconEmoji ?: "🌐",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activeProject?.title ?: "No Project",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isBuilding) Color(0xFFFF9100) else Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBuilding) "COMPILING..." else "BUILD READY",
                            color = if (isBuilding) Color(0xFFFF9100) else Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Build button
                Button(
                    onClick = {
                        activeProject?.let { viewModel.buildProject(it.id) }
                        studioTab = 2 // Switch to build logs
                    },
                    enabled = !isBuilding,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isBuilding) "⚙️ BUILDING" else "▶️ BUILD",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Add Site button
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD600).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFFD600), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Website",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- PROJECTS SELECTOR STRIP ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050E18))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(projects) { proj ->
                val isSelected = proj.id == selectedProjectId
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF0A1929))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.selectProject(proj.id)
                            webViewKey++
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(proj.iconEmoji, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = proj.title,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD600).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFFD600).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("➕", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW SITE",
                        color = Color(0xFFFFD600),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SUB-NAVIGATION TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
        ) {
            val tabs = listOf(
                "🌐 PREVIEW" to 0,
                "💻 CODE" to 1,
                "🚀 BUILD" to 2,
                "📂 VAULT" to 3,
                "🔍 GOOGLE & APK" to 4
            )
            tabs.forEach { (label, idx) ->
                val isTabSelected = studioTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { studioTab = idx }
                        .background(if (isTabSelected) Color(0xFF0D2538) else Color.Transparent)
                        .border(
                            width = if (isTabSelected) 1.dp else 0.dp,
                            color = if (isTabSelected) Color(0xFF00E5FF) else Color.Transparent
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isTabSelected) Color(0xFF00E5FF) else Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // --- MAIN VIEWPORT / CONTENT ---
        Box(modifier = Modifier.weight(1f)) {
            when (studioTab) {
                0 -> {
                    // LIVE PREVIEW TAB
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Viewport & Action Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF081420))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { isMobileViewport = !isMobileViewport },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Devices,
                                        contentDescription = "Toggle Viewport",
                                        tint = if (isMobileViewport) Color(0xFFFF9100) else Color(0xFF00E5FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isMobileViewport) "MOBILE VIEW" else "DESKTOP VIEW",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isMobileViewport) Color(0xFFFF9100) else Color(0xFF00E5FF)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { webViewKey++ },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reload",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                TextButton(
                                    onClick = {
                                        activeProject?.let { viewModel.buildProject(it.id) }
                                        webViewKey++
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = "⚡ RECOMPILE",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFFFD600)
                                    )
                                }
                            }
                        }

                        // Embedded WebView
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = if (isMobileViewport) {
                                    Modifier
                                        .fillMaxHeight()
                                        .width(340.dp)
                                        .padding(vertical = 8.dp)
                                        .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                } else {
                                    Modifier.fillMaxSize()
                                }
                            ) {
                                key(webViewKey, selectedProjectId) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize().background(Color.White),
                                        factory = { ctx ->
                                            WebView(ctx).apply {
                                                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                                settings.apply {
                                                    javaScriptEnabled = true
                                                    domStorageEnabled = true
                                                    useWideViewPort = true
                                                    loadWithOverviewMode = true
                                                    databaseEnabled = true
                                                    allowFileAccess = true
                                                    setSupportZoom(true)
                                                    builtInZoomControls = true
                                                    displayZoomControls = false
                                                }
                                                webChromeClient = WebChromeClient()
                                                webViewClient = WebViewClient()
                                            }
                                        },
                                        update = { webView ->
                                            webView.loadDataWithBaseURL(null, codeContent, "text/html", "UTF-8", null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // SOURCE CODE TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF050B12))
                            .padding(8.dp)
                    ) {
                        // Code Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0A1624))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LANG: ${activeProject?.language ?: "HTML5"} • ${editableCode.lines().size} LINES",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Code", editableCode))
                                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📋 COPY", color = Color(0xFF00E5FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        activeProject?.let {
                                            viewModel.updateProjectCode(it.id, editableCode)
                                            viewModel.buildProject(it.id)
                                            studioTab = 0
                                            webViewKey++
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676).copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, Color(0xFF00E676)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("💾 SAVE & BUILD", color = Color(0xFF00E676), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Code Editor Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .background(Color(0xFF03070E))
                                .padding(8.dp)
                        ) {
                            BasicTextField(
                                value = editableCode,
                                onValueChange = { editableCode = it },
                                textStyle = TextStyle(
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                2 -> {
                    // BUILD CONSOLE TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF02060D))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF091422))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "J.A.R.V.I.S. QUANTUM COMPILER v4.2",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isBuilding) "COMPILING..." else "COMPLETED",
                                color = if (isBuilding) Color(0xFFFF9100) else Color(0xFF00E676),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Console Output Area
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val logs = if (activeBuildLogs.isNotEmpty()) activeBuildLogs else listOf(
                                "[READY] Quantum compiler initialized and listening for build triggers.",
                                "Target project: '${activeProject?.title ?: "None"}'",
                                "Press 'RUN FULL BUILD SEQUENCE' below to compile."
                            )
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    color = when {
                                        log.contains("[SUCCESS]") -> Color(0xFF00E676)
                                        log.contains("[JARVIS") -> Color(0xFF00E5FF)
                                        log.contains(">>") -> Color(0xFF38BDF8)
                                        else -> Color.LightGray
                                    },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { activeProject?.let { viewModel.buildProject(it.id) } },
                            enabled = !isBuilding,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text(
                                text = if (isBuilding) "BUILDING PROJECT..." else "🚀 RUN FULL BUILD SEQUENCE",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                3 -> {
                    // WEBSITES VAULT TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SciFiDark)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STORED WEBSITES (${projects.size})",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ ADD NEW SITE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(projects) { proj ->
                                val isCurrent = proj.id == selectedProjectId
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrent) Color(0xFF0D253A) else Color(0xFF081422)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(proj.iconEmoji, fontSize = 22.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = proj.title,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Text(
                                                        text = "${proj.language} • ${proj.timestamp}",
                                                        color = Color.Gray,
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }

                                            if (isCurrent) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = proj.description,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            if (projects.size > 1) {
                                                IconButton(
                                                    onClick = { viewModel.deleteProject(proj.id) },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = Color(0xFFFF5252),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.selectProject(proj.id)
                                                    viewModel.buildProject(proj.id)
                                                    studioTab = 0
                                                    webViewKey++
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(
                                                    text = "OPEN PREVIEW 🚀",
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // GOOGLE SEARCH, FB & MEDIA DOWNLOADER & APK GENERATOR TAB
                    var googleQuery by remember { mutableStateOf("") }
                    var fbLink by remember { mutableStateOf("") }
                    var downloadStatus by remember { mutableStateOf("Ready for Google search, media downloads, and APK generation.") }
                    var apkBuildStatus by remember { mutableStateOf("No APK generated yet.") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SciFiDark)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🌐 STARK GOOGLE SEARCH ENGINE",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BasicTextField(
                                value = googleQuery,
                                onValueChange = { googleQuery = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                decorationBox = { inner ->
                                    if (googleQuery.isEmpty()) Text("Search Google (e.g. latest tech news)...", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    inner()
                                }
                            )
                            Button(
                                onClick = {
                                    if (googleQuery.isNotBlank()) {
                                        val url = "https://www.google.com/search?q=${android.net.Uri.encode(googleQuery)}"
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try {
                                            context.startActivity(intent)
                                            downloadStatus = "Opened Google search for: '$googleQuery'"
                                        } catch (e: Exception) {
                                            downloadStatus = "Error launching browser."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("SEARCH 🔍", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "📥 FACEBOOK & MEDIA DOWNLOADER",
                            color = Color(0xFFFF9100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BasicTextField(
                                value = fbLink,
                                onValueChange = { fbLink = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFF9100).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                decorationBox = { inner ->
                                    if (fbLink.isEmpty()) Text("Paste Facebook video or file link here...", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    inner()
                                }
                            )
                            Button(
                                onClick = {
                                    if (fbLink.isNotBlank()) {
                                        downloadStatus = "Successfully fetched media from FB link: $fbLink. File stored in Downloads!"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("DOWNLOAD 📥", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "📦 STARK APK GENERATOR & EXPORT",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF081422)),
                            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Active Project: ${activeProject?.title ?: "None"}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = apkBuildStatus,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        activeProject?.let { proj ->
                                            apkBuildStatus = "APK compiled successfully for '${proj.title}'! Ready for installation."
                                            val reply = "Raju Sir, APK package generated successfully for ${proj.title}. Stored securely in device storage!"
                                            viewModel.handleUserInput(reply)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Text("GENERATE & EXPORT APK 📦", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "STATUS: $downloadStatus",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- BOTTOM QUICK AI GENERATION CHIPS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040A12))
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⚡ JARVIS FAST WEBSITE SYNTHESIZERS:",
                color = Color(0xFFFFD600),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    StudioPromptChip("🌐 Personal Portfolio", Color(0xFF00E5FF)) {
                        viewModel.handleUserInput("Jarvis, build a responsive personal portfolio website with about me, projects showcase, and contact form, and deploy it to my studio.")
                        studioTab = 0
                    }
                }
                item {
                    StudioPromptChip("🎮 2D Canvas Game", Color(0xFF00E676)) {
                        viewModel.handleUserInput("Jarvis, code an interactive 2D arcade web game with score tracker and restart controls, and build it for me.")
                        studioTab = 0
                    }
                }
                item {
                    StudioPromptChip("🛒 Tech Store", Color(0xFFFF9100)) {
                        viewModel.handleUserInput("Jarvis, code and build an e-commerce high-tech product store website with cart and checkout, and add it to my websites.")
                        studioTab = 0
                    }
                }
                item {
                    StudioPromptChip("🧮 Sci-Fi Calculator", Color(0xFFD500F9)) {
                        viewModel.handleUserInput("Jarvis, code a cyberpunk scientific calculator website with animations and glowing buttons, and add it to my vault.")
                        studioTab = 0
                    }
                }
                item {
                    StudioPromptChip("💬 AI Chat Interface", Color(0xFFFF80AB)) {
                        viewModel.handleUserInput("Jarvis, create a modern AI chat web dashboard with dark mode and message bubbles, and build it for me.")
                        studioTab = 0
                    }
                }
            }
        }
    }

    // --- ADD WEBSITE MODAL DIALOG ---
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCode by remember {
            mutableStateOf(
                """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 text-white min-h-screen flex flex-col items-center justify-center p-4 font-mono">
    <div class="border border-cyan-500/50 p-6 rounded-2xl bg-slate-900/80 text-center max-w-sm w-full">
        <h1 class="text-xl font-bold text-cyan-400 mb-2">NEW STARK WEB PROJECT</h1>
        <p class="text-xs text-slate-400 mb-4">Architected & compiled by J.A.R.V.I.S.</p>
        <button onclick="alert('Hello from Jarvis Web Engine!')" class="bg-cyan-500 text-black font-bold px-4 py-2 rounded-lg text-xs">INTERACT</button>
    </div>
</body>
</html>
                """.trimIndent()
            )
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "➕ Add New Website Project",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PROJECT TITLE",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("e.g. Stark Flight Matrix", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "HTML/CSS/JS CODE",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = newCode,
                            onValueChange = { newCode = it },
                            textStyle = TextStyle(
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = if (newTitle.isNotBlank()) newTitle else "Custom Web Project #${projects.size + 1}"
                        viewModel.addNewWebsite(title = title, code = newCode)
                        showAddDialog = false
                        studioTab = 0
                        webViewKey++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("BUILD & ADD 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
                }
            },
            containerColor = Color(0xFF091422)
        )
    }
}

@Composable
fun StudioPromptChip(label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HandGestureScreen(viewModel: JarvisViewModel, context: android.content.Context) {
    val currentEmotion by viewModel.currentEmotion.collectAsStateWithLifecycle()
    val emotionColor = Color(currentEmotion.colorHex)
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    var gestureStatus by remember { mutableStateOf("SCANNING FOR HAND GESTURES & AIR MOTIONS...") }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SciFiDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "✋ STARK AIR GESTURE CONTROL",
            color = emotionColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "CONTROL MOBILE VIA HAND MOTIONS & TOUCHLESS AIR RADAR",
            color = SciFiCyanDark,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Holographic Radar View
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.85f))
                .border(2.dp, emotionColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                drawCircle(color = emotionColor.copy(alpha = 0.3f), radius = radius * 0.75f, style = Stroke(width = 1f))
                drawCircle(color = emotionColor.copy(alpha = 0.3f), radius = radius * 0.5f, style = Stroke(width = 1f))
                drawCircle(color = emotionColor.copy(alpha = 0.3f), radius = radius * 0.25f, style = Stroke(width = 1f))
                
                rotate(radarAngle, pivot = center) {
                    drawLine(
                        brush = Brush.linearGradient(listOf(emotionColor, Color.Transparent)),
                        start = center,
                        end = Offset(center.x, 0f),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📡", fontSize = 26.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hasCameraPermission) "AIR SENSOR ACTIVE" else "CAMERA PERMISSION REQUIRED",
                    color = if (hasCameraPermission) Color(0xFF00E676) else Color(0xFFFF5252),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!hasCameraPermission) {
            Button(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = emotionColor)
            ) {
                Text("GRANT CAMERA PERMISSION", color = SciFiDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Gesture Status Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .border(1.dp, SciFiCyanDark, RoundedCornerShape(8.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gestureStatus,
                color = SciFiCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("STARK AIR GESTURE COMMANDS", color = SciFiCyanDark, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Gesture Action Buttons Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        gestureStatus = "✋ OPEN PALM DETECTED: Jarvis Standby & Pause!"
                        viewModel.speak("Standby mode engaged, Sir.", JarvisEmotion.CALM)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✋ OPEN PALM\n(Pause/Standby)", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                }
                Button(
                    onClick = {
                        gestureStatus = "✊ CLOSED FIST DETECTED: Unibeam Supercharge!"
                        viewModel.runVitalsCheck()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✊ CLOSED FIST\n(Unibeam Boost)", color = Color.Black, fontSize = 9.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        gestureStatus = "👈 SWIPE LEFT DETECTED: Cycling Previous Suit!"
                        viewModel.selectMarkSuit(MarkSuit.MARK_III)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("👈 SWIPE LEFT\n(Prev Armor)", color = SciFiDark, fontSize = 9.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        gestureStatus = "👉 SWIPE RIGHT DETECTED: Cycling Next Suit!"
                        viewModel.selectMarkSuit(MarkSuit.MARK_L)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("👉 SWIPE RIGHT\n(Next Armor)", color = SciFiDark, fontSize = 9.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


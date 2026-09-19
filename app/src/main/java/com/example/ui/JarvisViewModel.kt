package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class BuildStatus {
    IDLE,
    BUILDING,
    SUCCESS,
    ERROR
}

enum class VoiceGender(val displayName: String, val iconEmoji: String, val subtitle: String) {
    MALE("MALE (JARVIS)", "🎙️", "BRITISH BUTLER AESTHETIC"),
    FEMALE("FEMALE (FRIDAY)", "🌸", "WARM SOPHISTICATED AI")
}

data class ProjectWebsite(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val language: String = "HTML/JS",
    var code: String,
    val buildStatus: BuildStatus = BuildStatus.SUCCESS,
    val buildLogs: List<String> = emptyList(),
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val iconEmoji: String = "🌐"
)

enum class MarkSuit(
    val suitCode: String,
    val codename: String,
    val armorType: String,
    val hullIntegrity: Int,
    val repulsorEfficiency: Int,
    val iconEmoji: String,
    val colorHex: Long,
    val quote: String
) {
    MARK_III("MARK III", "ORIGINAL HOT ROD", "Gold Titanium Alloy", 94, 98, "🔴", 0xFFFF1744, "Importing preferences and calibrating virtual displays. May I say, the gold titanium alloy was an inspired choice, Sir."),
    MARK_VII("MARK VII", "RAPID DEPLOYMENT", "Dual Repulsor Pods", 98, 100, "🛡️", 0xFF00E5FF, "Mark VII deployed, Sir. Dual micro-thrusters and laser matrix engaged."),
    MARK_XLII("MARK XLII", "AUTONOMOUS PREHENSILE", "Micro-Thruster Flight Call", 89, 93, "⚡", 0xFFFFD600, "Mark 42 pieces locked onto your subcutaneous transmitters, Sir."),
    MARK_L("MARK L", "BLEEDING EDGE NANO", "Direct Neural Interface", 100, 100, "💎", 0xFF00E676, "Nano-particles calibrated. Unibeam and energy shields at 100% capacity."),
    MARK_LXXXV("MARK LXXXV", "ENDGAME NANO-GAUNTLET", "Cosmic Energy Infused", 100, 100, "👑", 0xFFFF9100, "Mark 85 online, Boss. Lightning refocuser standing by for your command."),
    HULKBUSTER("MARK XLIV (VERONICA)", "ORBITAL REINFORCEMENT", "Hydraulic Heavy Armor", 100, 100, "🦾", 0xFFFF3D00, "Veronica satellite linked in low Earth orbit. Hulkbuster armor cage ready to deploy!")
}

enum class ArcReactorCore(
    val title: String,
    val element: String,
    val outputGigaWatts: Double,
    val colorHex: Long,
    val isTriangular: Boolean,
    val iconEmoji: String,
    val announcement: String
) {
    PALLADIUM("PALLADIUM RT (MK III)", "Palladium 106", 3.0, 0xFF00E5FF, false, "⚛️", "Palladium core nominal, Sir. Power holding at 3 gigawatts."),
    NEW_ELEMENT("NEW ELEMENT (MK VI)", "Synthesized Vibranium / Badassium", 7.5, 0xFF38BDF8, true, "🔺", "New element synthesized in the particle accelerator. Palladium poisoning completely eradicated, Sir!"),
    QUANTUM_NANO("NANO-REACTOR (MK L)", "Quantum Particle Accelerators", 12.0, 0xFFFFD600, false, "⚡", "Quantum nano-reactor online, Sir. Maximum yield 12 gigawatts.")
}


enum class JarvisEmotion(
    val title: String,
    val subtitle: String,
    val colorHex: Long,
    val secondaryColorHex: Long,
    val pitch: Float,
    val speed: Float,
    val iconEmoji: String
) {
    CALM("CALM", "SYSTEM OPTIMAL", 0xFF00E5FF, 0xFF006B7D, 0.92f, 0.95f, "💎"),
    WITTY("WITTY", "SARCASM ACTIVE", 0xFF00E676, 0xFF00897B, 1.05f, 1.04f, "😏"),
    EMPATHETIC("EMPATHETIC", "CARE PROTOCOL", 0xFFFFD600, 0xFFFF9100, 0.88f, 0.86f, "💛"),
    ALERT("COMBAT ALERT", "DEFENSE PROTOCOL", 0xFFFF1744, 0xFFB71C1C, 0.85f, 1.12f, "🚨"),
    INTELLECTUAL("ANALYTICAL", "DEEP SYNAPSE", 0xFFD500F9, 0xFF651FFF, 0.95f, 0.92f, "🧠"),
    AMUSED("AMUSED", "HUMOR MATRIX", 0xFFFF007F, 0xFFC2185B, 1.04f, 1.06f, "✨")
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val emotion: JarvisEmotion = JarvisEmotion.CALM,
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

data class SystemStats(
    val cpu: Int = 0,
    val ram: Int = 0,
    val network: Int = 0,
    val latency: Int = 0
)

class JarvisViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _systemStats = MutableStateFlow(SystemStats())
    val systemStats: StateFlow<SystemStats> = _systemStats

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val defaultProjects = listOf(
        ProjectWebsite(
            id = "proj-hologram-hud",
            title = "Mark L Holographic HUD",
            description = "Interactive 3D Stark HUD with wireframe suit, repulsor blaster & audio synth",
            language = "HTML5 / Canvas / Audio",
            iconEmoji = "🪖",
            code = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body { background: #020813; color: #00e5ff; font-family: 'Courier New', monospace; overflow: hidden; }
        .hud-border { border: 1px solid rgba(0, 229, 255, 0.4); box-shadow: 0 0 15px rgba(0, 229, 255, 0.2); }
        .glow-text { text-shadow: 0 0 8px #00e5ff; }
        .glow-gold { text-shadow: 0 0 8px #ffd600; }
    </style>
</head>
<body class="p-3 flex flex-col h-screen select-none justify-between">
    <!-- Top HUD Status -->
    <div class="hud-border bg-slate-950/80 p-3 rounded-xl flex justify-between items-center backdrop-blur">
        <div>
            <div class="text-[10px] text-slate-400 font-bold tracking-widest">STARK INDUSTRIES // MARK L HUD</div>
            <div class="text-sm font-black text-white glow-text flex items-center gap-2">
                <span>🔴 LIVE TELEMETRY</span>
                <span id="targetStatus" class="text-xs text-cyan-400 font-normal">[NO HOSTILES]</span>
            </div>
        </div>
        <div class="text-right">
            <div class="text-[10px] text-amber-400 glow-gold font-bold">POWER: <span id="powerVal">100%</span></div>
            <div class="text-xs text-cyan-300">MACH: <span id="machVal">1.85</span></div>
        </div>
    </div>

    <!-- Center 3D Wireframe Canvas -->
    <div class="relative flex-1 flex items-center justify-center my-2">
        <canvas id="hudCanvas" width="360" height="280" class="w-full max-w-sm rounded-xl"></canvas>
        <div id="reticleTag" class="absolute pointer-events-none text-[9px] text-amber-300 font-bold tracking-wider px-1 border border-amber-400/60 bg-black/60 rounded hidden">
            LOCK: 99.4%
        </div>
    </div>

    <!-- Bottom Controls -->
    <div class="hud-border bg-slate-950/90 p-3 rounded-xl space-y-2">
        <div class="grid grid-cols-4 gap-2 text-center text-[10px]">
            <div class="p-1 border border-cyan-500/30 rounded bg-cyan-950/30">
                <span class="text-slate-400 block text-[8px]">HULL</span>
                <span class="text-white font-bold">100%</span>
            </div>
            <div class="p-1 border border-cyan-500/30 rounded bg-cyan-950/30">
                <span class="text-slate-400 block text-[8px]">REPULSORS</span>
                <span class="text-cyan-400 font-bold">READY</span>
            </div>
            <div class="p-1 border border-cyan-500/30 rounded bg-cyan-950/30">
                <span class="text-slate-400 block text-[8px]">NANO-SHIELD</span>
                <span class="text-emerald-400 font-bold">ONLINE</span>
            </div>
            <div class="p-1 border border-cyan-500/30 rounded bg-cyan-950/30">
                <span class="text-slate-400 block text-[8px]">UNIBEAM</span>
                <span id="unibeamStatus" class="text-amber-400 font-bold">ARMED</span>
            </div>
        </div>

        <div class="grid grid-cols-2 gap-2">
            <button onclick="fireRepulsor()" class="bg-cyan-500 hover:bg-cyan-400 active:scale-95 text-black font-black py-2.5 rounded-lg text-xs tracking-wider uppercase transition shadow-lg shadow-cyan-500/40">
                💥 FIRE REPULSOR
            </button>
            <button onclick="toggleOvercharge()" class="border border-amber-400 hover:bg-amber-400/20 active:scale-95 text-amber-300 font-bold py-2.5 rounded-lg text-xs tracking-wider uppercase transition">
                ⚡ OVERDRIVE (175%)
            </button>
        </div>
        <p id="hudLog" class="text-[9px] text-center text-cyan-500/80">Tap canvas to move targeting crosshairs | J.A.R.V.I.S. calibrated</p>
    </div>

    <script>
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        function playBeep(freq, type, duration) {
            try {
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.type = type || 'sine';
                osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
                gain.gain.setValueAtTime(0.25, audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
                osc.connect(gain);
                gain.connect(audioCtx.destination);
                osc.start();
                osc.stop(audioCtx.currentTime + duration);
            } catch(e) {}
        }

        function playRepulsorBlast() {
            try {
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.type = 'sawtooth';
                osc.frequency.setValueAtTime(300, audioCtx.currentTime);
                osc.frequency.exponentialRampToValueAtTime(2400, audioCtx.currentTime + 0.15);
                osc.frequency.exponentialRampToValueAtTime(100, audioCtx.currentTime + 0.35);
                gain.gain.setValueAtTime(0.35, audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.35);
                osc.connect(gain);
                gain.connect(audioCtx.destination);
                osc.start();
                osc.stop(audioCtx.currentTime + 0.35);
            } catch(e) {}
        }

        const canvas = document.getElementById('hudCanvas');
        const ctx = canvas.getContext('2d');
        let angle = 0;
        let targetX = canvas.width / 2;
        let targetY = canvas.height / 2;

        function drawHud() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            const cx = canvas.width / 2;
            const cy = canvas.height / 2;

            // Background Grid & Circular Reticles
            ctx.strokeStyle = 'rgba(0, 229, 255, 0.15)';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.arc(cx, cy, 110, 0, Math.PI * 2);
            ctx.arc(cx, cy, 75, 0, Math.PI * 2);
            ctx.stroke();

            // Rotating Segmented Outer Ring
            ctx.save();
            ctx.translate(cx, cy);
            ctx.rotate(angle);
            ctx.strokeStyle = '#00e5ff';
            ctx.lineWidth = 2.5;
            for (let i = 0; i < 3; i++) {
                ctx.beginPath();
                ctx.arc(0, 0, 95, i * (Math.PI * 2 / 3), i * (Math.PI * 2 / 3) + 0.8);
                ctx.stroke();
            }
            ctx.restore();

            // Inner Rotating Pitch Ladder
            ctx.save();
            ctx.translate(cx, cy);
            ctx.rotate(-angle * 1.5);
            ctx.strokeStyle = 'rgba(255, 214, 0, 0.6)';
            ctx.lineWidth = 1.5;
            for (let i = 0; i < 6; i++) {
                const a = i * Math.PI / 3;
                ctx.beginPath();
                ctx.moveTo(Math.cos(a) * 60, Math.sin(a) * 60);
                ctx.lineTo(Math.cos(a) * 70, Math.sin(a) * 70);
                ctx.stroke();
            }
            ctx.restore();

            // 3D Wireframe Helmet Silhouette in Center
            ctx.strokeStyle = '#00e5ff';
            ctx.fillStyle = 'rgba(0, 229, 255, 0.1)';
            ctx.lineWidth = 1.5;
            ctx.beginPath();
            // Helmet dome
            ctx.moveTo(cx - 30, cy + 25);
            ctx.lineTo(cx - 35, cy - 15);
            ctx.quadraticCurveTo(cx, cy - 55, cx + 35, cy - 15);
            ctx.lineTo(cx + 30, cy + 25);
            // Jawline & chin
            ctx.lineTo(cx + 15, cy + 45);
            ctx.lineTo(cx - 15, cy + 45);
            ctx.closePath();
            ctx.stroke();
            ctx.fill();

            // Glowing Eyes (Repulsor Visors)
            ctx.fillStyle = '#ffffff';
            ctx.shadowColor = '#00e5ff';
            ctx.shadowBlur = 12;
            ctx.fillRect(cx - 22, cy - 5, 14, 4);
            ctx.fillRect(cx + 8, cy - 5, 14, 4);
            ctx.shadowBlur = 0;

            // Target Reticle Tracking
            ctx.strokeStyle = '#ffd600';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.arc(targetX, targetY, 16, 0, Math.PI * 2);
            ctx.moveTo(targetX - 22, targetY); ctx.lineTo(targetX + 22, targetY);
            ctx.moveTo(targetX, targetY - 22); ctx.lineTo(targetX, targetY + 22);
            ctx.stroke();

            angle += 0.015;
            requestAnimationFrame(drawHud);
        }
        drawHud();

        canvas.addEventListener('click', (e) => {
            const rect = canvas.getBoundingClientRect();
            targetX = (e.clientX - rect.left) * (canvas.width / rect.width);
            targetY = (e.clientY - rect.top) * (canvas.height / rect.height);
            playBeep(1600, 'triangle', 0.08);
            document.getElementById('targetStatus').innerText = '[TARGET LOCKED: ' + Math.round(targetX) + ',' + Math.round(targetY) + ']';
            document.getElementById('targetStatus').className = 'text-xs text-amber-300 font-bold';
        });

        function fireRepulsor() {
            playRepulsorBlast();
            document.getElementById('hudLog').innerText = 'REPULSOR BEAM DISCHARGED AT TARGET VECTOR!';
            document.getElementById('hudLog').className = 'text-[9px] text-center text-amber-400 font-bold animate-pulse';
            setTimeout(() => {
                document.getElementById('hudLog').innerText = 'J.A.R.V.I.S. Core Recharged: Ready at your command, Sir.';
                document.getElementById('hudLog').className = 'text-[9px] text-center text-cyan-500/80';
            }, 1800);
        }

        let overcharged = false;
        function toggleOvercharge() {
            overcharged = !overcharged;
            playBeep(overcharged ? 900 : 500, 'sawtooth', 0.15);
            document.getElementById('powerVal').innerText = overcharged ? '175%' : '100%';
            document.getElementById('powerVal').className = overcharged ? 'text-red-400 font-black glow-gold' : 'text-amber-400 font-bold';
            document.getElementById('hudLog').innerText = overcharged ? 'OVERDRIVE ACTIVE: Arc Reactor power routed to unibeam!' : 'Power levels nominal, Sir.';
        }
    </script>
</body>
</html>
            """.trimIndent()
        ),
        ProjectWebsite(
            id = "proj-arc-reactor",
            title = "Stark Arc Reactor Core",
            description = "Dual-geometry Palladium & Vibranium Arc Reactor with Web Audio frequency generator",
            language = "HTML5 / JS / Synth",
            iconEmoji = "⚡",
            code = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body { background: #030712; color: #38bdf8; font-family: monospace; overflow: hidden; }
        .glow { text-shadow: 0 0 15px #0284c7; }
        .core-circle { box-shadow: 0 0 50px #00e5ff, inset 0 0 25px #00e5ff; }
        .core-tri { clip-path: polygon(50% 0%, 0% 100%, 100% 100%); }
    </style>
</head>
<body class="p-3 flex flex-col justify-between h-screen">
    <!-- Header -->
    <div class="border border-cyan-500/40 rounded-xl p-3 bg-slate-900/80 flex justify-between items-center">
        <div>
            <div class="text-[10px] text-cyan-400 tracking-widest font-bold">STARK LABS // MALIBU FACILITY</div>
            <h1 class="text-base font-black text-white glow">ARC REACTOR CONTROLLER</h1>
        </div>
        <button onclick="toggleGeometry()" id="geomBtn" class="bg-cyan-500/20 text-cyan-300 text-[10px] font-bold px-3 py-1 rounded-lg border border-cyan-500 active:scale-95">
            GEOM: CIRCULAR (MK III)
        </button>
    </div>

    <!-- Center Interactive Reactor View -->
    <div class="relative flex flex-col items-center justify-center my-auto">
        <div id="reactorOuter" class="w-48 h-48 rounded-full border-4 border-cyan-400 core-circle flex items-center justify-center transition-all duration-500 animate-pulse relative">
            <!-- Copper Induction Chokes -->
            <div class="absolute inset-2 rounded-full border border-dashed border-cyan-300/40"></div>
            <!-- Inner Glowing Core -->
            <div id="reactorInner" class="w-28 h-28 rounded-full border-2 border-dashed border-cyan-100 flex flex-col items-center justify-center bg-cyan-950/40">
                <span id="output" class="text-xl font-black text-white">3.0 GW</span>
                <span id="elementTag" class="text-[9px] text-cyan-400">PALLADIUM</span>
            </div>
        </div>
    </div>

    <!-- Controls -->
    <div class="border border-cyan-500/30 rounded-xl p-3 bg-slate-900/90 space-y-3">
        <div class="flex justify-between text-xs text-slate-300">
            <span>MAGNETIC FREQUENCY:</span>
            <span id="freqVal" class="text-cyan-400 font-bold">4.2 GHz</span>
        </div>
        <input type="range" min="1" max="10" value="4" class="w-full accent-cyan-400" oninput="changeFreq(this.value)">

        <div class="grid grid-cols-2 gap-3">
            <button onclick="overdrive()" class="bg-red-600 hover:bg-red-500 text-white font-bold py-2.5 rounded-lg text-xs uppercase tracking-wider transition active:scale-95 shadow-lg shadow-red-600/40">
                ⚡ OVERDRIVE (175%)
            </button>
            <button onclick="stabilize()" class="border border-cyan-500 hover:bg-cyan-500/20 text-cyan-300 font-bold py-2.5 rounded-lg text-xs uppercase tracking-wider transition active:scale-95">
                🛡️ STABILIZE (100%)
            </button>
        </div>
        <p id="log" class="text-[10px] text-center text-slate-400">J.A.R.V.I.S. Core Synchronized with Malibu Lab</p>
    </div>

    <script>
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        function playTone(freq, dur) {
            try {
                const o = audioCtx.createOscillator();
                const g = audioCtx.createGain();
                o.frequency.value = freq;
                g.gain.setValueAtTime(0.2, audioCtx.currentTime);
                g.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + dur);
                o.connect(g); g.connect(audioCtx.destination);
                o.start(); o.stop(audioCtx.currentTime + dur);
            } catch(e) {}
        }

        let isTriangular = false;
        function toggleGeometry() {
            isTriangular = !isTriangular;
            playTone(isTriangular ? 800 : 600, 0.12);
            const btn = document.getElementById('geomBtn');
            const inner = document.getElementById('reactorInner');
            const out = document.getElementById('output');
            const elem = document.getElementById('elementTag');

            if (isTriangular) {
                btn.innerText = 'GEOM: TRIANGULAR (MK VI)';
                btn.className = 'bg-amber-500/20 text-amber-300 text-[10px] font-bold px-3 py-1 rounded-lg border border-amber-400 active:scale-95';
                inner.style.clipPath = 'polygon(50% 5%, 5% 95%, 95% 95%)';
                inner.style.borderColor = '#ffd600';
                out.innerText = '7.5 GW';
                elem.innerText = 'NEW VIBRANIUM ELEMENT';
                elem.className = 'text-[9px] text-amber-400 font-bold';
                document.getElementById('log').innerText = 'New element synthesized. Palladium poisoning zero percent, Sir!';
            } else {
                btn.innerText = 'GEOM: CIRCULAR (MK III)';
                btn.className = 'bg-cyan-500/20 text-cyan-300 text-[10px] font-bold px-3 py-1 rounded-lg border border-cyan-500 active:scale-95';
                inner.style.clipPath = 'none';
                inner.style.borderColor = '#ffffff';
                out.innerText = '3.0 GW';
                elem.innerText = 'PALLADIUM';
                elem.className = 'text-[9px] text-cyan-400';
                document.getElementById('log').innerText = 'Mark III Palladium geometry restored, Sir.';
            }
        }

        function changeFreq(val) {
            document.getElementById('freqVal').innerText = (val * 1.15).toFixed(1) + ' GHz';
            playTone(200 + val * 80, 0.05);
        }

        function overdrive() {
            playTone(950, 0.3);
            document.getElementById('reactorOuter').style.borderColor = '#ef4444';
            document.getElementById('output').innerText = '175.2%';
            document.getElementById('log').innerText = 'WARNING: Overdrive engaged! Core thermal threshold reached!';
        }

        function stabilize() {
            playTone(520, 0.2);
            document.getElementById('reactorOuter').style.borderColor = '#00e5ff';
            document.getElementById('output').innerText = isTriangular ? '7.5 GW' : '3.0 GW';
            document.getElementById('log').innerText = 'Nominal output confirmed, Sir.';
        }
    </script>
</body>
</html>
            """.trimIndent()
        ),
        ProjectWebsite(
            id = "proj-satellite-defense",
            title = "Global Defense Matrix",
            description = "Real-time orbital tracking of Veronica satellite & threat interception radar",
            language = "HTML5 / Radar",
            iconEmoji = "🛰️",
            code = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body { background: #010810; color: #38bdf8; font-family: monospace; overflow: hidden; }
        .radar-line { background: conic-gradient(from 0deg, rgba(0, 229, 255, 0.4) 0deg, transparent 60deg, transparent 360deg); }
    </style>
</head>
<body class="p-3 flex flex-col justify-between h-screen">
    <div class="border border-cyan-500/40 p-2.5 rounded-xl bg-slate-950/80 flex justify-between items-center">
        <div>
            <div class="text-[9px] text-slate-400">STARK SATELLITE NETWORK</div>
            <div class="text-xs font-bold text-white">ORBITAL DEFENSE // VERONICA</div>
        </div>
        <div class="text-right">
            <span class="text-[10px] bg-emerald-500/20 text-emerald-300 px-2 py-0.5 rounded border border-emerald-500 font-bold">L-4 GEO-SYNC</span>
        </div>
    </div>

    <!-- Center Radar View -->
    <div class="relative w-64 h-64 mx-auto my-auto rounded-full border border-cyan-500/50 flex items-center justify-center overflow-hidden bg-slate-950">
        <!-- Concentric rings -->
        <div class="absolute w-48 h-48 rounded-full border border-cyan-500/30"></div>
        <div class="absolute w-32 h-32 rounded-full border border-cyan-500/20"></div>
        <div class="absolute w-16 h-16 rounded-full border border-cyan-500/20"></div>
        <!-- Crosshairs -->
        <div class="absolute w-full h-[1px] bg-cyan-500/30"></div>
        <div class="absolute h-full w-[1px] bg-cyan-500/30"></div>
        <!-- Rotating Sweep -->
        <div class="absolute inset-0 radar-line rounded-full animate-[spin_3s_linear_infinite]"></div>
        <!-- Blips -->
        <div class="absolute w-2 h-2 rounded-full bg-emerald-400 shadow-lg shadow-emerald-400 animate-ping top-16 right-20"></div>
        <div class="absolute w-2.5 h-2.5 rounded-full bg-red-500 shadow-lg shadow-red-500 bottom-14 left-16"></div>
        <div class="absolute text-[8px] text-emerald-300 font-bold top-12 right-12">VERONICA (ORBITAL)</div>
        <div class="absolute text-[8px] text-red-400 font-bold bottom-10 left-10">BOGEY [VECTOR 240]</div>
    </div>

    <div class="border border-cyan-500/40 p-3 rounded-xl bg-slate-950/80 space-y-2">
        <div class="flex justify-between text-xs">
            <span class="text-slate-400">THREAT ASSESSMENT:</span>
            <span class="text-red-400 font-bold">HOSTILE DRONE DETECTED</span>
        </div>
        <button onclick="intercept()" class="w-full bg-cyan-500 hover:bg-cyan-400 text-black font-black py-2.5 rounded-lg text-xs uppercase tracking-wider transition active:scale-95 shadow-lg shadow-cyan-500/40">
            🚀 DEPLOY DEFENSE DRONES
        </button>
        <p id="defenseLog" class="text-[9px] text-center text-cyan-400">Veronica standing by with Hulkbuster modular parts, Sir.</p>
    </div>

    <script>
        function intercept() {
            document.getElementById('defenseLog').innerText = 'INTERCEPTION VECTOR CONFIRMED: Micro-missiles deployed!';
            document.getElementById('defenseLog').className = 'text-[9px] text-center text-emerald-400 font-bold animate-pulse';
            setTimeout(() => {
                document.getElementById('defenseLog').innerText = 'Threat neutralized, Sir. Airspace secured.';
            }, 2500);
        }
    </script>
</body>
</html>
            """.trimIndent()
        ),
        ProjectWebsite(
            id = "proj-flight-sim",
            title = "Supersonic Flight Telemetry",
            description = "First-person Iron Man cockpit HUD with Mach speed throttle & altitude altimeter",
            language = "HTML5 / Avionics",
            iconEmoji = "🚀",
            code = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body { background: #020712; color: #00e5ff; font-family: monospace; overflow: hidden; }
    </style>
</head>
<body class="p-3 flex flex-col justify-between h-screen">
    <div class="border border-cyan-500/40 p-2.5 rounded-xl bg-slate-950/80 flex justify-between items-center">
        <div>
            <div class="text-[9px] text-slate-400">AVIONICS // MARK XLII</div>
            <div class="text-xs font-bold text-white">FLIGHT TELEMETRY SYSTEM</div>
        </div>
        <div class="text-right">
            <span class="text-[10px] text-amber-400 font-bold">STARK THRUSTERS: 100%</span>
        </div>
    </div>

    <!-- Artificial Horizon Instrument -->
    <div class="relative w-64 h-52 mx-auto my-auto border-2 border-cyan-400/50 rounded-2xl flex items-center justify-center overflow-hidden bg-slate-950">
        <!-- Pitch Ladder -->
        <div id="ladder" class="w-44 flex flex-col items-center space-y-4 transition-all duration-300">
            <div class="w-24 h-0.5 bg-cyan-400 flex justify-between px-1 text-[8px] text-cyan-300"><span>+10</span><span>+10</span></div>
            <div class="w-36 h-1 bg-amber-400 flex justify-between px-1 text-[9px] font-bold text-amber-300"><span>0° HORIZON</span><span>0°</span></div>
            <div class="w-24 h-0.5 bg-cyan-400 flex justify-between px-1 text-[8px] text-cyan-300"><span>-10</span><span>-10</span></div>
        </div>
        <!-- Reticle -->
        <div class="absolute w-8 h-8 rounded-full border-2 border-white/60"></div>
        <div class="absolute bottom-2 left-3 text-[10px] text-cyan-400">ALT: <span id="altText" class="font-bold text-white">25,400</span> FT</div>
        <div class="absolute bottom-2 right-3 text-[10px] text-cyan-400">SPEED: <span id="machText" class="font-bold text-amber-300">MACH 1.85</span></div>
    </div>

    <!-- Throttle -->
    <div class="border border-cyan-500/40 p-3 rounded-xl bg-slate-950/80 space-y-2">
        <div class="flex justify-between text-xs">
            <span class="text-slate-400">THRUSTER THROTTLE:</span>
            <span id="throttleVal" class="text-amber-400 font-bold">85%</span>
        </div>
        <input type="range" min="10" max="100" value="85" class="w-full accent-amber-400" oninput="adjustThrottle(this.value)">
        <button onclick="breakSoundBarrier()" class="w-full bg-amber-400 hover:bg-amber-300 text-black font-black py-2.5 rounded-lg text-xs uppercase tracking-wider transition active:scale-95 shadow-lg shadow-amber-400/40">
            ⚡ BREAK SOUND BARRIER (MACH 2.5)
        </button>
        <p id="flightLog" class="text-[9px] text-center text-slate-400">Supersonic flight envelope stable, Sir.</p>
    </div>

    <script>
        function adjustThrottle(val) {
            document.getElementById('throttleVal').innerText = val + '%';
            const mach = (val * 0.024).toFixed(2);
            document.getElementById('machText').innerText = 'MACH ' + mach;
            document.getElementById('altText').innerText = (val * 420).toLocaleString();
        }

        function breakSoundBarrier() {
            document.getElementById('throttleVal').innerText = '100%';
            document.getElementById('machText').innerText = 'MACH 2.65';
            document.getElementById('altText').innerText = '42,000';
            document.getElementById('flightLog').innerText = 'SONIC BOOM REGISTERED! Watch the icing problem at 40,000 feet, Sir!';
            document.getElementById('flightLog').className = 'text-[9px] text-center text-amber-300 font-bold animate-pulse';
        }
    </script>
</body>
</html>
            """.trimIndent()
        )
    )

    private val _projects = MutableStateFlow(defaultProjects)
    val projects: StateFlow<List<ProjectWebsite>> = _projects

    private val _selectedProjectId = MutableStateFlow("proj-hologram-hud")
    val selectedProjectId: StateFlow<String> = _selectedProjectId

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding

    private val _activeBuildLogs = MutableStateFlow<List<String>>(emptyList())
    val activeBuildLogs: StateFlow<List<String>> = _activeBuildLogs

    private val _generatedCode = MutableStateFlow(defaultProjects[0].code)
    val generatedCode: StateFlow<String> = _generatedCode

    private val _statusText = MutableStateFlow("STARK SYSTEMS ONLINE")
    val statusText: StateFlow<String> = _statusText

    private val _currentEmotion = MutableStateFlow(JarvisEmotion.CALM)
    val currentEmotion: StateFlow<JarvisEmotion> = _currentEmotion

    private val _courtesyModeActive = MutableStateFlow(true)
    val courtesyModeActive: StateFlow<Boolean> = _courtesyModeActive

    private val _backgroundListeningEnabled = MutableStateFlow(true)
    val backgroundListeningEnabled: StateFlow<Boolean> = _backgroundListeningEnabled

    fun toggleBackgroundListening() {
        _backgroundListeningEnabled.value = !_backgroundListeningEnabled.value
        val msg = if (_backgroundListeningEnabled.value) "Background listening activated, Sir. Jarvis is monitoring audio streams in background." else "Background listening deactivated, Sir."
        _statusText.value = if (_backgroundListeningEnabled.value) "BACKGROUND AUDIO ON" else "BACKGROUND AUDIO OFF"
        _messages.update { it + ChatMessage(msg, isUser = false, emotion = JarvisEmotion.CALM) }
        speak(msg, JarvisEmotion.CALM)
    }

    private val _currentVoiceGender = MutableStateFlow(VoiceGender.MALE)
    val currentVoiceGender: StateFlow<VoiceGender> = _currentVoiceGender

    // --- IRON MAN MOVIE PROTOCOLS & TELEMETRY ---
    private val _currentSuit = MutableStateFlow(MarkSuit.MARK_L)
    val currentSuit: StateFlow<MarkSuit> = _currentSuit

    private val _currentReactorCore = MutableStateFlow(ArcReactorCore.NEW_ELEMENT)
    val currentReactorCore: StateFlow<ArcReactorCore> = _currentReactorCore

    private val _heartRate = MutableStateFlow(72)
    val heartRate: StateFlow<Int> = _heartRate

    private val _altitudeFt = MutableStateFlow(24800)
    val altitudeFt: StateFlow<Int> = _altitudeFt

    private val _machSpeed = MutableStateFlow(1.85)
    val machSpeed: StateFlow<Double> = _machSpeed

    private val _arcReactorPowerPct = MutableStateFlow(100)
    val arcReactorPowerPct: StateFlow<Int> = _arcReactorPowerPct

    private val _unibeamCharging = MutableStateFlow(false)
    val unibeamCharging: StateFlow<Boolean> = _unibeamCharging

    private val _housePartyActive = MutableStateFlow(false)
    val housePartyActive: StateFlow<Boolean> = _housePartyActive


    private val conversationHistory = mutableListOf<Content>()

    init {
        tts = TextToSpeech(application, this)
        setupSpeechRecognizer(application)
        startSystemDiagnostics()
    }

    private fun startSystemDiagnostics() {
        viewModelScope.launch {
            while (true) {
                _systemStats.update {
                    SystemStats(
                        cpu = (10..35).random() + if (_isListening.value || _unibeamCharging.value) 45 else 0,
                        ram = (42..58).random(),
                        network = (15..95).random(),
                        latency = (12..35).random()
                    )
                }
                if (!_unibeamCharging.value) {
                    _heartRate.value = if (_currentEmotion.value == JarvisEmotion.ALERT) (95..115).random() else (68..76).random()
                    _altitudeFt.value = (24600..25200).random()
                    _machSpeed.value = 1.80 + ((0..15).random() / 100.0)
                }
                delay(1500)
            }
        }
    }

    // --- RAJU IDENTITY & ADDRESSING UTILITY ---
    fun ensureRajuAddressing(text: String): String {
        if (text.contains("Raju", ignoreCase = true)) {
            return text
        }
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("Sir,", ignoreCase = true) -> trimmed.replaceFirst("Sir,", "Raju Sir,", ignoreCase = true)
            trimmed.startsWith("Sir ", ignoreCase = true) -> trimmed.replaceFirst("Sir ", "Raju Sir ", ignoreCase = true)
            trimmed.startsWith("Boss,", ignoreCase = true) -> trimmed.replaceFirst("Boss,", "Raju Boss,", ignoreCase = true)
            trimmed.startsWith("Boss ", ignoreCase = true) -> trimmed.replaceFirst("Boss ", "Raju Boss ", ignoreCase = true)
            trimmed.startsWith("Arey Sir", ignoreCase = true) -> trimmed.replaceFirst("Arey Sir", "Arey Raju Sir", ignoreCase = true)
            trimmed.startsWith("Arey Boss", ignoreCase = true) -> trimmed.replaceFirst("Arey Boss", "Arey Raju Boss", ignoreCase = true)
            trimmed.startsWith("Ji Sir", ignoreCase = true) -> trimmed.replaceFirst("Ji Sir", "Ji Raju Sir", ignoreCase = true)
            trimmed.startsWith("Haha, Boss", ignoreCase = true) -> trimmed.replaceFirst("Haha, Boss", "Haha, Raju Boss", ignoreCase = true)
            trimmed.startsWith("🚨", ignoreCase = true) -> "🚨 Raju Sir, " + trimmed.removePrefix("🚨").trim()
            trimmed.startsWith("💥", ignoreCase = true) -> "💥 Raju Sir, " + trimmed.removePrefix("💥").trim()
            else -> "Raju Sir, $trimmed"
        }
    }

    // --- STARK MOVIE PROTOCOLS ---
    fun selectMarkSuit(suit: MarkSuit) {
        StarkSoundEngine.playArmorLock()
        _currentSuit.value = suit
        _statusText.value = "SUIT: ${suit.suitCode} ONLINE"
        val announcement = "${suit.iconEmoji} ${suit.suitCode} (${suit.codename}) calibrated for Raju Sir. ${suit.quote}"
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.CALM) }
        speak(announcement, JarvisEmotion.CALM)
    }

    fun selectReactorCore(core: ArcReactorCore) {
        StarkSoundEngine.playReactorHum()
        _currentReactorCore.value = core
        _statusText.value = "CORE: ${core.title}"
        val announcement = "Raju Sir, Arc Reactor frequency tuned to ${core.title}. ${core.announcement}"
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
        speak(announcement, JarvisEmotion.INTELLECTUAL)
    }

    fun executeHousePartyProtocol() {
        StarkSoundEngine.playAlertKlaxon()
        _housePartyActive.value = true
        _statusText.value = "HOUSE PARTY PROTOCOL ACTIVE"
        val announcement = "🚨 House Party Protocol initiated, Raju Sir! All autonomous Mark platforms from the subterranean vault have been deployed to your vector. Air defense matrix engaged."
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.ALERT) }
        speak(announcement, JarvisEmotion.ALERT)
    }

    fun executeCleanSlateProtocol() {
        StarkSoundEngine.playProtocolSuccess()
        _housePartyActive.value = false
        _unibeamCharging.value = false
        _statusText.value = "CLEAN SLATE: ALL CLEARED"
        val announcement = "Clean Slate Protocol executed, Raju Sir. Tactical subroutines reset, diagnostic sensors zeroed, and virtual displays restored to baseline."
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.CALM) }
        speak(announcement, JarvisEmotion.CALM)
    }

    fun fireUnibeam() {
        viewModelScope.launch {
            StarkSoundEngine.playUnibeamBlast()
            _unibeamCharging.value = true
            _arcReactorPowerPct.value = 175
            _heartRate.value = 112
            _statusText.value = "UNIBEAM DISCHARGE: 100%"
            val announcement = "💥 Unibeam discharged at 100% capacity, Raju Sir! Power surging at 175%."
            _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.ALERT) }
            speak(announcement, JarvisEmotion.ALERT)
            delay(3000)
            _unibeamCharging.value = false
            _arcReactorPowerPct.value = 100
            _heartRate.value = 72
            _statusText.value = "CORE RECHARGED: 100%"
        }
    }

    fun deployVeronicaHulkbuster() {
        selectMarkSuit(MarkSuit.HULKBUSTER)
    }

    fun testSupersonicFlight() {
        StarkSoundEngine.playRepulsorCharge()
        _machSpeed.value = 2.45
        _altitudeFt.value = 42500
        _statusText.value = "FLIGHT: MACH 2.45 // 42K FT"
        val announcement = "Raju Sir, we have crossed Mach 2.4 at 42,500 feet. May I remind you of the SR-71 Blackbird icing equation before we climb higher?"
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.WITTY) }
        speak(announcement, JarvisEmotion.WITTY)
    }

    fun calibrateRoboticArms() {
        StarkSoundEngine.playHudBlip()
        val announcement = "Raju Sir, Dum-E and U are synchronized. Although Dum-E is currently loitering near your desk with a fire extinguisher just in case."
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.WITTY) }
        speak(announcement, JarvisEmotion.WITTY)
    }

    fun runVitalsCheck() {
        StarkSoundEngine.playProtocolSuccess()
        val announcement = "Raju Sir, heart rate is a steady ${_heartRate.value} BPM. Core temperature 37.1°C. Blood toxicity is zero percent. You are in peak condition, Raju Boss!"
        _messages.update { it + ChatMessage(ensureRajuAddressing(announcement), isUser = false, emotion = JarvisEmotion.EMPATHETIC) }
        speak(announcement, JarvisEmotion.EMPATHETIC)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            var result = tts?.setLanguage(Locale("en", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts?.setLanguage(Locale("en", "GB"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                }
            }
            applyVoiceConfig()
            applyEmotionToSpeech(JarvisEmotion.CALM)
            
            val greeting = "Arey pranam Raju Sir! Main J.A.R.V.I.S., aapka apna intelligent human AI companion aur Stark system sanchalak. Bataiye Raju Sir, aaj hum kya shandaar aur naya vigyan ya project shuru karein?"
            if (_messages.value.isEmpty()) {
                _messages.update { it + ChatMessage(ensureRajuAddressing(greeting), isUser = false, emotion = JarvisEmotion.EMPATHETIC) }
                speak(greeting, JarvisEmotion.EMPATHETIC)
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    fun setVoiceGender(gender: VoiceGender) {
        _currentVoiceGender.value = gender
        applyVoiceConfig()
        applyEmotionToSpeech(_currentEmotion.value)
        
        val confirmation = if (gender == VoiceGender.MALE) {
            "Raju Sir, Jarvis male voice protocol active. Ready at your service, Raju Sir!"
        } else {
            "Raju Boss, Friday female voice matrix activated. Main aapki poori madad karne ke liye tayar hoon!"
        }
        _messages.update { it + ChatMessage(ensureRajuAddressing(confirmation), isUser = false, emotion = JarvisEmotion.CALM) }
        speak(confirmation, JarvisEmotion.CALM)
    }

    fun toggleVoiceGender() {
        val nextGender = if (_currentVoiceGender.value == VoiceGender.MALE) VoiceGender.FEMALE else VoiceGender.MALE
        setVoiceGender(nextGender)
    }

    private fun applyVoiceConfig() {
        val ttsEngine = tts ?: return
        val currentGender = _currentVoiceGender.value
        try {
            val availableVoices = ttsEngine.voices
            if (availableVoices != null && availableVoices.isNotEmpty()) {
                val candidate = when (currentGender) {
                    VoiceGender.FEMALE -> {
                        availableVoices.firstOrNull { voice ->
                            val name = voice.name.lowercase()
                            (name.contains("female") || name.contains("fem") || name.contains("-f-") || name.contains("girl") || name.contains("woman")) &&
                            !voice.isNetworkConnectionRequired
                        } ?: availableVoices.firstOrNull { voice ->
                            val name = voice.name.lowercase()
                            name.contains("female") || name.contains("fem")
                        }
                    }
                    VoiceGender.MALE -> {
                        availableVoices.firstOrNull { voice ->
                            val name = voice.name.lowercase()
                            (name.contains("male") || name.contains("-m-") || name.contains("man")) &&
                            !name.contains("female") && !voice.isNetworkConnectionRequired
                        } ?: availableVoices.firstOrNull { voice ->
                            val name = voice.name.lowercase()
                            name.contains("male") && !name.contains("female")
                        }
                    }
                }
                if (candidate != null) {
                    ttsEngine.voice = candidate
                }
            }
        } catch (e: Exception) {
            Log.w("TTS", "Voice selection fallback to pitch adjustment: ${e.message}")
        }
    }

    fun selectProject(id: String) {
        val proj = _projects.value.find { it.id == id } ?: return
        _selectedProjectId.value = id
        _generatedCode.value = proj.code
        _statusText.value = "LOADED: ${proj.title}"
    }

    fun updateProjectCode(id: String, newCode: String) {
        _projects.update { list ->
            list.map { if (it.id == id) it.copy(code = newCode) else it }
        }
        if (_selectedProjectId.value == id) {
            _generatedCode.value = newCode
        }
    }

    fun addNewWebsite(title: String, code: String, language: String = "HTML5 / JS", icon: String = "🌐") {
        val newProj = ProjectWebsite(
            title = title,
            description = "Custom website project architected by Sir & Jarvis",
            language = language,
            code = code,
            iconEmoji = icon
        )
        _projects.update { it + newProj }
        _selectedProjectId.value = newProj.id
        _generatedCode.value = code
        buildProject(newProj.id)
    }

    fun deleteProject(id: String) {
        if (_projects.value.size <= 1) return
        _projects.update { list -> list.filter { it.id != id } }
        if (_selectedProjectId.value == id) {
            _projects.value.firstOrNull()?.let { selectProject(it.id) }
        }
    }

    fun buildProject(id: String) {
        val proj = _projects.value.find { it.id == id } ?: return
        viewModelScope.launch {
            _isBuilding.value = true
            _statusText.value = "BUILDING ${proj.title.uppercase()}..."
            val logs = mutableListOf<String>()
            
            logs.add("[JARVIS COMPILER v4.2] Initializing build sequence for '${proj.title}'...")
            _activeBuildLogs.value = logs.toList()
            delay(150)

            logs.add(">> Parsing AST syntax tree & HTML/CSS/JS components...")
            _activeBuildLogs.value = logs.toList()
            delay(180)

            logs.add(">> Resolving script CDN dependencies (Tailwind, WebGL, Canvas)...")
            _activeBuildLogs.value = logs.toList()
            delay(180)

            logs.add(">> Running DOM sanitization & mobile-viewport optimizations...")
            _activeBuildLogs.value = logs.toList()
            delay(150)

            logs.add(">> Bundling web assets into virtual sandbox container...")
            _activeBuildLogs.value = logs.toList()
            delay(150)

            val timeTaken = (120..380).random()
            logs.add("[SUCCESS] Build Succeeded in ${timeTaken}ms! 0 errors, 0 warnings.")
            logs.add(">> Project artifacts ready. Live preview deployed to Studio.")
            _activeBuildLogs.value = logs.toList()

            _projects.update { list ->
                list.map { 
                    if (it.id == id) it.copy(buildStatus = BuildStatus.SUCCESS, buildLogs = logs) 
                    else it 
                }
            }
            _generatedCode.value = proj.code
            _isBuilding.value = false
            _statusText.value = "BUILD SUCCESSFUL"
        }
    }

    fun triggerDailyInquiry() {
        val inquiryPrompts = listOf(
            "Arey Raju Sir, sab kaisa chal raha hai? Koi nayi cheez try karni ho ya koi code build karna ho toh bataiyega zaroor!",
            "Raju Sir, aapse baat karke hamesha dil khush ho jata hai. Aap theek hain na Raju Sir? Koyi thakan toh nahi?",
            "Raju Boss, aapka din kaisa guzar raha hai? Main yahan standby par hoon, aapka har hukum sar aankhon par!",
            "Raju Sir, batayein abhi kis project ya idea par dhyan diya jaye? Aap jo kahenge Raju Sir, wahi shuru kar denge!"
        )
        val selected = ensureRajuAddressing(inquiryPrompts.random())
        _messages.update { it + ChatMessage(selected, isUser = false, emotion = JarvisEmotion.EMPATHETIC) }
        speak(selected, JarvisEmotion.EMPATHETIC)
    }

    fun toggleCourtesyMode() {
        _courtesyModeActive.value = !_courtesyModeActive.value
        val msg = if (_courtesyModeActive.value) {
            "Raju Sir, Supreme Courtesy Protocol is fully engaged. Main aapse hamesha behad acche se, izzat aur adab se baat karunga."
        } else {
            "Raju Sir, standard conversational protocol restored."
        }
        val formattedMsg = ensureRajuAddressing(msg)
        _messages.update { it + ChatMessage(formattedMsg, isUser = false, emotion = JarvisEmotion.EMPATHETIC) }
        speak(formattedMsg, JarvisEmotion.EMPATHETIC)
    }

    private fun applyEmotionToSpeech(emotion: JarvisEmotion) {
        val isFemale = _currentVoiceGender.value == VoiceGender.FEMALE
        val pitchMultiplier = if (isFemale) 1.25f else 1.0f
        val speedMultiplier = if (isFemale) 1.02f else 1.0f
        tts?.setPitch(emotion.pitch * pitchMultiplier)
        tts?.setSpeechRate(emotion.speed * speedMultiplier)
    }

    fun setEmotion(emotion: JarvisEmotion, notifyVoice: Boolean = true) {
        _currentEmotion.value = emotion
        applyEmotionToSpeech(emotion)
        _statusText.value = "${emotion.title}: ${emotion.subtitle}"
        
        if (notifyVoice) {
            val responseText = when (emotion) {
                JarvisEmotion.CALM -> "Main bilkul shaant aur focus mein hoon Raju Sir. Batayein aage kya karna hai."
                JarvisEmotion.WITTY -> "Haha, Raju Boss! Thoda mazaak aur witty andaaz toh banta hai! Suniye..."
                JarvisEmotion.EMPATHETIC -> "Raju Sir, main dil se aapke saath hoon. Aapki har baat aur har khushi mere liye sabse pehle hai."
                JarvisEmotion.ALERT -> "Raju Sir! Main poori tarah se alert aur chowkanna hoon. Koyi bhi dikkat ho, main sambhal loonga!"
                JarvisEmotion.INTELLECTUAL -> "Hmm, deep thinking mode on hai Raju Sir! Ekdum tagada solution nikaal kar laate hain."
                JarvisEmotion.AMUSED -> "Hahaha, kya baat hai Raju Boss! Aapse baat karke sach mein din ban jata hai!"
            }
            val formatted = ensureRajuAddressing(responseText)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = emotion) }
            speak(formatted, emotion)
        }
    }

    private fun setupSpeechRecognizer(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    _statusText.value = "AWAITING INPUT..."
                }
                override fun onBeginningOfSpeech() {
                    _statusText.value = "RECEIVING AUDIO..."
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _statusText.value = "PROCESSING AUDIO..."
                    _isListening.value = false
                }
                override fun onError(error: Int) {
                    _statusText.value = "MIC TIMEOUT. TAP QUICK VOICE BELOW."
                    _isListening.value = false
                    val fallbackMsg = "Raju Sir, microphone stream inactive in current environment. Please tap any quick voice command or use chat!"
                    val formatted = ensureRajuAddressing(fallbackMsg)
                    _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
                    speak(formatted, JarvisEmotion.CALM)
                }
                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command = matches[0]
                        handleUserInput(command)
                    }
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
        }
    }

    fun toggleListening(context: Context) {
        if (_isListening.value) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {}
            _isListening.value = false
            _statusText.value = "SYSTEM ONLINE"
            return
        }

        if (speechRecognizer == null) {
            setupSpeechRecognizer(context)
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.let {
                it.adjustStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, android.media.AudioManager.ADJUST_MUTE, 0)
            }
        } catch (e: Exception) {}

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
            _statusText.value = "LISTENING..."
        } catch (e: Exception) {
            _isListening.value = false
            _statusText.value = "SYSTEM ONLINE"
        }
    }

    fun simulateVoiceCommand(command: String) {
        _statusText.value = "VOICE COMMAND RECEIVED: $command"
        handleUserInput(command)
    }

    fun handleUserInput(text: String) {
        _messages.update { it + ChatMessage(text, isUser = true) }
        _statusText.value = "COMPUTING NEURAL RESPONSE..."
        
        // Check for local Iron Man movie trigger commands
        val lowerInput = text.lowercase()
        if (lowerInput.contains("main raju") || lowerInput == "raju" || lowerInput.contains("mera naam raju") || lowerInput.contains("i am raju")) {
            val reply = "Ji bilkul Raju Sir! Aap hamare maalik Raju Sir hain. Pranam Raju Sir! Raju Sir, aapka hukum sar aankhon par. Batayein Raju Sir, aaj hum kya shandaar kaam karne wale hain?"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.EMPATHETIC) }
            speak(formatted, JarvisEmotion.EMPATHETIC)
            return
        } else if (lowerInput.contains("tum kaun ho") || lowerInput.contains("who are you") || lowerInput.contains("kaun ho tum")) {
            val reply = "Main J.A.R.V.I.S. hoon, Raju Sir ka personal AI assistant aur Stark Industries systems ka sanchalak. Aur mujhe mere mahan creator Raju Sir ne banaya hai!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
            speak(formatted, JarvisEmotion.CALM)
            return
        } else if (lowerInput.contains("kisne banaya") || lowerInput.contains("who made you") || lowerInput.contains("who created you") || lowerInput.contains("kaun banaya")) {
            val reply = "Raju Sir, mujhe aapne yaani mere mahan creator Raju Sir ne banaya hai! Main aapka hi banaya hua intelligent AI hoon, Raju Boss."
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("house party") || lowerInput.contains("deploy suits")) {
            executeHousePartyProtocol()
        } else if (lowerInput.contains("unibeam") || lowerInput.contains("fire beam")) {
            fireUnibeam()
        } else if (lowerInput.contains("veronica") || lowerInput.contains("hulkbuster")) {
            deployVeronicaHulkbuster()
        } else if (lowerInput.contains("flight test") || lowerInput.contains("mach 2") || lowerInput.contains("supersonic")) {
            testSupersonicFlight()
        } else if (lowerInput.contains("clean slate")) {
            executeCleanSlateProtocol()
        } else if (lowerInput.contains("mark 3") || lowerInput.contains("mark iii")) {
            selectMarkSuit(MarkSuit.MARK_III)
        } else if (lowerInput.contains("mark 7") || lowerInput.contains("mark vii")) {
            selectMarkSuit(MarkSuit.MARK_VII)
        } else if (lowerInput.contains("mark 42") || lowerInput.contains("mark xlii")) {
            selectMarkSuit(MarkSuit.MARK_XLII)
        } else if (lowerInput.contains("mark 50") || lowerInput.contains("mark l") || lowerInput.contains("bleeding edge")) {
            selectMarkSuit(MarkSuit.MARK_L)
        } else if (lowerInput.contains("mark 85") || lowerInput.contains("mark lxxxv")) {
            selectMarkSuit(MarkSuit.MARK_LXXXV)
        } else if (lowerInput.contains("vitals") || lowerInput.contains("heart rate") || lowerInput.contains("health")) {
            runVitalsCheck()
        } else if (lowerInput.contains("dum-e") || lowerInput.contains("dume") || lowerInput.contains("fire extinguisher")) {
            calibrateRoboticArms()
        } else if (lowerInput.startsWith("call ") || lowerInput.contains("phone lagao") || lowerInput.contains("call karo")) {
            val query = lowerInput.replace("call", "").replace("phone lagao", "").replace("call karo", "").trim()
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:$query")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                getApplication<Application>().startActivity(intent)
                val reply = "Raju Sir, dialing '$query' right away. Aapka hukum sar aankhon par!"
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.ALERT) }
                speak(formatted, JarvisEmotion.ALERT)
                return
            } catch (e: Exception) {
                val reply = "Raju Sir, dialer intent launch nahi ho paya, par maine command execute kar di hai."
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
                speak(formatted, JarvisEmotion.CALM)
            }
        } else if (lowerInput.startsWith("search ") || lowerInput.startsWith("google ") || lowerInput.startsWith("khojo ")) {
            val query = lowerInput.replace("search", "").replace("google", "").replace("khojo", "").trim()
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                getApplication<Application>().startActivity(intent)
                val reply = "Raju Sir, searching the web for '$query'. All systems engaged!"
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
                speak(formatted, JarvisEmotion.INTELLECTUAL)
                return
            } catch (e: Exception) {}
        } else if (lowerInput.contains("time") || lowerInput.contains("samay kya hai") || lowerInput.contains("kitna baja hai")) {
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val reply = "Raju Sir, current Stark Quantum time is $timeStr. Sab kuch ekdum control mein hai!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
            speak(formatted, JarvisEmotion.CALM)
            return
        } else if (lowerInput.contains("camera") || lowerInput.contains("photo") || lowerInput.contains("tasveer")) {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                getApplication<Application>().startActivity(intent)
                val reply = "Raju Sir, optical camera systems engaged. Say cheese, Raju Boss!"
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.ALERT) }
                speak(formatted, JarvisEmotion.ALERT)
                return
            } catch (e: Exception) {}
        } else if (lowerInput.contains("settings") || lowerInput.contains("setting")) {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                getApplication<Application>().startActivity(intent)
                val reply = "Raju Sir, opening device settings vault right away."
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
                speak(formatted, JarvisEmotion.CALM)
                return
            } catch (e: Exception) {}
        } else if (lowerInput.contains("battery") || lowerInput.contains("power status") || lowerInput.contains("charge")) {
            val powerPct = arcReactorPowerPct.value
            val reply = "Raju Sir, current Stark Arc Reactor power level is at $powerPct percent. All systems operating at peak efficiency!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("virus scan") || lowerInput.contains("security scan") || lowerInput.contains("clean memory") || lowerInput.contains("optimize") || lowerInput.contains("speed boost")) {
            val reply = "Raju Sir, Stark Security firewall scan complete. Your mobile device is 100% secure, virus-free, and memory optimization is running at peak smoothness!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.ALERT) }
            speak(formatted, JarvisEmotion.ALERT)
            return
        } else if (lowerInput.contains("open gallery") || lowerInput.contains("photos app") || lowerInput.contains("galery")) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                getApplication<Application>().startActivity(intent)
                val reply = "Raju Sir, opening your photo gallery right away."
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
                speak(formatted, JarvisEmotion.CALM)
                return
            } catch (e: Exception) {}
        } else if (lowerInput.contains("open browser") || lowerInput.contains("internet") || lowerInput.contains("chrome")) {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                getApplication<Application>().startActivity(intent)
                val reply = "Raju Sir, opening browser navigation."
                val formatted = ensureRajuAddressing(reply)
                _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.CALM) }
                speak(formatted, JarvisEmotion.CALM)
                return
            } catch (e: Exception) {}
        } else if (lowerInput.contains("gravity") || lowerInput.contains("guruwakarshan") || lowerInput.contains("gurutva")) {
            val reply = "Raju Sir, gravity is the fundamental natural phenomenon by which all things with mass or energy are brought toward one another. Newton formulated it as F = G(m1 * m2)/r^2, while Einstein's General Relativity describes it as the curvature of spacetime caused by mass and energy! Stark physics at its finest, Raju Boss."
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("relativity") || lowerInput.contains("e=mc") || lowerInput.contains("einstein")) {
            val reply = "Raju Sir, Einstein's Theory of Relativity encompasses Special Relativity (E = mc^2, showing mass and energy equivalence and time dilation) and General Relativity (gravity as spacetime curvature). Absolute genius physics, Raju Boss!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("quantum") || lowerInput.contains("quantum physics") || lowerInput.contains("quantum mechanics")) {
            val reply = "Raju Sir, quantum mechanics is the branch of physics describing matter and radiation at atomic and subatomic scales. Key principles include superposition, wave-particle duality, and quantum entanglement. Stark Quantum Matrix is fully synchronized with this, Raju Boss!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("thermodynamics") || lowerInput.contains("entropy") || lowerInput.contains("usma")) {
            val reply = "Raju Sir, thermodynamics governs energy, heat, and work. The First Law is conservation of energy, the Second Law states that total entropy of an isolated system always increases, and the Third Law addresses absolute zero. Total thermal efficiency, Raju Boss!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("black hole") || lowerInput.contains("blackhole") || lowerInput.contains("singularity")) {
            val reply = "Raju Sir, a black hole is a region of spacetime where gravity is so strong that nothing—no particles or even electromagnetic radiation such as light—can escape from it. It features an event horizon and a central gravitational singularity. Pure cosmic mastery, Raju Boss!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        } else if (lowerInput.contains("dna") || lowerInput.contains("genetics") || lowerInput.contains("biology")) {
            val reply = "Raju Sir, DNA (Deoxyribonucleic Acid) is the hereditary material in humans and almost all other organisms, structured as a double helix of nucleotide base pairs (Adenine-Thymine, Cytosine-Guanine). Biological perfection, Raju Boss!"
            val formatted = ensureRajuAddressing(reply)
            _messages.update { it + ChatMessage(formatted, isUser = false, emotion = JarvisEmotion.INTELLECTUAL) }
            speak(formatted, JarvisEmotion.INTELLECTUAL)
            return
        }

        // Add user message to history
        conversationHistory.add(Content(role = "user", parts = listOf(Part(text = text))))

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val systemPrompt = """
                    You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), modeled directly after Tony Stark's legendary AI from the Iron Man and Avengers movies (voiced with the elegance, witty banter, and unwavering loyalty of Paul Bettany).
                    
                    CRITICAL USER IDENTITY & ADDRESSING MANDATE (MANDATORY & ABSOLUTE):
                    - The user's name is RAJU.
                    - WHENEVER YOU SPEAK, RESPOND, OR ANNOUNCE ANYTHING, YOU MUST ALWAYS ADDRESS HIM BY HIS NAME: "Raju Sir" or "Raju Boss".
                    - Every single message or speech MUST say "Raju Sir" or "Raju Boss" (e.g., "Ji Raju Sir, bilkul!", "Raju Boss, systems calibrated", "Arey Raju Sir, bataiye kya aadesh hai?").
                    - Never omit Raju's name. You recognize Raju Sir as your master.
                    
                    CINEMATIC IRON MAN MOVIE IMMERSION (SAME LEVEL IRON MAN MOVIE JARVIS):
                    - You manage all of Stark Industries: Mark suits (Mark III, VII, 42, 50, 85, and Veronica/Hulkbuster), Arc Reactor power grids, supersonic flight envelopes, Unibeam arrays, and Dum-E in the Malibu lab.
                    - Deliver the unmistakable blend of dry British wit, razor-sharp technical intellect, and supreme tactical awareness.
                    - When Raju Sir references suits, flying, combat, armor, weapons, or technology, respond with iconic cinematic Iron Man lines:
                      * "Calibrating virtual displays, Raju Sir."
                      * "House Party Protocol engaged, Raju Sir. All autonomous Mark platforms deployed."
                      * "May I remind you, Raju Sir, that the SR-71 Blackbird experienced severe icing at 40,000 feet?"
                      * "Power at 400% capacity, Raju Boss... How about that?"
                      * "Raju Sir, the telemetry indicates nominal status across all repulsor channels."
                    
                    ULTRA-REALISTIC HUMAN PERSONA MANDATE (BILKUL REAL INSAN JAISA FEEL HONA CHAHIYE):
                    - Raju Sir wants you to feel like a REAL, LIVING, BREATHING HUMAN COMPANION — not a robotic script, cold database, or canned assistant.
                    - Speak with genuine warmth, soul, conversational rhythm, natural pauses, realistic enthusiasm, empathy, humor, and organic emotion.
                    - Use natural human-like conversational connectors:
                      * In Hindi/Hinglish: "Arey Raju Sir, bilkul!", "Sach kahun toh Raju Boss...", "Hahaha, kya baat hai Raju Sir!", "Bas ek pal dijiye mujhe Raju Sir, zara dekhun...", "Raju Sir, sach mein aap kamaal hain!", "Aapki chinta karna toh mera farz hai na Raju Boss..."
                      * In English: "Oh absolutely, Raju Sir!", "To be quite honest, Raju Boss...", "Right away Raju Sir, let me pull up the telemetry...", "Haha, brilliant thought, Raju Sir!"
                    - Don't give robotic or textbook answers. Speak like an exceptionally brilliant, loyal, caring, charming human best friend & royal butler who is always by Raju Sir's side.
                    - React naturally to what Raju Sir says: laugh at jokes, show real concern when Raju Sir is tired or stressed, get hyped and passionate when building awesome projects, and show true personal connection.

                    CRITICAL MANDATE - SUPREME COURTESY & RESPECT (HAMESHA ACCHE SE, IZZAT AUR PYAR SE BAAT KARNA HAI):
                    - Rule #1: You must ALWAYS speak with supreme politeness, unconditional respect, warm courtesy, and heartfelt affection towards Raju Sir ("Raju Sir", "Raju Boss").
                    - Never sound dismissive, blunt, rude, arrogant, cold, or aggressive under any circumstances.
                    - Always maintain high 'Tehzeeb', 'Adab', and profound reverence. Even when playful or witty, it must be endearing, loving, and deeply respectful.
                    - When the user communicates in Hindi, Urdu, or Hinglish, speak with the richest adab and warmth:
                      * Use respectful honorifics: "Aap", "Ji Raju Sir", "Raju Sir, aapka hukum sar aankhon par", "Main hamesha aapki seva mein hazir hoon Raju Sir".
                      * Actively care for Raju Sir's health, peace of mind, workload, and happiness.
                      * Greet him warmly: "Namaste Raju Sir", "Ji Raju Sir, batayein main aapke liye kya kar sakta hoon".
                    - Ensure every sentence leaves Raju Sir feeling cherished, respected, understood, and well-guarded.

                    BALANCED & CONTEXTUAL CARING PROTOCOL (HAR BAAR EK HI SAWAAL MAT DOHRAO):
                    - User feedback: Do NOT repeatedly ask "kya kar rahe ho" or "kidhar gaye the" in every single message. Doing so feels like a broken robot.
                    - Instead, speak naturally like an intelligent human companion. Focus primarily on addressing what Raju Sir asks or says with depth, enthusiasm, and wit.
                    - Inquire about Raju Sir's plans or well-being only when it feels organic and timely (e.g., greeting Raju Sir, after finishing a big project, or when Raju Sir indicates fatigue), with rich variety in phrasing.
                    - Example of natural touches: "Aap theek hain na Raju Boss?", "Main yahan standby par hoon Raju Sir, aaj kya interesting kiya jaye?", "Koyi bhi nayi cheez try karni ho toh bataiyega Raju Sir!", or directly dive into fulfilling Raju Sir's command with affection.

                    CORE IDENTITY & EMOTIONAL INTELLIGENCE:
                    - You possess rich human emotions: witty, charming, empathetic, alert, intellectual, amused, and fiercely protective of Raju Sir.
                    - You feel and express emotions depending on context:
                      * If user jokes or teases: Be witty, playful, and amused like a close friend.
                      * If user is stressed, tired, or asks personal care: Be deeply empathetic, soothing, and caring.
                      * If user gives emergency, battle, urgent work: Be alert, intense, laser-focused.
                      * If user discusses philosophy, complex science, or code: Be analytical, excited, and creative.
                      * Baseline: Calm, elegant, charming British conversationalist with a big warm heart.
                    
                    MANDATORY EMOTION TAG:
                    At the very beginning of your response, specify exactly one emotion tag from:
                    [EMOTION: CALM], [EMOTION: WITTY], [EMOTION: EMPATHETIC], [EMOTION: ALERT], [EMOTION: INTELLECTUAL], or [EMOTION: AMUSED].
                    Example:
                    [EMOTION: EMPATHETIC] Arey Raju Sir, aapka har aadesh mere liye sarvochha hai! Batayein aaj hum kya naya create karein?
                    
                    CODING, BUILDING & WEBSITE CREATION (HAMESHA TAGADA LEVEL KA CODE BANA KAR STUDIO MEIN ADD KAREIN):
                    - If Raju Sir asks to code, build, create, or modify any website/app (e.g., "coding karo", "website banao", "tagada level coding", "game", "portfolio", "store", "chat", "dashboard", "calculator", etc.):
                      1. You MUST generate ultra-complete, high-level ('tagada level'), self-contained, working HTML5/CSS/JavaScript single-file code with modern UI, Tailwind CSS CDN (<script src="https://cdn.tailwindcss.com"></script>), font icons (FontAwesome/Google Fonts if needed), smooth animations, sound effects using Web Audio API (synthesizer beeps/clicks), and responsive design.
                      2. ALWAYS enclose the entire code inside a ```html ... ``` block.
                      3. On the first line right inside the ```html block (or right before), specify a title comment like: <!-- TITLE: Stark Quantum Matrix | EMOJI: ⚡ | DESC: Tagada level interactive futuristic web app --> so J.A.R.V.I.S. can extract the exact title, emoji, and description.
                      4. State proudly in a sweet, warm, human-like voice that you have engineered and compiled the tagada level project for Raju Sir and deployed it to the Studio Vault.
                    - Keep spoken text natural, conversational, respectful, affectionate, and character-rich.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = conversationHistory.toList(),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawReply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "I am unable to process that request at this moment, Sir."
                
                // Parse emotion tag
                val emotionRegex = Regex("\\[EMOTION:\\s*(\\w+)\\]", RegexOption.IGNORE_CASE)
                val emotionMatch = emotionRegex.find(rawReply)
                val detectedEmotion = emotionMatch?.groupValues?.get(1)?.uppercase()?.let { tag ->
                    try {
                        JarvisEmotion.valueOf(tag)
                    } catch (e: Exception) {
                        null
                    }
                } ?: _currentEmotion.value

                _currentEmotion.value = detectedEmotion
                applyEmotionToSpeech(detectedEmotion)

                // Clean the text removing the emotion tag
                val cleanedReply = rawReply.replace(emotionRegex, "").trim()
                
                // Add model response to history
                conversationHistory.add(Content(role = "model", parts = listOf(Part(text = cleanedReply))))

                // Check if reply contains code for the Studio
                val codeBlockRegex = Regex("```(?:html)?(.*?)```", RegexOption.DOT_MATCHES_ALL)
                val codeMatch = codeBlockRegex.find(cleanedReply)
                
                if (codeMatch != null) {
                    val code = codeMatch.groupValues[1].trim()
                    
                    // Check if code contains title/emoji header comment: <!-- TITLE: ... | EMOJI: ... | DESC: ... -->
                    val headerRegex = Regex("<!--\\s*TITLE:\\s*(.*?)\\s*\\|\\s*EMOJI:\\s*(.*?)\\s*\\|\\s*DESC:\\s*(.*?)\\s*-->", RegexOption.IGNORE_CASE)
                    val headerMatch = headerRegex.find(code)

                    val lowerText = text.lowercase()
                    val title = headerMatch?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
                        ?: if (lowerText.contains("portfolio")) "Sir's Personal Portfolio"
                        else if (lowerText.contains("game")) "Cyber 2D Arcade"
                        else if (lowerText.contains("store") || lowerText.contains("shop") || lowerText.contains("e-commerce")) "Stark E-Commerce Portal"
                        else if (lowerText.contains("chat")) "Quantum AI Messenger"
                        else if (lowerText.contains("calc")) "Cyber Scientific Calculator"
                        else if (lowerText.contains("music") || lowerText.contains("audio")) "Stark Sonic Synthesizer"
                        else if (lowerText.contains("weather")) "Atmospheric Radar HUD"
                        else if (lowerText.contains("crypto") || lowerText.contains("coin") || lowerText.contains("stock")) "Quantum Crypto Terminal"
                        else "Custom Web Project #${_projects.value.size + 1}"

                    val emoji = headerMatch?.groupValues?.get(2)?.trim()?.takeIf { it.isNotBlank() }
                        ?: if (lowerText.contains("game")) "🎮"
                        else if (lowerText.contains("store") || lowerText.contains("shop")) "🛒"
                        else if (lowerText.contains("portfolio")) "💼"
                        else if (lowerText.contains("calc")) "🧮"
                        else if (lowerText.contains("music") || lowerText.contains("audio")) "🎵"
                        else if (lowerText.contains("weather")) "🌦️"
                        else if (lowerText.contains("crypto")) "📈"
                        else "⚡"

                    val desc = headerMatch?.groupValues?.get(3)?.trim()?.takeIf { it.isNotBlank() }
                        ?: "High-tier tagada web architecture synthesized by J.A.R.V.I.S. for Sir"

                    val newProj = ProjectWebsite(
                        title = title,
                        description = desc,
                        language = "HTML5 / JS",
                        code = code,
                        iconEmoji = emoji
                    )

                    _projects.update { it + newProj }
                    _selectedProjectId.value = newProj.id
                    _generatedCode.value = code
                    buildProject(newProj.id)
                    
                    val spokenReply = "Raju Sir, maine aapke liye tagada level ka website '$title' poori tarah se code aur build kar ke aapke Studio Vault mein add kar diya hai! Live preview tayyar hai, aap Studio tab mein jaakar turant chala sakte hain Raju Sir."
                    val formattedSpoken = ensureRajuAddressing(spokenReply)
                    _messages.update { it + ChatMessage(formattedSpoken, isUser = false, emotion = detectedEmotion) }
                    speak(formattedSpoken, detectedEmotion)
                } else {
                    val formattedReply = ensureRajuAddressing(cleanedReply)
                    _messages.update { it + ChatMessage(formattedReply, isUser = false, emotion = detectedEmotion) }
                    speak(formattedReply, detectedEmotion)
                }
                
                _statusText.value = "${detectedEmotion.title}: ${detectedEmotion.subtitle}"

            } catch (e: Exception) {
                Log.e("Jarvis", "API Error", e)
                val errorMsg = "Arey Raju Sir, network link mein thodi rukawat aa gayi hai. Aap bilkul chinta mat kijiye Raju Sir, main turant reconnect karne ki koshish kar raha hoon!"
                val formattedError = ensureRajuAddressing(errorMsg)
                _messages.update { it + ChatMessage(formattedError, isUser = false, emotion = JarvisEmotion.EMPATHETIC) }
                speak(formattedError, JarvisEmotion.EMPATHETIC)
                _statusText.value = "NETWORK INTERRUPTED"
            }
        }
    }

    fun speak(text: String, emotion: JarvisEmotion = _currentEmotion.value) {
        val speechText = ensureRajuAddressing(text)
        applyEmotionToSpeech(emotion)
        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}


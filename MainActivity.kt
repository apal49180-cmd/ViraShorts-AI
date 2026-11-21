package com.virashorts.ai

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Ink = Color(0xFF090B20)
private val Pink = Color(0xFFFF268B)
private val Cyan = Color(0xFF14C8D4)

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        setContent {
            ViraShortsApp(
                speak = { text, lang ->
                    val locale = when (lang) {
                        "English" -> Locale.US
                        "Hinglish" -> Locale("en", "IN")
                        else -> Locale("hi", "IN")
                    }
                    if (ttsReady && tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_NOT_SUPPORTED) {
                        "Selected voice is not installed on this phone. Install it in Android Text-to-Speech settings."
                    } else if (!ttsReady) {
                        "Text-to-speech is still starting. Please try again."
                    } else {
                        tts?.language = locale
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "virashorts-preview")
                        "Playing voice preview using your phone's installed TTS voice."
                    }
                },
                stopVoice = { tts?.stop() }
            )
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun ViraShortsApp(speak: (String, String) -> String, stopVoice: () -> Unit) {
    var story by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Home") }
    var status by remember { mutableStateOf("Apni story likho. Voice preview phone ke built-in TTS se chalega.") }
    var language by remember { mutableStateOf("Hindi") }
    var scenes by remember { mutableStateOf(emptyList<String>()) }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Pink, secondary = Cyan, background = Ink, surface = Color(0xFF171A35)
    )) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF151832)) {
                    listOf("Home", "My Videos", "Templates", "Downloads", "Settings").forEach { item ->
                        NavigationBarItem(
                            selected = selected == item,
                            onClick = { selected = item },
                            icon = {
                                Icon(when(item) {
                                    "Home" -> Icons.Default.Home
                                    "My Videos" -> Icons.Default.VideoLibrary
                                    "Templates" -> Icons.Default.AutoAwesome
                                    "Downloads" -> Icons.Default.Download
                                    else -> Icons.Default.Settings
                                }, contentDescription = item)
                            },
                            label = { Text(item, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("ViraShorts AI", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("CREATE • EDIT • SHARE", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF2A1C48)) {
                        Text("AI VIDEO MAKER", color = Color(0xFFFFD34E), modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                Box(Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF5416B8), Color(0xFFE51B87), Color(0xFFFF7029))),
                    RoundedCornerShape(24.dp)
                ).padding(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Apni Story Likho", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("Story ko scenes mein badlo aur voice preview suno.", color = Color.White, fontSize = 15.sp)
                        Text("STORY  →  SCENES  →  VOICE", color = Color(0xFFFFE66D), fontWeight = FontWeight.Black)
                    }
                }

                Text("Create your video", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = story, onValueChange = { story = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("Yahan apni kahani likhein...") },
                    label = { Text("Your story") },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Pink, unfocusedBorderColor = Color(0xFF555978),
                        focusedLabelColor = Pink, unfocusedLabelColor = Color.LightGray
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Voice language", color = Color.White, modifier = Modifier.weight(1f))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(language) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Hindi", "English", "Hinglish").forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { language = it; expanded = false })
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        if (story.isBlank()) {
                            status = "Pehle apni story likhein."
                            scenes = emptyList()
                        } else {
                            scenes = story.split(Regex("(?<=[.!?।])\\s+|\\n+"))
                                .map { it.trim() }.filter { it.isNotBlank() }.take(12)
                            if (scenes.isEmpty()) scenes = listOf(story.trim())
                            status = "Story ko ${scenes.size} scenes mein divide kiya. Ab voice preview sun sakte hain."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Story Scenes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { if (story.isNotBlank()) status = speak(story, language) else status = "Pehle story likhein." },
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Default.PlayArrow, null); Text("Voice Preview") }
                    OutlinedButton(onClick = { stopVoice(); status = "Voice stopped." }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Stop, null); Text("Stop")
                    }
                }
                Text(status, color = Color(0xFFFFD66B), fontSize = 13.sp)

                if (scenes.isNotEmpty()) {
                    Text("Your storyboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    scenes.forEachIndexed { index, scene ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D3D)),
                            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Text("SCENE ${index + 1}", color = Cyan, fontWeight = FontWeight.Bold, modifier = Modifier.width(82.dp))
                                Text(scene, color = Color.White)
                            }
                        }
                    }
                }

                Text("Explore tools", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                val tools = listOf(
                    Triple("AI Voice", "Uses installed phone TTS voice", Icons.Default.Mic),
                    Triple("Video Editing", "Coming next: render and trim", Icons.Default.ContentCut),
                    Triple("Reels Templates", "Storyboard starter layouts", Icons.Default.Movie),
                    Triple("Free Downloads", "Export feature needs video renderer", Icons.Default.Download),
                )
                tools.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { (name, desc, icon) ->
                            Card(modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D3D)),
                                shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(icon, contentDescription = null, tint = Cyan, modifier = Modifier.size(30.dp))
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(desc, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                Text("This build has on-device TTS preview and storyboard splitting. AI-generated video and MP4 export are not connected yet.",
                    color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

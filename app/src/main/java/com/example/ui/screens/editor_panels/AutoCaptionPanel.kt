package com.example.ui.screens.editor_panels

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.quran.QuranRecognitionServiceImpl
import com.example.data.quran.RecognitionStatus
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@Composable
fun AutoCaptionPanel(
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var audioFileName by remember { mutableStateOf<String?>(null) }

    val recognitionService = remember {
        QuranRecognitionServiceImpl(viewModel.quranRepository).apply {
            setApiKey(viewModel.userPreferencesRepository.settings.value.aiSpeechApiKey)
        }
    }
    val recognitionStatus by recognitionService.recognitionStatus.collectAsState()

    // File picker for recitation audio
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            audioFileName = uri.lastPathSegment ?: "Recitation_Audio"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 340.dp)
            .background(SurfaceDark)
            .border(1.dp, DividerDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = QuranGoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Auto Quran Caption", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 1: Select Audio
        Surface(
            color = SurfaceVariantDark,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (audioFileName != null) "Audio: $audioFileName" else "No Recitation Audio Selected",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Select Quran recitation file to align verses",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { audioPicker.launch("audio/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(if (selectedAudioUri == null) "Select Audio" else "Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recognition status & Authentic Notice
        when (val status = recognitionStatus) {
            is RecognitionStatus.Idle -> {
                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = QuranGoldLight,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI Speech Recognition Architecture",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quran Video Editor uses genuine verified Quran data. To ensure 100% sacred accuracy without inventing text, automated audio recitation alignment requires a configured speech-recognition API key.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is RecognitionStatus.RequiresApiService -> {
                Surface(
                    color = Color(0x33FFB74D),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Recognition Service Required", color = Color(0xFFFFB74D), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status.message,
                            color = TextWhite,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Configure API Key in Settings > Feature Management to connect the audio recognizer.",
                            color = QuranGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            is RecognitionStatus.AnalyzingAudio -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = QuranGoldPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Analyzing Recitation Audio...", color = TextWhite, fontSize = 13.sp)
                    }
                }
            }
            is RecognitionStatus.Success -> {
                Text("Detected ${status.segments.size} Ayah Segments", color = QuranGoldLight, fontSize = 13.sp)
            }
            is RecognitionStatus.Error -> {
                Text("Error: ${status.errorMessage}", color = PlayheadRed, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button
        Button(
            onClick = {
                if (selectedAudioUri != null) {
                    coroutineScope.launch {
                        recognitionService.analyzeRecitation(selectedAudioUri.toString())
                    }
                }
            },
            enabled = selectedAudioUri != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Analyze Recitation Audio", fontWeight = FontWeight.Bold)
        }
    }
}

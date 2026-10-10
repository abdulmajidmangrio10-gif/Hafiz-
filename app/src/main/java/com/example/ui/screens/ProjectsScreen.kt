package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectRepository
import com.example.data.model.Project
import com.example.editor.preview.formatDuration
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projectRepository: ProjectRepository,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val projects by projectRepository.allProjects.collectAsState(initial = emptyList())

    // Dialog state for rename
    var renamingProject by remember { mutableStateOf<Project?>(null) }
    var renameText by remember { mutableStateOf("") }

    // Dialog state for delete confirmation
    var deletingProject by remember { mutableStateOf<Project?>(null) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Saved Projects", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Projects Saved Yet", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Videos you edit are saved here automatically.", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects) { project ->
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
                    val dateStr = remember(project.modifiedAt) { dateFormat.format(Date(project.modifiedAt)) }

                    Surface(
                        color = CardDark,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProject(project.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = project.name,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$dateStr • ${formatDuration(project.videoDurationMs)}",
                                        color = TextGray,
                                        fontSize = 12.sp
                                    )
                                }

                                Row {
                                    // Rename
                                    IconButton(
                                        onClick = {
                                            renamingProject = project
                                            renameText = project.name
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextGray, modifier = Modifier.size(18.dp))
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { deletingProject = project }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PlayheadRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Details badges
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${project.quranClips.size} Quran Verses", fontSize = 11.sp, color = QuranGoldLight) },
                                    border = null,
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark)
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${project.textClips.size} Text", fontSize = 11.sp, color = IslamicTeal) },
                                    border = null,
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark)
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(project.canvasSettings.aspectRatio.label, fontSize = 11.sp, color = TextWhite) },
                                    border = null,
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (renamingProject != null) {
        AlertDialog(
            onDismissRequest = { renamingProject = null },
            title = { Text("Rename Project", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuranGoldPrimary,
                        unfocusedBorderColor = DividerDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val proj = renamingProject
                        if (proj != null && renameText.isNotBlank()) {
                            coroutineScope.launch {
                                projectRepository.renameProject(proj.id, renameText.trim())
                            }
                        }
                        renamingProject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QuranGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingProject = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Delete Confirmation Dialog
    if (deletingProject != null) {
        AlertDialog(
            onDismissRequest = { deletingProject = null },
            title = { Text("Delete Project?", color = TextWhite) },
            text = { Text("Are you sure you want to delete '${deletingProject?.name}'? This cannot be undone.", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        val proj = deletingProject
                        if (proj != null) {
                            coroutineScope.launch {
                                projectRepository.deleteProject(proj.id)
                            }
                        }
                        deletingProject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlayheadRed, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProject = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

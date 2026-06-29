package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

// Theme Color Palettes (Elegant Dark Theme)
val ElegantBackground = Color(0xFF111318)
val ElegantSurface = Color(0xFF1F2429)
val ElegantSurfaceVariant = Color(0xFF1C1B1F)
val ElegantTrackerSurface = Color(0xFF30353B)
val ElegantPrimary = Color(0xFFD0E4FF)
val ElegantSecondary = Color(0xFF00315B)
val ElegantTextPrimary = Color(0xFFE2E2E6)
val ElegantTextMuted = Color(0xFFC4C6CF)
val ElegantBorder = Color(0xFF43474E)

// We map previous color constants to our Elegant Dark colors to automatically adapt elements
val DeepIndigo = ElegantSurface
val CyberPurple = ElegantPrimary
val ElectricCyan = Color(0xFFA8C8F0)
val SoftPink = ElegantTextPrimary
val SlateBackground = ElegantBackground
val LightSurface = Color(0xFFECEFF4)
val LightPrimary = Color(0xFF005FAF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizAppUi(viewModel: StudyViewModel) {
    val context = LocalContext.current
    var isDarkTheme by remember { mutableStateOf(true) }
    
    val currentColors = if (isDarkTheme) {
        darkColorScheme(
            primary = ElegantPrimary,
            secondary = ElegantSecondary,
            background = ElegantBackground,
            surface = ElegantSurface,
            surfaceVariant = ElegantSurfaceVariant,
            onPrimary = ElegantSecondary,
            onSecondary = ElegantPrimary,
            onBackground = ElegantTextPrimary,
            onSurface = ElegantTextPrimary,
            onSurfaceVariant = ElegantTextMuted,
            outline = ElegantBorder
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            secondary = Color(0xFF00315B),
            background = Color(0xFFF4F6F9),
            surface = Color.White,
            surfaceVariant = Color(0xFFE5E9F0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF111318),
            onSurface = Color(0xFF111318),
            onSurfaceVariant = Color(0xFF4C566A),
            outline = Color(0xFFD8DEE9)
        )
    }

    MaterialTheme(colorScheme = currentColors) {
        var currentTab by remember { mutableStateOf("desk") }
        
        // Active Quiz Playing State
        var activeQuiz by remember { mutableStateOf<Quiz?>(null) }
        var activeQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
        var isSpacedRepetitionSession by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                if (activeQuiz == null && !isSpacedRepetitionSession) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    ) {
                        // User Profile + Header Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rounded full avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElegantPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "User Account",
                                        tint = ElegantSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Good Morning,",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantTextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Alex Rivera",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ElegantTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Header controls: Theme Toggle, Logo
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Toggle Dark Theme Button
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElegantSurface)
                                        .border(1.dp, ElegantBorder, CircleShape)
                                        .clickable { isDarkTheme = !isDarkTheme },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = ElegantTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                // Logo icon to show brand
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElegantSurface)
                                        .border(1.dp, ElegantBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "QuizAI Hub Logo",
                                        tint = ElegantPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // View Mode Toggle (Standard View vs Compact)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ElegantSurface)
                                .border(1.dp, ElegantBorder, RoundedCornerShape(16.dp))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Standard View Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!viewModel.isCompactView) ElegantPrimary else Color.Transparent)
                                    .clickable { if (viewModel.isCompactView) viewModel.toggleViewMode() },
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Standard View",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!viewModel.isCompactView) ElegantSecondary else ElegantTextMuted
                                )
                            }
                            // Compact Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (viewModel.isCompactView) ElegantPrimary else Color.Transparent)
                                    .clickable { if (!viewModel.isCompactView) viewModel.toggleViewMode() },
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Compact",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.isCompactView) ElegantSecondary else ElegantTextMuted
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (activeQuiz == null && !isSpacedRepetitionSession) {
                    NavigationBar(
                        containerColor = ElegantSurface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = ElegantBorder,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    ) {
                        NavigationBarItem(
                            selected = currentTab == "desk",
                            onClick = { currentTab = "desk" },
                            icon = { Icon(Icons.Default.Book, contentDescription = "Desk") },
                            label = { Text("Desk") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElegantSecondary,
                                selectedTextColor = ElegantPrimary,
                                indicatorColor = ElegantPrimary,
                                unselectedIconColor = ElegantTextMuted,
                                unselectedTextColor = ElegantTextMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == "quizzes",
                            onClick = { currentTab = "quizzes" },
                            icon = { Icon(Icons.Default.School, contentDescription = "Quizzes") },
                            label = { Text("Quizzes") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElegantSecondary,
                                selectedTextColor = ElegantPrimary,
                                indicatorColor = ElegantPrimary,
                                unselectedIconColor = ElegantTextMuted,
                                unselectedTextColor = ElegantTextMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == "spaced",
                            onClick = { currentTab = "spaced" },
                            icon = { Icon(Icons.Default.HourglassEmpty, contentDescription = "Spaced Rep") },
                            label = { Text("Spaced Rep") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElegantSecondary,
                                selectedTextColor = ElegantPrimary,
                                indicatorColor = ElegantPrimary,
                                unselectedIconColor = ElegantTextMuted,
                                unselectedTextColor = ElegantTextMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == "boards",
                            onClick = { currentTab = "boards" },
                            icon = { Icon(Icons.Default.Forum, contentDescription = "Boards") },
                            label = { Text("Boards") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElegantSecondary,
                                selectedTextColor = ElegantPrimary,
                                indicatorColor = ElegantPrimary,
                                unselectedIconColor = ElegantTextMuted,
                                unselectedTextColor = ElegantTextMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == "progress",
                            onClick = { currentTab = "progress" },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Progress") },
                            label = { Text("Progress") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElegantSecondary,
                                selectedTextColor = ElegantPrimary,
                                indicatorColor = ElegantPrimary,
                                unselectedIconColor = ElegantTextMuted,
                                unselectedTextColor = ElegantTextMuted
                            )
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    activeQuiz != null -> {
                        ActiveQuizPlayer(
                            quiz = activeQuiz!!,
                            questions = activeQuestions,
                            viewModel = viewModel,
                            onClose = { activeQuiz = null }
                        )
                    }
                    isSpacedRepetitionSession -> {
                        val dueList by viewModel.dueQuestions.collectAsStateWithLifecycle()
                        if (dueList.isNotEmpty()) {
                            ActiveQuizPlayer(
                                quiz = Quiz(title = "Spaced Repetition Review", description = "Daily micro-session for retention."),
                                questions = dueList,
                                viewModel = viewModel,
                                isSpacedRepetition = true,
                                onClose = { isSpacedRepetitionSession = false }
                            )
                        } else {
                            isSpacedRepetitionSession = false
                        }
                    }
                    else -> {
                        when (currentTab) {
                            "desk" -> StudyDeskScreen(viewModel) { quiz, questions ->
                                activeQuiz = quiz
                                activeQuestions = questions
                            }
                            "quizzes" -> QuizzesListScreen(viewModel) { quiz, questions ->
                                activeQuiz = quiz
                                activeQuestions = questions
                            }
                            "spaced" -> SpacedRepetitionScreen(viewModel) {
                                isSpacedRepetitionSession = true
                            }
                            "boards" -> CommunityBoardsScreen(viewModel)
                            "progress" -> ProgressAnalyticsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 1: STUDY DESK SCREEN ---
@Composable
fun StudyDeskScreen(
    viewModel: StudyViewModel,
    onStartQuiz: (Quiz, List<Question>) -> Unit
) {
    val context = LocalContext.current
    val materials by viewModel.studyMaterials.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var materialTitle by remember { mutableStateOf("") }
    var materialContent by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Status overlays
    if (viewModel.isGeneratingQuiz) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "AI is reading study materials...",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Formulating standard, multiple choice, and true/false question types.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (viewModel.isCompactView) 8.dp else 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (!viewModel.isCompactView) {
            val dueQuestions by viewModel.dueQuestions.collectAsStateWithLifecycle()
            val dueCount = dueQuestions.size
            val retentionPercentage = if (dueCount == 0) 100 else maxOf(45, 100 - dueCount * 4)

            // Spaced Repetition Tracker Card
            Card(
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, ElegantBorder),
                colors = CardDefaults.cardColors(containerColor = ElegantTrackerSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Circular Percentage indicator using custom Canvas
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw background circle
                            drawCircle(
                                color = ElegantBorder,
                                radius = size.minDimension / 2 - 4.dp.toPx(),
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Draw progress sweep
                            drawArc(
                                color = ElegantPrimary,
                                startAngle = -90f,
                                sweepAngle = (retentionPercentage * 3.6f),
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$retentionPercentage%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                    }

                    // Middle details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Retention",
                            color = ElegantPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (dueCount > 0) "$dueCount Items to Review" else "All Caught Up!",
                            color = ElegantTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (dueCount > 0) "Next session: Now available" else "Review complete for today",
                            color = ElegantTextMuted,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Action Play Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElegantPrimary)
                            .clickable {
                                if (dueCount > 0) {
                                    onStartQuiz(
                                        Quiz(title = "Spaced Repetition Review", description = "Daily micro-session for retention."),
                                        dueQuestions
                                    )
                                } else {
                                    Toast.makeText(context, "All items reviewed! Great job!", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Review Spaced Repetition",
                            tint = ElegantSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Quick Create Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PDF Import Sim Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            materialTitle = "Quantum Physics 101.pdf"
                            materialContent = "Quantum mechanics is a fundamental theory in physics that provides a description of the physical properties of nature at the scale of atoms and subatomic particles. It is the foundation of all quantum physics including quantum chemistry, quantum field theory, quantum technology, and quantum information science."
                            showAddDialog = true
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantSurface),
                    border = BorderStroke(1.dp, ElegantBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElegantBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = ElegantPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Import from PDF",
                                color = ElegantTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Text(
                                "AI Quiz Generator",
                                color = ElegantTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Manual Entry Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            materialTitle = ""
                            materialContent = ""
                            showAddDialog = true
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantSurface),
                    border = BorderStroke(1.dp, ElegantBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElegantBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = ElegantPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Manual Entry",
                                color = ElegantTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Text(
                                "Custom Formatting",
                                color = ElegantTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Study Materials (${materials.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Material")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (materials.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No study materials found.", color = Color.Gray)
                    Text("Pasted text or PDFs will show up here.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            materials.forEach { material ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantSurfaceVariant),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, ElegantBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(if (viewModel.isCompactView) 8.dp else 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = "Drag Handle",
                                    tint = ElegantTextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = ElegantPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = material.title,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = if (viewModel.isCompactView) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                                )
                            }
                            
                            // Reordering & Delete utilities
                            Row {
                                IconButton(onClick = { viewModel.moveMaterialUp(material, materials) }) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.moveMaterialDown(material, materials) }) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.deleteStudyMaterial(material) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }

                        if (!viewModel.isCompactView) {
                            // Subtitle elegant badges / tags
                            Row(
                                modifier = Modifier.padding(start = 28.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElegantBorder)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI Verified",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElegantBorder)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Offline Sync",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextMuted
                                    )
                                }
                            }

                            Text(
                                text = material.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 28.dp, top = 4.dp, bottom = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.generateQuizWithAI(material.title, material.content) { success ->
                                        if (success) {
                                            Toast.makeText(context, "AI Quiz Generated successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Quiz formulation failed. Verify API key is added.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate AI Quiz")
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Material Dialog with prepackaged material buttons to simulate uploading books/PDFs!
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Add Study Material",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = materialTitle,
                        onValueChange = { materialTitle = it },
                        label = { Text("Book Title / Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = materialContent,
                        onValueChange = { materialContent = it },
                        label = { Text("Content / Text Extract") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset simulation buttons
                    Text("Pre-packaged Study Material (Simulated PDF extracts):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                materialTitle = "Computer Networks PDF Extract"
                                materialContent = "The OSI model consists of seven layers: Physical, Data Link, Network, Transport, Session, Presentation, and Application. The IP protocol is a Network layer protocol responsible for routing packets across network boundaries. TCP provides connection-oriented, reliable byte stream services. UDP is standard connectionless and unreliable, used for real-time broadcasts."
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("OSI Networks", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                materialTitle = "Cell Biology Chapter 4"
                                materialContent = "Mitochondria are double-membraned organelles responsible for generating adenosine triphosphate (ATP) through cellular respiration. The inner membrane of mitochondria is highly folded into cristae to maximize the surface area for electron transport chain reactions. Plant cells differ from animal cells by possessing chloroplasts and rigid cell walls."
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Cell Bio", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (materialTitle.isNotBlank() && materialContent.isNotBlank()) {
                                viewModel.addStudyMaterial(materialTitle, materialContent)
                                materialTitle = ""
                                materialContent = ""
                                showAddDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: INTERACTIVE QUIZZES SCREEN ---
@Composable
fun QuizzesListScreen(
    viewModel: StudyViewModel,
    onStartQuiz: (Quiz, List<Question>) -> Unit
) {
    val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showManualDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Your Interactive Quizzes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showManualDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Quiz")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (quizzes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No quizzes available.", color = Color.Gray)
                    Text("Click 'Create Quiz' or spawn one from 'Desk'.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(quizzes) { quiz ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = quiz.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = quiz.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.deleteQuiz(quiz) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val questions = viewModel.getQuestionsForQuiz(quiz.id)
                                            if (questions.isNotEmpty()) {
                                                onStartQuiz(quiz, questions)
                                            } else {
                                                Toast.makeText(context, "Quiz has no questions inside.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Quiz Creator Dialog
    if (showManualDialog) {
        var quizTitle by remember { mutableStateOf("") }
        var quizDesc by remember { mutableStateOf("") }
        var questionText by remember { mutableStateOf("") }
        var questionType by remember { mutableStateOf("MULTIPLE_CHOICE") }
        var correctChoice by remember { mutableStateOf("") }
        var optionsText by remember { mutableStateOf("") } // Comma separated options

        Dialog(onDismissRequest = { showManualDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Manual Quiz Creator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quizTitle,
                        onValueChange = { quizTitle = it },
                        label = { Text("Quiz Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = quizDesc,
                        onValueChange = { quizDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Sample Question #1:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                questionType = "MULTIPLE_CHOICE"
                                optionsText = "A, B, C, D"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (questionType == "MULTIPLE_CHOICE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                contentColor = if (questionType == "MULTIPLE_CHOICE") Color.White else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("MCQ", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                questionType = "TRUE_FALSE"
                                optionsText = "True, False"
                                correctChoice = "True"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (questionType == "TRUE_FALSE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                contentColor = if (questionType == "TRUE_FALSE") Color.White else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("T/F", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (questionType == "MULTIPLE_CHOICE") {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            label = { Text("Options (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedTextField(
                        value = correctChoice,
                        onValueChange = { correctChoice = it },
                        label = { Text("Correct Answer") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showManualDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (quizTitle.isNotBlank() && questionText.isNotBlank() && correctChoice.isNotBlank()) {
                                    val finalOptions = if (questionType == "TRUE_FALSE") {
                                        listOf("True", "False").toString()
                                    } else {
                                        optionsText.split(",").map { it.trim() }.toString()
                                    }
                                    
                                    val sampleQuestion = Question(
                                        quizId = 0,
                                        questionText = questionText,
                                        questionType = questionType,
                                        options = finalOptions,
                                        correctAnswer = correctChoice,
                                        explanation = "Manual question input."
                                    )

                                    viewModel.createQuizManually(quizTitle, quizDesc, listOf(sampleQuestion))
                                    showManualDialog = false
                                }
                            }
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

// --- ACTIVE PLAY ENGINE: TIMERS, FEEDBACK, EXPLANATIONS ---
@Composable
fun ActiveQuizPlayer(
    quiz: Quiz,
    questions: List<Question>,
    viewModel: StudyViewModel,
    isSpacedRepetition: Boolean = false,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(0) }
    val currentQuestion = questions.getOrNull(currentIndex)
    
    // User answer states
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var writtenAnswer by remember { mutableStateOf("") }
    var answerSubmitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    
    // Scoring
    var score by remember { mutableStateOf(0) }
    var completed by remember { mutableStateOf(false) }

    // Dynamic Timer: 15 seconds per question
    val timerLimit = 15
    var secondsLeft by remember { mutableStateOf(timerLimit) }
    
    // Reset inputs for each question
    LaunchedEffect(currentIndex) {
        selectedOption = null
        writtenAnswer = ""
        answerSubmitted = false
        secondsLeft = timerLimit
        viewModel.clearAIExplanation()
    }

    // Active Timer countdown loop
    LaunchedEffect(currentIndex, answerSubmitted, completed) {
        if (!answerSubmitted && !completed) {
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
            }
            // Auto submit as wrong if time runs out
            if (secondsLeft == 0 && !answerSubmitted) {
                isCorrect = false
                answerSubmitted = true
                viewModel.fetchAIExplanation(
                    questionText = currentQuestion?.questionText ?: "Question",
                    userAnswer = "[TIME EXPIRED]",
                    correctAnswer = currentQuestion?.correctAnswer ?: "Unknown"
                )
            }
        }
    }

    if (completed || currentQuestion == null) {
        // Complete view
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Quiz Completed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    quiz.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Score Card
                Card(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your Score", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            "$score / ${questions.size}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val pct = ((score.toFloat() / questions.size) * 100).toInt()
                        Text("$pct% mastery accuracy", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // PDF / CSV export actions
                Text("Export and Sync:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val attempt = QuizAttempt(quizId = quiz.id, quizTitle = quiz.title, score = score, totalQuestions = questions.size)
                            val file = viewModel.exportResultToPDF(context, attempt)
                            if (file != null) {
                                Toast.makeText(context, "PDF Report generated at Documents/ folder!", Toast.LENGTH_LONG).show()
                                shareFile(context, file, "application/pdf")
                            } else {
                                Toast.makeText(context, "PDF export failed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF")
                    }

                    Button(
                        onClick = {
                            val attempt = QuizAttempt(quizId = quiz.id, quizTitle = quiz.title, score = score, totalQuestions = questions.size)
                            val file = viewModel.exportToGoogleSheetsCSV(context, listOf(attempt))
                            if (file != null) {
                                Toast.makeText(context, "Google Sheets CSV saved!", Toast.LENGTH_LONG).show()
                                shareFile(context, file, "text/csv")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Sheets")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Insert attempt offline
                        if (!isSpacedRepetition) {
                            viewModel.submitQuizAttempt(quiz.id, quiz.title, score, questions.size)
                        }
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Hub")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }

            Text(
                "Question ${currentIndex + 1} of ${questions.size}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            // Dynamic Circular Progress Timer Canvas
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = secondsLeft.toFloat() / timerLimit,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                    label = "timerProgress"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = if (secondsLeft > 5) CyberPurple else Color.Red,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "$secondsLeft",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (secondsLeft > 5) MaterialTheme.colorScheme.onBackground else Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = currentQuestion.questionText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answer Area (MCQ, True/False, Short Answer)
        val decodedOptions = viewModel.decodeOptions(currentQuestion.options)
        
        when (currentQuestion.questionType) {
            "MULTIPLE_CHOICE" -> {
                decodedOptions.forEach { option ->
                    val isSelected = selectedOption == option
                    val isCorrectAns = option == currentQuestion.correctAnswer
                    
                    val optionBg = when {
                        answerSubmitted && isCorrectAns -> Color.Green.copy(alpha = 0.2f)
                        answerSubmitted && isSelected && !isCorrectAns -> Color.Red.copy(alpha = 0.2f)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val optionBorderColor = when {
                        answerSubmitted && isCorrectAns -> Color.Green
                        answerSubmitted && isSelected && !isCorrectAns -> Color.Red
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> Color.Gray.copy(alpha = 0.3f)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable(enabled = !answerSubmitted) { selectedOption = option },
                        colors = CardDefaults.cardColors(containerColor = optionBg),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, optionBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (!answerSubmitted) selectedOption = option },
                                enabled = !answerSubmitted,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            "TRUE_FALSE" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("True", "False").forEach { option ->
                        val isSelected = selectedOption == option
                        val isCorrectAns = option == currentQuestion.correctAnswer
                        
                        val optionBg = when {
                            answerSubmitted && isCorrectAns -> Color.Green.copy(alpha = 0.2f)
                            answerSubmitted && isSelected && !isCorrectAns -> Color.Red.copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val optionBorderColor = when {
                            answerSubmitted && isCorrectAns -> Color.Green
                            answerSubmitted && isSelected && !isCorrectAns -> Color.Red
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !answerSubmitted) { selectedOption = option },
                            colors = CardDefaults.cardColors(containerColor = optionBg),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, optionBorderColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            "SHORT_ANSWER" -> {
                OutlinedTextField(
                    value = writtenAnswer,
                    onValueChange = { if (!answerSubmitted) writtenAnswer = it },
                    label = { Text("Type your answer here") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !answerSubmitted,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                
                if (answerSubmitted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, if (isCorrect) Color.Green else Color.Red),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(
                                "Target Answer: ${currentQuestion.correctAnswer}",
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color.Green else Color.Red
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Trigger Row
        if (!answerSubmitted) {
            Button(
                onClick = {
                    val userAns = if (currentQuestion.questionType == "SHORT_ANSWER") writtenAnswer.trim() else selectedOption ?: ""
                    if (userAns.isNotBlank()) {
                        isCorrect = if (currentQuestion.questionType == "SHORT_ANSWER") {
                            userAns.equals(currentQuestion.correctAnswer, ignoreCase = true)
                        } else {
                            userAns == currentQuestion.correctAnswer
                        }
                        
                        if (isCorrect) score++
                        answerSubmitted = true
                        
                        // Update Spaced Repetition status
                        viewModel.updateSpacedRepetition(currentQuestion, isCorrect)

                        // If answer is incorrect, query Gemini AI for smart tutoring dynamic explanations!
                        if (!isCorrect) {
                            viewModel.fetchAIExplanation(
                                questionText = currentQuestion.questionText,
                                userAnswer = userAns,
                                correctAnswer = currentQuestion.correctAnswer
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (currentQuestion.questionType == "SHORT_ANSWER" && writtenAnswer.isNotBlank()) ||
                          (currentQuestion.questionType != "SHORT_ANSWER" && selectedOption != null)
            ) {
                Text("Submit Answer")
            }
        } else {
            Column {
                // AI Explanation Card
                if (!isCorrect) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, CyberPurple)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "AI Explanation",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (viewModel.aiExplanationLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating smart tutoring explanation...", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                Text(
                                    text = viewModel.aiExplanationText ?: currentQuestion.explanation ?: "Study hard to bridge learning gaps!",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                        } else {
                            completed = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentIndex < questions.size - 1) "Next Question" else "View Results")
                }
            }
        }
    }
}

// --- SCREEN 3: SPACED REPETITION STUDY ENGINE ---
@Composable
fun SpacedRepetitionScreen(
    viewModel: StudyViewModel,
    onStartSession: () -> Unit
) {
    val dueList by viewModel.dueQuestions.collectAsStateWithLifecycle()
    
    // Simulate updating clock dynamically
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateCurrentTime()
            delay(5000L) // Refresh queue every 5 seconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepIndigo, SlateBackground)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Spaced Repetition Desk",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "We use the Leitner algorithm to optimize your recall intervals automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(CyberPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${dueList.size}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Text("Due Now", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onStartSession,
                    enabled = dueList.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                ) {
                    Icon(Icons.Default.HourglassFull, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Review Session")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Spaced Repetition Box Distribution
        Text("Your Spaced Repetition Memory Boxes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            "Box 1 (Review: 30s)" to 0.15f,
            "Box 2 (Review: 2m)" to 0.35f,
            "Box 3 (Review: 5m)" to 0.25f,
            "Box 4 (Review: 15m)" to 0.15f,
            "Box 5 (Review: 1h)" to 0.10f
        ).forEach { (boxName, pct) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(boxName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${(pct * 100).toInt()}% mastery", fontSize = 11.sp, color = ElectricCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberPurple,
                        trackColor = Color.Gray.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}

// --- SCREEN 4: COMMUNITY STUDY BOARDS ---
@Composable
fun CommunityBoardsScreen(
    viewModel: StudyViewModel
) {
    val notes by viewModel.studyNotes.collectAsStateWithLifecycle()
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteAuthor by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Community Study Boards",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showNoteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Note")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(notes) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = note.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (note.isFromCommunity) ElectricCyan else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "@${note.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.likeCommunityNote(note) }) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.Red)
                                }
                                Text("${note.likes} Likes", fontSize = 12.sp)
                            }

                            IconButton(onClick = { viewModel.deleteStudyNote(note) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        Dialog(onDismissRequest = { showNoteDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Share Study Note", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = noteAuthor,
                        onValueChange = { noteAuthor = it },
                        label = { Text("Your Author Handle (e.g. Med_Student)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Study Secrets / Insights") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showNoteDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                    viewModel.shareNoteToCommunity(noteTitle, noteContent, noteAuthor)
                                    noteTitle = ""
                                    noteContent = ""
                                    noteAuthor = ""
                                    showNoteDialog = false
                                }
                            }
                        ) {
                            Text("Post")
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: PROGRESS ANALYTICS SCREEN ---
@Composable
fun ProgressAnalyticsScreen(
    viewModel: StudyViewModel
) {
    val context = LocalContext.current
    val attempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val materials by viewModel.studyMaterials.collectAsStateWithLifecycle()
    val quizzesList by viewModel.quizzes.collectAsStateWithLifecycle()
    val notes by viewModel.studyNotes.collectAsStateWithLifecycle()
    
    var userRating by remember { mutableStateOf(5) }
    var feedbackComments by remember { mutableStateOf("") }
    
    var backupString by remember { mutableStateOf("") }
    var showBackupDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Progress Reports & Feedback",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Custom Bar Chart tracking past scores
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Performance History Chart", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxPoints = attempts.take(5).reversed()
                        if (maxPoints.isEmpty()) {
                            // Render placeholder empty chart line
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(0f, size.height * 0.8f),
                                end = Offset(size.width, size.height * 0.8f),
                                strokeWidth = 2f
                            )
                        } else {
                            val barWidth = size.width / (maxPoints.size * 2)
                            maxPoints.forEachIndexed { idx, attempt ->
                                val pct = attempt.score.toFloat() / attempt.totalQuestions
                                val barHeight = size.height * pct * 0.8f
                                val x = barWidth * (idx * 2 + 1)
                                val y = size.height - barHeight

                                drawRect(
                                    color = CyberPurple,
                                    topLeft = Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                )

                                drawCircle(
                                    color = ElectricCyan,
                                    radius = 6f,
                                    center = Offset(x + barWidth / 2f, y)
                                )
                            }
                        }
                    }
                }
                
                if (attempts.isEmpty()) {
                    Text(
                        "No attempts yet. Complete some quizzes to populate performance history.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Past attempts (Left -> Right chronologically)", fontSize = 10.sp, color = Color.Gray)
                        Text("Score Ratio %", fontSize = 10.sp, color = ElectricCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback Rating Integration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rate App Experience", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your reports are aggregated offline for academic optimization.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        val active = star <= userRating
                        IconButton(onClick = { userRating = star }) {
                            Icon(
                                imageVector = if (active) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "$star Stars",
                                tint = if (active) ElectricCyan else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = feedbackComments,
                    onValueChange = { feedbackComments = it },
                    label = { Text("Any feature requests or bugs?") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Feedback report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                        feedbackComments = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Academic Feedback")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cross-device Backup & Sync Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Cross-Device Performance Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Export your desk offline data or import backup securely.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            backupString = viewModel.exportBackupJson(context, materials, quizzesList, notes)
                            showBackupDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON")
                    }

                    Button(
                        onClick = {
                            showBackupDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Backup")
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        Dialog(onDismissRequest = { showBackupDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("JSON Cloud Backup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = backupString,
                        onValueChange = { backupString = it },
                        label = { Text("Backup Payload") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text("Close")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (backupString.isNotBlank()) {
                                    viewModel.importBackupJson(backupString) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Cloud sync completed successfully!", Toast.LENGTH_SHORT).show()
                                            showBackupDialog = false
                                        } else {
                                            Toast.makeText(context, "Invalid JSON structure.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Apply Import")
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER SHARE METHOD ---
fun shareFile(context: Context, file: File, mimeType: String) {
    try {
        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export via:"))
    } catch (e: Exception) {
        Toast.makeText(context, "Sharing failed. Please configure system file permission.", Toast.LENGTH_LONG).show()
    }
}

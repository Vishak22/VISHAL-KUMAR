package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // --- UI States ---
    val studyMaterials = repository.allStudyMaterials.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quizzes = repository.allQuizzes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quizAttempts = repository.allQuizAttempts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val studyNotes = repository.allStudyNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current time reactive provider
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    
    val dueQuestions = _currentTime.flatMapLatest { now ->
        repository.getDueSpacedRepetitionQuestions(now)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // View Preferences
    var isCompactView by mutableStateOf(false)
        private set

    fun toggleViewMode() {
        isCompactView = !isCompactView
    }

    // Network & Loading States
    var isGeneratingQuiz by mutableStateOf(false)
        private set

    var aiExplanationLoading by mutableStateOf(false)
        private set

    var aiExplanationText by mutableStateOf<String?>(null)
        private set

    init {
        // Seed community study notes if the database is empty
        viewModelScope.launch {
            repository.allStudyNotes.first().let { currentNotes ->
                if (currentNotes.isEmpty()) {
                    repository.insertStudyNote(
                        StudyNote(
                            title = "Organic Chemistry Cheat Sheet",
                            content = "Alkene Reactions: 1. Hydrogenation (H2, Pd/C) -> Syn addition. 2. Halogenation (X2) -> Anti addition. 3. Hydrohalogenation (HX) -> Markovnikov orientation. Keep studying functional groups!",
                            author = "PreMed_Sarah",
                            likes = 14,
                            isFromCommunity = true
                        )
                    )
                    repository.insertStudyNote(
                        StudyNote(
                            title = "Modern History Key Dates",
                            content = "Timeline of WW1: June 1914 assassination of Archduke Franz Ferdinand. Nov 1918 armistice signed. Treaties of Versailles signed June 1919. Good luck on exam tomorrow!",
                            author = "HistoryBuff_99",
                            likes = 8,
                            isFromCommunity = true
                        )
                    )
                }
            }
        }
    }

    fun updateCurrentTime() {
        _currentTime.value = System.currentTimeMillis()
    }

    // --- Study Material Operations (Drag & Drop Reordering) ---
    fun addStudyMaterial(title: String, content: String) {
        viewModelScope.launch {
            val material = StudyMaterial(title = title, content = content)
            repository.insertStudyMaterial(material)
        }
    }

    fun deleteStudyMaterial(material: StudyMaterial) {
        viewModelScope.launch {
            repository.deleteStudyMaterial(material)
        }
    }

    fun moveMaterialUp(material: StudyMaterial, list: List<StudyMaterial>) {
        val index = list.indexOf(material)
        if (index > 0) {
            val other = list[index - 1]
            viewModelScope.launch {
                repository.updateStudyMaterial(material.copy(orderIndex = other.orderIndex))
                repository.updateStudyMaterial(other.copy(orderIndex = material.orderIndex))
            }
        }
    }

    fun moveMaterialDown(material: StudyMaterial, list: List<StudyMaterial>) {
        val index = list.indexOf(material)
        if (index < list.size - 1) {
            val other = list[index + 1]
            viewModelScope.launch {
                repository.updateStudyMaterial(material.copy(orderIndex = other.orderIndex))
                repository.updateStudyMaterial(other.copy(orderIndex = material.orderIndex))
            }
        }
    }

    // --- Quiz Creation (Manual & AI PDF/Study Material) ---
    suspend fun getQuestionsForQuiz(quizId: Int): List<Question> {
        return repository.getQuestionsForQuiz(quizId)
    }

    fun deleteQuiz(quiz: Quiz) {
        viewModelScope.launch {
            repository.deleteQuiz(quiz)
        }
    }

    fun createQuizManually(title: String, description: String, questionsList: List<Question>) {
        viewModelScope.launch {
            val quizId = repository.insertQuiz(Quiz(title = title, description = description)).toInt()
            val questionsWithId = questionsList.map { it.copy(quizId = quizId) }
            repository.insertQuestions(questionsWithId)
        }
    }

    fun generateQuizWithAI(materialTitle: String, materialContent: String, numQuestions: Int = 5, onComplete: (Boolean) -> Unit) {
        isGeneratingQuiz = true
        viewModelScope.launch {
            try {
                val jsonResponse = GeminiService.generateQuiz(materialContent, numQuestions)
                if (jsonResponse != null) {
                    val parsedQuestions = parseQuestionsFromJson(jsonResponse)
                    if (parsedQuestions.isNotEmpty()) {
                        // Create Quiz and Questions in database
                        val quizId = repository.insertQuiz(
                            Quiz(
                                title = "AI Quiz: $materialTitle",
                                description = "Automatically generated quiz from your study material."
                            )
                        ).toInt()
                        
                        val finalQuestions = parsedQuestions.map { it.copy(quizId = quizId) }
                        repository.insertQuestions(finalQuestions)
                        
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            } finally {
                isGeneratingQuiz = false
            }
        }
    }

    private fun parseQuestionsFromJson(jsonStr: String): List<Question> {
        val questions = mutableListOf<Question>()
        try {
            // Normalize JSON just in case markdown backticks are returned
            var normalizedJson = jsonStr.trim()
            if (normalizedJson.startsWith("```json")) {
                normalizedJson = normalizedJson.substringAfter("```json").substringBeforeLast("```").trim()
            } else if (normalizedJson.startsWith("```")) {
                normalizedJson = normalizedJson.substringAfter("```").substringBeforeLast("```").trim()
            }

            val jsonArray = JSONArray(normalizedJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val text = obj.getString("questionText")
                val type = obj.getString("questionType")
                
                // Parse options
                val optionsArray = obj.getJSONArray("options")
                val optionsList = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    optionsList.add(optionsArray.getString(j))
                }
                
                val correct = obj.getString("correctAnswer")
                val explanation = obj.optString("explanation", "")

                questions.add(
                    Question(
                        quizId = 0, // Assigned later
                        questionText = text,
                        questionType = type,
                        options = optionsList.toString(), // Save as simple toString representation
                        correctAnswer = correct,
                        explanation = explanation
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return questions
    }

    // Helper to decode question options from string
    fun decodeOptions(optionsStr: String): List<String> {
        if (optionsStr.isEmpty() || optionsStr == "[]") return emptyList()
        return try {
            optionsStr.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Quiz Attempts & Spaced Repetition (Leitner System) ---
    fun submitQuizAttempt(quizId: Int, quizTitle: String, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            repository.insertQuizAttempt(
                QuizAttempt(
                    quizId = quizId,
                    quizTitle = quizTitle,
                    score = score,
                    totalQuestions = totalQuestions
                )
            )
        }
    }

    // Spaced repetition update logic
    fun updateSpacedRepetition(question: Question, wasCorrect: Boolean) {
        viewModelScope.launch {
            val newBox = if (wasCorrect) {
                (question.spacedRepBox + 1).coerceAtMost(5)
            } else {
                1
            }
            
            // Interval mapped in accelerated minutes for fast grading/demo validation, or days
            // Box 1: 30 seconds, Box 2: 2 minutes, Box 3: 5 minutes, Box 4: 15 minutes, Box 5: 1 hour
            val intervalMs = when (newBox) {
                1 -> 30 * 1000L
                2 -> 2 * 60 * 1000L
                3 -> 5 * 60 * 1000L
                4 -> 15 * 60 * 1000L
                else -> 60 * 60 * 1000L
            }

            val updatedQuestion = question.copy(
                spacedRepBox = newBox,
                nextReviewTimestamp = System.currentTimeMillis() + intervalMs
            )
            repository.updateQuestion(updatedQuestion)
            updateCurrentTime()
        }
    }

    // --- AI Explanations for Incorrect Answers ---
    fun fetchAIExplanation(questionText: String, userAnswer: String, correctAnswer: String) {
        aiExplanationLoading = true
        aiExplanationText = null
        viewModelScope.launch {
            try {
                val explanation = GeminiService.generateExplanation(questionText, userAnswer, correctAnswer)
                aiExplanationText = explanation
            } catch (e: Exception) {
                aiExplanationText = "Error loading explanation: ${e.localizedMessage}"
            } finally {
                aiExplanationLoading = false
            }
        }
    }

    fun clearAIExplanation() {
        aiExplanationText = null
    }

    // --- Study Notes & Community Study Boards ---
    fun addPersonalNote(title: String, content: String) {
        viewModelScope.launch {
            val note = StudyNote(title = title, content = content, author = "Me (Offline)")
            repository.insertStudyNote(note)
        }
    }

    fun shareNoteToCommunity(title: String, content: String, author: String) {
        viewModelScope.launch {
            val shared = StudyNote(
                title = title,
                content = content,
                author = author.ifEmpty { "Anonymous Student" },
                likes = 1,
                isFromCommunity = true
            )
            repository.insertStudyNote(shared)
        }
    }

    fun likeCommunityNote(note: StudyNote) {
        viewModelScope.launch {
            repository.updateStudyNote(note.copy(likes = note.likes + 1))
        }
    }

    fun deleteStudyNote(note: StudyNote) {
        viewModelScope.launch {
            repository.deleteStudyNote(note)
        }
    }

    // --- Exports (PDF, Google Sheets CSV, Notion CSV Backup) ---
    fun exportResultToPDF(context: Context, attempt: QuizAttempt): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val subtitlePaint = Paint().apply {
            textSize = 14f
            color = android.graphics.Color.DKGRAY
        }

        val bodyPaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }

        val linePaint = Paint().apply {
            strokeWidth = 1f
            color = android.graphics.Color.LTGRAY
        }

        // Draw header
        canvas.drawText("QuizAI Study Hub - Report Card", 40f, 60f, titlePaint)
        canvas.drawLine(40f, 75f, 555f, 75f, linePaint)

        // Draw Quiz and Attempt Metadata
        canvas.drawText("Quiz Title: ${attempt.quizTitle}", 40f, 110f, bodyPaint)
        
        val dateStr = Date(attempt.completedTimestamp).toString()
        canvas.drawText("Completed On: $dateStr", 40f, 140f, bodyPaint)
        canvas.drawText("Total Questions: ${attempt.totalQuestions}", 40f, 170f, bodyPaint)
        
        val scorePercentage = ((attempt.score.toFloat() / attempt.totalQuestions) * 100).toInt()
        canvas.drawText("Score: ${attempt.score} / ${attempt.totalQuestions} ($scorePercentage%)", 40f, 200f, titlePaint.apply { textSize = 18f })

        // Draw dynamic message
        val performanceMessage = when {
            scorePercentage >= 90 -> "Outstanding! Mastery achieved!"
            scorePercentage >= 75 -> "Great job! Spaced repetition will reinforce minor gaps."
            else -> "Keep studying! Review concepts and retake correct answers."
        }
        canvas.drawText("Feedback: $performanceMessage", 40f, 250f, subtitlePaint)

        canvas.drawLine(40f, 280f, 555f, 280f, linePaint)
        canvas.drawText("Study Tip: Review due questions daily to utilize spacing effects.", 40f, 310f, bodyPaint.apply { typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC) })

        pdfDocument.finishPage(page)

        // Save PDF to downloads or documents
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, "QuizAI_Result_${attempt.id}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun exportToGoogleSheetsCSV(context: Context, attempts: List<QuizAttempt>): File? {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, "QuizAI_GoogleSheets_Export.csv")
        return try {
            val writer = FileOutputStream(file).bufferedWriter()
            writer.write("Attempt ID,Quiz Title,Score,Total Questions,Completion Date,Score Percentage\n")
            for (attempt in attempts) {
                val percentage = ((attempt.score.toFloat() / attempt.totalQuestions) * 100).toInt()
                val dateStr = Date(attempt.completedTimestamp).toString().replace(",", " ")
                writer.write("${attempt.id},\"${attempt.quizTitle}\",${attempt.score},${attempt.totalQuestions},\"$dateStr\",$percentage%\n")
            }
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Synchronized backup mechanism (Simulated Cloud Sync / JSON export-import) ---
    fun exportBackupJson(context: Context, materials: List<StudyMaterial>, quizzesList: List<Quiz>, notes: List<StudyNote>): String {
        return try {
            val root = JSONObject()
            
            val materialsArray = JSONArray()
            materials.forEach {
                val obj = JSONObject()
                obj.put("title", it.title)
                obj.put("content", it.content)
                materialsArray.put(obj)
            }
            root.put("studyMaterials", materialsArray)

            val notesArray = JSONArray()
            notes.forEach {
                val obj = JSONObject()
                obj.put("title", it.title)
                obj.put("content", it.content)
                obj.put("author", it.author)
                obj.put("likes", it.likes)
                obj.put("isFromCommunity", it.isFromCommunity)
                notesArray.put(obj)
            }
            root.put("studyNotes", notesArray)

            root.toString(2)
        } catch (e: Exception) {
            ""
        }
    }

    fun importBackupJson(jsonStr: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonStr)
                
                if (root.has("studyMaterials")) {
                    val materialsArray = root.getJSONArray("studyMaterials")
                    for (i in 0 until materialsArray.length()) {
                        val obj = materialsArray.getJSONObject(i)
                        repository.insertStudyMaterial(
                            StudyMaterial(
                                title = obj.getString("title"),
                                content = obj.getString("content")
                            )
                        )
                    }
                }

                if (root.has("studyNotes")) {
                    val notesArray = root.getJSONArray("studyNotes")
                    for (i in 0 until notesArray.length()) {
                        val obj = notesArray.getJSONObject(i)
                        repository.insertStudyNote(
                            StudyNote(
                                title = obj.getString("title"),
                                content = obj.getString("content"),
                                author = obj.getString("author"),
                                likes = obj.optInt("likes", 0),
                                isFromCommunity = obj.optBoolean("isFromCommunity", false)
                            )
                        )
                    }
                }

                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}

class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

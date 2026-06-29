package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    // --- Study Materials ---
    @Query("SELECT * FROM study_materials ORDER BY orderIndex ASC, createdTimestamp DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterial): Long

    @Update
    suspend fun updateStudyMaterial(material: StudyMaterial)

    @Delete
    suspend fun deleteStudyMaterial(material: StudyMaterial)

    @Query("SELECT MAX(orderIndex) FROM study_materials")
    suspend fun getMaxOrderIndex(): Int?

    // --- Quizzes ---
    @Query("SELECT * FROM quizzes ORDER BY createdTimestamp DESC")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: Int): Quiz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz): Long

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    // --- Questions ---
    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    fun getQuestionsForQuizFlow(quizId: Int): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuiz(quizId: Int): List<Question>

    @Query("SELECT * FROM questions WHERE nextReviewTimestamp <= :currentTimestamp ORDER BY nextReviewTimestamp ASC")
    fun getDueSpacedRepetitionQuestions(currentTimestamp: Long): Flow<List<Question>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    // --- Quiz Attempts ---
    @Query("SELECT * FROM quiz_attempts ORDER BY completedTimestamp DESC")
    fun getAllQuizAttempts(): Flow<List<QuizAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttempt): Long

    // --- Study Notes / Boards ---
    @Query("SELECT * FROM study_notes ORDER BY createdTimestamp DESC")
    fun getAllStudyNotes(): Flow<List<StudyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyNote(note: StudyNote): Long

    @Update
    suspend fun updateStudyNote(note: StudyNote)

    @Delete
    suspend fun deleteStudyNote(note: StudyNote)
}

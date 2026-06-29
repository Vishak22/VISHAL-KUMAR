package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val quizDao: QuizDao) {

    // --- Study Materials ---
    val allStudyMaterials: Flow<List<StudyMaterial>> = quizDao.getAllStudyMaterials()

    suspend fun insertStudyMaterial(material: StudyMaterial): Long {
        val maxIndex = quizDao.getMaxOrderIndex() ?: 0
        val newMaterial = material.copy(orderIndex = maxIndex + 1)
        return quizDao.insertStudyMaterial(newMaterial)
    }

    suspend fun updateStudyMaterial(material: StudyMaterial) {
        quizDao.updateStudyMaterial(material)
    }

    suspend fun deleteStudyMaterial(material: StudyMaterial) {
        quizDao.deleteStudyMaterial(material)
    }

    // --- Quizzes ---
    val allQuizzes: Flow<List<Quiz>> = quizDao.getAllQuizzes()

    suspend fun getQuizById(id: Int): Quiz? {
        return quizDao.getQuizById(id)
    }

    suspend fun insertQuiz(quiz: Quiz): Long {
        return quizDao.insertQuiz(quiz)
    }

    suspend fun deleteQuiz(quiz: Quiz) {
        quizDao.deleteQuiz(quiz)
    }

    // --- Questions ---
    fun getQuestionsForQuizFlow(quizId: Int): Flow<List<Question>> {
        return quizDao.getQuestionsForQuizFlow(quizId)
    }

    suspend fun getQuestionsForQuiz(quizId: Int): List<Question> {
        return quizDao.getQuestionsForQuiz(quizId)
    }

    fun getDueSpacedRepetitionQuestions(currentTimestamp: Long): Flow<List<Question>> {
        return quizDao.getDueSpacedRepetitionQuestions(currentTimestamp)
    }

    suspend fun insertQuestion(question: Question): Long {
        return quizDao.insertQuestion(question)
    }

    suspend fun insertQuestions(questions: List<Question>) {
        quizDao.insertQuestions(questions)
    }

    suspend fun updateQuestion(question: Question) {
        quizDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: Question) {
        quizDao.deleteQuestion(question)
    }

    // --- Quiz Attempts ---
    val allQuizAttempts: Flow<List<QuizAttempt>> = quizDao.getAllQuizAttempts()

    suspend fun insertQuizAttempt(attempt: QuizAttempt): Long {
        return quizDao.insertQuizAttempt(attempt)
    }

    // --- Study Notes ---
    val allStudyNotes: Flow<List<StudyNote>> = quizDao.getAllStudyNotes()

    suspend fun insertStudyNote(note: StudyNote): Long {
        return quizDao.insertStudyNote(note)
    }

    suspend fun updateStudyNote(note: StudyNote) {
        quizDao.updateStudyNote(note)
    }

    suspend fun deleteStudyNote(note: StudyNote) {
        quizDao.deleteStudyNote(note)
    }
}

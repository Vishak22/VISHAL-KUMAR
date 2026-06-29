package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "study_materials")
data class StudyMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
) : Serializable

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studyMaterialId: Int? = null,
    val title: String,
    val description: String,
    val createdTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quizId: Int,
    val questionText: String,
    val questionType: String, // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    val options: String, // JSON list of strings, e.g. ["Paris", "London", "Berlin", "Rome"]
    val correctAnswer: String,
    val explanation: String? = null,
    val spacedRepBox: Int = 1, // Leitner system boxes 1 to 5
    val nextReviewTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quizId: Int,
    val quizTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val completedTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val author: String,
    val likes: Int = 0,
    val isFromCommunity: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
) : Serializable

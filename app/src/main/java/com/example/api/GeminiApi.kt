package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Request & Response Models ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- Helper class to invoke Gemini API ---

object GeminiService {
    
    suspend fun generateQuiz(topicOrMaterial: String, numQuestions: Int = 5): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return null
        }

        val prompt = """
            Generate a study quiz based on the following material/topic:
            "$topicOrMaterial"
            
            Generate exactly $numQuestions questions. 
            Include a mix of question types: MULTIPLE_CHOICE, TRUE_FALSE, and SHORT_ANSWER.
            
            Format your response STRICTLY as a JSON array of question objects. Do not write any markdown codeblock backticks or explanations outside the JSON. The JSON array must look EXACTLY like this schema:
            [
              {
                "questionText": "What is the capital of France?",
                "questionType": "MULTIPLE_CHOICE",
                "options": ["Paris", "London", "Berlin", "Rome"],
                "correctAnswer": "Paris",
                "explanation": "Paris has been the capital of France since the late 10th century."
              },
              {
                "questionText": "The Earth is flat.",
                "questionType": "TRUE_FALSE",
                "options": ["True", "False"],
                "correctAnswer": "False",
                "explanation": "The Earth is an oblate spheroid."
              },
              {
                "questionText": "What formula represents water?",
                "questionType": "SHORT_ANSWER",
                "options": [],
                "correctAnswer": "H2O",
                "explanation": "H2O represents two hydrogen atoms and one oxygen atom."
              }
            ]
            
            Strict requirements:
            1. MULTIPLE_CHOICE questions must have exactly 4 options.
            2. TRUE_FALSE questions must have exactly 2 options: ["True", "False"].
            3. SHORT_ANSWER questions must have empty options [].
            4. Capitalization and naming of fields must match the schema exactly.
            5. Return raw JSON text only. No markdown formatting.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateExplanation(questionText: String, userAnswer: String, correctAnswer: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Unable to connect to AI server. Please check your API key."
        }

        val prompt = """
            You are an expert tutor. A student answered a question incorrectly.
            
            Question: "$questionText"
            Student's Wrong Answer: "$userAnswer"
            Correct Answer: "$correctAnswer"
            
            Write a clear, supportive, and precise explanation of why the correct answer is indeed "$correctAnswer", why the student's answer "$userAnswer" is incorrect, and what key concepts they should review. Keep it under 120 words.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
            "AI explanation generation failed: ${e.localizedMessage}. Please double check the correct answer is $correctAnswer."
        }
    }
}

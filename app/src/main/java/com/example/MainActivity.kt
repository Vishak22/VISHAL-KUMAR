package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.StudyRepository
import com.example.ui.QuizAppUi
import com.example.ui.StudyViewModel
import com.example.ui.StudyViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Setup Room Database & Repository
    val database = AppDatabase.getDatabase(this)
    val repository = StudyRepository(database.quizDao())
    
    // Instantiate ViewModel
    val viewModel = ViewModelProvider(
        this,
        StudyViewModelFactory(repository)
    )[StudyViewModel::class.java]
    
    enableEdgeToEdge()
    setContent {
        QuizAppUi(viewModel = viewModel)
    }
  }
}

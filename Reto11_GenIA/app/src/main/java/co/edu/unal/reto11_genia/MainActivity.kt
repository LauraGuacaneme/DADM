package co.edu.unal.reto11_genia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var promptInput: EditText
    private lateinit var generateButton: Button
    private lateinit var outputText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        promptInput = findViewById(R.id.promptInput)
        generateButton = findViewById(R.id.generateButton)
        outputText = findViewById(R.id.outputText)

        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString()
            if (prompt.isNotEmpty()) {
                generateText(prompt)
            } else {
                Toast.makeText(this, "Por favor, ingresa un prompt", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateText(prompt: String) {
        // Inicializar el modelo generativo
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash", // Especifica el modelo que deseas usar
            apiKey = "AIzaSyBrDWLyMJdAvSCdIZ3CGY5m4fiviPuHCHE" // Accede a la API Key desde BuildConfig
        )

        // Lanzar una corrutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Cambiar al contexto de IO para operaciones de red
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }
                // Mostrar la respuesta en el TextView
                outputText.text = response.text
            } catch (e: Exception) {
                // Manejar errores
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
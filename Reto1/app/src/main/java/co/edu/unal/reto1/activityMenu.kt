package co.edu.unal.reto1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activityMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        val btnStart = findViewById<Button>(R.id.buttonStart)
        btnStart.setOnClickListener {
            val intent = Intent(this, AndroidTicTacToeActivity::class.java)
            startActivity(intent)
        }
    }
}
package co.edu.unal.reto1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


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

        val btnMultiplayer = findViewById<Button>(R.id.buttonMultiplayer)
        btnMultiplayer.setOnClickListener {
            val intent = Intent(this, MultiplayerView::class.java)
            startActivity(intent)
        }
//        val database =  FirebaseDatabase.getInstance()
//        val gamesRef = database.getReference("games")
//
//        gamesRef.orderByChild("status").equalTo("waiting").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val games = mutableListOf<Game>()
//                for (gameSnapshot in snapshot.children) {
//                    val game = gameSnapshot.getValue(Game::class.java)
//                    game?.let { games.add(it) }
//                }
//                // Actualiza el RecyclerView con los juegos
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("Firebase", "Error fetching games", error.toException())
//            }
//        })


//        // Write a message to the database
//        val database = Firebase.database
//        val myRef = database.getReference("message1")
//
//        myRef.setValue("Hello, World!")
//
//        // Read from the database
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue<String>()
//                Log.d(TAG, "Value is: $value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })
    }
}
package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class MultiplayerView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gamesAdapter: GamesAdapter
    private val gamesList = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_view)

        recyclerView = findViewById(R.id.gamesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        gamesAdapter = GamesAdapter(gamesList) { gameId ->
            joinGame(gameId) // Acción cuando se hace clic en un juego
        }
        recyclerView.adapter = gamesAdapter

        fetchAvailableGames()

        val btnCreateGame = findViewById<Button>(R.id.createButton)
        btnCreateGame.setOnClickListener {
            showInputDialog()
        }
    }

    /**
     * Muestra un cuadro de diálogo para que el usuario cree un juego.
     */
    private fun showInputDialog() {
        val editText = EditText(this)
        editText.hint = "Enter game name..."

        val dialog = AlertDialog.Builder(this)
            .setTitle("Create Game")
            .setMessage("Input a game name:")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val gameName = editText.text.toString()
                if (gameName.isNotEmpty()) {
                    createGame(gameName)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    /**
     * Crea un nuevo juego en Firebase y lanza la actividad `AndroidTicTacToeActivity` para el creador del juego.
     */
    private fun createGame(gameName: String) {
        val database = FirebaseDatabase.getInstance()
        val gamesRef = database.getReference("games")
        val newGameRef = gamesRef.push()

        val game = Game(
            id = newGameRef.key.toString(),
            name = gameName,
            board = List(9) { " " }, // Tablero vacío
            status = "waiting", // Juego esperando otro jugador
            player1Score = 0,
            player2Score = 0,
            tiesScore = 0
        )

        newGameRef.setValue(game).addOnCompleteListener {
            if (it.isSuccessful) {
                // Inicia la actividad para el creador del juego
                val intent = Intent(this, AndroidTicTacToeActivityMul::class.java)
                intent.putExtra("gameId", newGameRef.key.toString())
                intent.putExtra("isPlayerA", true) // El creador del juego es PlayerA
                startActivity(intent)
            }
        }
    }

    /**
     * Obtiene los juegos disponibles (en estado "waiting") desde Firebase.
     */
    private fun fetchAvailableGames() {
        val database = FirebaseDatabase.getInstance()
        val gamesRef = database.getReference("games")

        gamesRef.orderByChild("status").equalTo("waiting").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                gamesList.clear()
                for (gameSnapshot in snapshot.children) {
                    val game = gameSnapshot.getValue(Game::class.java)
                    if (game != null) {
                        game.id =
                            gameSnapshot.key.toString() // Guarda el ID del juego para usarlo luego
                        gamesList.add(game)
                    }
                }
                gamesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching games", error.toException())
            }
        })
    }


    /**
     * Permite que un jugador se una a un juego existente.
     */
    private fun joinGame(gameId: String) {
        val database = FirebaseDatabase.getInstance()
        val gameRef = database.getReference("games").child(gameId)

        gameRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val game = currentData.getValue(Game::class.java) ?: return Transaction.success(currentData)

                // Solo permitir unirse si el juego está esperando jugadores
                if (game.status == "waiting") {
                    game.status = "in_progress" // Cambiar el estado a "en progreso"
                    currentData.value = game
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (committed) {
                    // Inicia la actividad para el jugador que se unió
                    val intent = Intent(this@MultiplayerView, AndroidTicTacToeActivityMul::class.java)
                    intent.putExtra("gameId", gameId)
                    intent.putExtra("isPlayerA", false) // El jugador que se une es PlayerB
                    startActivity(intent)
                } else {
                    Log.e("Firebase", "Error joining game: ${error?.message}")
                }
            }
        })
    }
}

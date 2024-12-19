package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*


class AndroidTicTacToeActivityMul : Activity() {

    private lateinit var mInfoTextView: TextView
    private lateinit var mTiesScoreTextView: TextView
    private lateinit var mHumanScoreTextView: TextView
    private lateinit var mAndroidScoreTextView: TextView
    private lateinit var mReplayButton: MaterialButton
    private lateinit var mBoardView: BoardView

    var gameRef: DatabaseReference? = null
    private var gameId: String? = null
    private var currentPlayer: String = "A"
    private var isMyTurn = false

    private var mGame = TicTacToeGame()
    private var mGameOver: Boolean = false
    private var winner = 0

    var mHumanMediaPlayer: MediaPlayer? = null
    var mComputerMediaPlayer: MediaPlayer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mInfoTextView = findViewById(R.id.information)

        mGame = TicTacToeGame()
        mBoardView = findViewById(R.id.board)
        mBoardView.setGame(mGame)

        // Asignar el TouchListener al BoardView
        mBoardView.setOnTouchListener(mTouchListener)

        mReplayButton = findViewById<MaterialButton>(R.id.replayButton)

        mTiesScoreTextView = findViewById(R.id.tiesScore)
        mHumanScoreTextView = findViewById(R.id.playerScore)
        mAndroidScoreTextView = findViewById(R.id.androidScore)

        gameId = intent.extras?.get("gameId").toString()
        gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId!!)

        // Alternar el primer turno
        currentPlayer = if ((intent.extras?.get("isPlayerA") ?: Boolean) as Boolean) "A" else "B"
        isMyTurn = currentPlayer == "B" // Asumiendo que el primer turno es para PlayerA


//         Listener para los cambios en el juego en Firebase
        gameRef?.child("board")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val boardList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                boardList?.let {
                    // Procesar el cambio en el campo "board"
                    val boardCharArray: CharArray = boardList.joinToString("") { it.toString() }.toCharArray()
                    mGame.setBoardState(boardCharArray)

                    winner = mGame.checkForWinner()
                    if (winner == 0) {
                        mReplayButton.isVisible = false
                        mGameOver = false
                        togglePlayerTurn()
                        updateUI(gameRef!!)
                    } else {
                        handleWinner()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores al escuchar cambios
                Log.e("Firebase", "Error al escuchar cambios en 'board': ${error.message}")
            }
        })

        gameRef?.child("status")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                status?.let {
                    if ( currentPlayer == "A") mInfoTextView.text = "Your turn (x)"
                    if ( status == "deleted" ) finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores al escuchar cambios
                Log.e("Firebase", "Error al escuchar cambios en 'status': ${error.message}")
            }
        })


        mReplayButton.setOnClickListener { startNewGame() }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(gameRef: DatabaseReference) {
        gameRef.get().addOnSuccessListener { dataSnapshot ->
            val game = dataSnapshot.getValue(Game::class.java)

            if (game != null) {
                when (winner) {
                    0 -> {
                        if (game.status == "waiting") mInfoTextView.text =
                            "Waiting for other player..."
                        else {
                            if(currentPlayer == "A")
                                mInfoTextView.text = if (isMyTurn) "Your Turn (x)" else "Opponent's Turn (o)"
                            else
                                mInfoTextView.text = if (isMyTurn) "Your Turn (o)" else "Opponent's Turn (x)"
                        }
                    }
                    1 -> {
                        mInfoTextView.text = getString(R.string.result_tie)
                    }
                    2 -> {
                        if(isMyTurn){
                            mInfoTextView.text = "You won"
                        } else {
                            mInfoTextView.text = "Your opponet won"
                        }

                    }
                }
                mTiesScoreTextView.text = "Ties: ${game.tiesScore}"
                mHumanScoreTextView.text = if (currentPlayer == "A") "Your score: ${game.player1Score}" else "Your score: ${game.player2Score}"
                mAndroidScoreTextView.text = if (currentPlayer == "A") "Opponent's Score: ${game.player2Score}" else "Opponent's Score: ${game.player1Score}"
                mBoardView.invalidate()
            } else {
                Log.e("Firebase", "No se pudieron obtener los datos del juego.")
            }
        }.addOnFailureListener { exception ->
            // Manejar el error de la base de datos
            Log.e("Firebase", "Error al obtener los datos: ${exception.message}")
        }
    }


    private fun togglePlayerTurn() {
        isMyTurn = !isMyTurn
    }

    private fun setMove(location: Int): Boolean {
        val c: Char = if (currentPlayer == "A") 'X' else 'O'
        if (mGame.setMove(c, location)) {
            mBoardView.invalidate() // Redibujar el tablero para mostrar el movimiento
            return true
        }
        return false
    }

    private fun startNewGame() {
        mGame.clearBoard()
        mBoardView.invalidate()
        mReplayButton.isVisible = false
        mGameOver = false
        winner = 0

        gameRef?.child("board")?.setValue(mGame.getBoardState().map { it.toString() })

    }

    /**
     * Listener para gestionar toques en el tablero.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = View.OnTouchListener { _, event ->
        val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
        val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
        val pos = row * 3 + col

        gameRef?.child("status")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val status = task.result?.value as? String
                if (status != "waiting" && isMyTurn && !mGameOver && setMove(pos)) {
                    mHumanMediaPlayer?.start()
                    gameRef?.child("board")?.setValue(mGame.getBoardState().map { it.toString() })


                }
            } else {
                // Maneja el error aquí
                Log.e("FirebaseError", "Error al obtener el valor de 'status'", task.exception)
            }
        }
        false
    }

    private fun handleWinner() {

        mReplayButton.isVisible = true
        mGameOver = true

        var puntaje = ""

        when (winner) {
            1 -> {
                mInfoTextView.text = getString(R.string.result_tie)
                if(currentPlayer == "A") puntaje = "tiesScore"
            }
            2 -> {
                if(currentPlayer == "A" && isMyTurn){
                    puntaje = "player1Score"
                } else if (currentPlayer == "B" && isMyTurn) {
                    puntaje = "player2Score"
                }

            }
        }
        if (puntaje != ""){
            gameRef?.child(puntaje)?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    // Si el valor actual es null, lo inicializamos a 0
                    val currentScore = currentData.getValue(Int::class.java) ?: 0
                    // Incrementamos el valor en 1
                    currentData.value = currentScore + 1
                    return Transaction.success(currentData) // Confirma que la transacción fue exitosa
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (committed) {
                        // La transacción fue exitosa, puedes hacer algo aquí si lo deseas
                        Log.d("Firebase", "El puntaje del jugador se incrementó correctamente")
                    } else {
                        // Ocurrió un error durante la transacción
                        Log.e("Firebase", "Error al incrementar el puntaje del jugador", error?.toException())
                    }
                }
            })
        }
        updateUI(gameRef!!)
    }

    override fun onResume() {
        super.onResume()
        mHumanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.player_sound)
        mComputerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.computer_sound)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Verificar si gameRef no es nulo
        gameRef?.get()?.addOnSuccessListener { dataSnapshot ->
            // Obtener el nombre del juego desde la base de datos
            val game = dataSnapshot.getValue(Game::class.java)
            val gameName = game?.name ?: " "

            // Mostrar un diálogo de confirmación
            AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit? ( the game $gameName will close )")
                .setPositiveButton("Yes") { _, _ ->
                    // Eliminar el registro del juego en Firebase
                    gameRef?.child("status")?.setValue("deleted")?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            gameRef?.removeValue()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "The game was deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Error al eliminar el juego", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    super.onBackPressed()
                }
                .setNegativeButton("No", null) // Solo cierra el diálogo
                .show()
        } ?: run {
            // Si gameRef es nulo, llamar al comportamiento predeterminado
            gameRef?.child("status")?.setValue("deleted")?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    gameRef?.removeValue()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "The game was deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al eliminar el juego", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            super.onBackPressed()
        }
    }

    override fun onStop() {
        gameRef?.get()?.addOnSuccessListener { dataSnapshot ->
            // Obtener el nombre del juego desde la base de datos
            val game = dataSnapshot.getValue(Game::class.java)
            val gameName = game?.name ?: " "
            gameRef?.child("status")?.setValue("deleted")?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    gameRef?.removeValue()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "The game was deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al eliminar el juego", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            super.onStop()
        } ?: run {
            // Si gameRef es nulo, llamar al comportamiento predeterminado
            gameRef?.child("status")?.setValue("deleted")?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    gameRef?.removeValue()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "The game was deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al eliminar el juego", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            super.onStop()
        }
    }

}

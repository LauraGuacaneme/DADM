package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton


/**
 * Actividad principal para un juego de Tic-Tac-Toe en Android.
 * Esta clase maneja la lógica del juego, la interfaz de usuario y las interacciones del jugador.
 */
class AndroidTicTacToeActivity : Activity() {
    // Elementos de la interfaz para mostrar información y puntajes
    private lateinit var mInfoTextView: TextView
    private lateinit var mTiesScoreTextView: TextView
    private lateinit var mHumanScoreTextView: TextView
    private lateinit var mAndroidScoreTextView: TextView

    // Instancia del juego y botón de reinicio
    private lateinit var mGame: TicTacToeGame
    private lateinit var mReplayButton: MaterialButton

    // Variables para gestionar el estado del juego
    private var winner = 0
    private var isHumanTurnFirst = true // Indica si el jugador empieza primero
    private var humanScore = 0
    private var tiesScore = 0
    private var androidScore = 0

    // IDs para diferentes diálogos
    private val DIALOG_DIFFICULTY_ID: Int = 0
    private val DIALOG_ABOUT_ID: Int = 1
//    private val DIALOG_QUIT_ID: Int = 2
    private val DIALOG_RESET_SCORES_ID: Int = 2

    private lateinit var mBoardView: BoardView // Vista personalizada del tablero
    private var mGameOver: Boolean = false

    // Reproductores de sonido para movimientos de jugador y computadora
    var mHumanMediaPlayer: MediaPlayer? = null
    var mComputerMediaPlayer: MediaPlayer? = null

    private var mPrefs: SharedPreferences? = null

    /**
     * Llamado al crear la actividad.
     * Inicializa los componentes y configura el juego.
     */
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar? = findViewById(R.id.tool_bar)
        setActionBar(toolbar)

//        mBoardButtons = Array(9) { findViewById<Button>(resources.getIdentifier("button${it + 1}", "id", packageName)) }
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

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        // Restore the scores
        humanScore = mPrefs!!.getInt("mHumanWins", 0);
        androidScore = mPrefs!!.getInt("mComputerWins", 0);
        tiesScore = mPrefs!!.getInt("mTies", 0);

        if (savedInstanceState == null) {
            startNewGame();
        }
        else {
            // Restore the game's state
            savedInstanceState.getCharArray("board")?.let { mGame.setBoardState(it) }
            mGameOver = savedInstanceState.getBoolean("mGameOver")
            mInfoTextView.text = savedInstanceState.getCharSequence("info")
            humanScore = savedInstanceState.getInt("mHumanWins")
            androidScore = savedInstanceState.getInt("mComputerWins")
            tiesScore = savedInstanceState.getInt("mTies")
            isHumanTurnFirst = savedInstanceState.getBoolean("mGoFirst")
            winner = savedInstanceState.getInt("winner")
            if (winner != 0) {
                mReplayButton.isVisible = true
                mGameOver = true
            }
        }
        displayScores()

    }

    /**
     * Inicia un nuevo juego y alterna el primer turno entre el jugador y la computadora.
     */
    private fun startNewGame() {
        mGame.clearBoard()

        // Reiniciar los botones
//        mBoardButtons.forEachIndexed { index, button ->
//            button.text = " "
//            button.isEnabled = true
//            button.setOnClickListener(ButtonClickListener(index))
//        }
        mBoardView.invalidate()

        mReplayButton.isVisible = false
        mGameOver = false
        winner = 0
        if (isHumanTurnFirst) {
            mInfoTextView.text = getString(R.string.first_human)
        } else {
            // Código para iniciar el turno de la computadora
            val move = mGame.getComputerMove()
            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            mInfoTextView.text = getString(R.string.turn_human)
        }
        isHumanTurnFirst = !isHumanTurnFirst // Alterna para la próxima partida
    }

    @SuppressLint("SetTextI18n")
    private fun displayScores() {
        mHumanScoreTextView.text = "Player: $humanScore"
        mAndroidScoreTextView.text = "Android: $androidScore"
        mTiesScoreTextView.text = "Ties: $tiesScore"
    }
    /**
     * Listener para gestionar toques en el tablero.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = View.OnTouchListener { _, event ->
        val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
        val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
        val pos = row * 3 + col

        // Si el juego no ha terminado y el movimiento es válido
        if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
            mHumanMediaPlayer?.start()

            winner = mGame.checkForWinner()
            if (winner == 0) {
                mInfoTextView.text = getString(R.string.turn_computer)

                // Deshabilitar el tablero para evitar que el jugador haga movimientos
                mBoardView.isEnabled = false

                // Usar un Handler para retrasar el movimiento del computador
                Handler().postDelayed({
                    val move = mGame.getComputerMove()
                    mComputerMediaPlayer?.start()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)


                    winner = mGame.checkForWinner()

                    // Habilitar el tablero de nuevo después del movimiento del computador
                    mBoardView.isEnabled = true

                    // Verificar ganador y actualizar la interfaz
                    handleWinner()
                }, 500) // Retraso de 500 milisegundos (medio segundo)
            } else {
                handleWinner()
            }
        }
        false
    }


    /**
     * Verifica el estado del juego y actualiza los puntajes y la interfaz según el ganador.
     */
    @SuppressLint("SetTextI18n")
    private fun handleWinner() {
        if (winner != 0) {
            mReplayButton.isVisible = true
            mGameOver = true
        }

        when (winner) {
            0 -> mInfoTextView.text = getString(R.string.turn_human)
            1 -> {
                mInfoTextView.text = getString(R.string.result_tie)
                tiesScore++
                mTiesScoreTextView.text = "Ties: $tiesScore"
            }
            2 -> {
                mInfoTextView.text = getString(R.string.result_human_wins)
                humanScore++
                mHumanScoreTextView.text = "Player: $humanScore"
            }
            3 -> {
                mInfoTextView.text = getString(R.string.result_computer_wins)
                androidScore++
                mAndroidScoreTextView.text = "Android: $androidScore"
            }
        }
    }

    /**
     * Realiza un movimiento en el tablero.
     * @param player Jugador que realiza el movimiento.
     * @param location Posición en el tablero.
     * @return True si el movimiento fue exitoso, false en caso contrario.
     */
    private fun setMove(player: Char, location: Int): Boolean {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate() // Redibujar el tablero para mostrar la imagen del jugador
            return true
        }
        return false
    }

    /**
     * Reinicia el juego cuando se presiona el botón de reinicio.
     */
    fun replay(view: View) {
        startNewGame()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ai_difficulty -> {
                showDialog(DIALOG_DIFFICULTY_ID)
                return true
            }

            R.id.reset_scores -> {
                showDialog(DIALOG_RESET_SCORES_ID)
                return true
            }

            R.id.about -> {
                showDialog(DIALOG_ABOUT_ID)
                return true
            }
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        when (id) {
            DIALOG_DIFFICULTY_ID -> {
                // Título del diálogo
                builder.setTitle(R.string.difficulty_choose)


                // Niveles de dificultad
                val levels = arrayOf<CharSequence>(
                    resources.getString(R.string.difficulty_easy),
                    resources.getString(R.string.difficulty_harder),
                    resources.getString(R.string.difficulty_expert)
                )


                // Aquí defines cuál radio button estará seleccionado inicialmente
                val selected = 2 // Por ejemplo, el nivel fácil estará seleccionado

                builder.setSingleChoiceItems(levels, selected,
                    DialogInterface.OnClickListener { dialog, item ->
                        dialog.dismiss() // Cierra el diálogo

                        // TODO: Aquí puedes asignar la dificultad al juego según el elemento seleccionado
                        val selectedDifficulty = when (item) {
                            0 -> TicTacToeGame.DifficultyLevel.Easy
                            1 -> TicTacToeGame.DifficultyLevel.Harder
                            2 -> TicTacToeGame.DifficultyLevel.Expert
                            else -> TicTacToeGame.DifficultyLevel.Expert // Por defecto, para evitar errores
                        }
                        mGame.setDifficultyLevel(selectedDifficulty)


                        // Muestra un mensaje con el nivel seleccionado
                        Toast.makeText(
                            applicationContext,
                            levels[item],
                            Toast.LENGTH_SHORT
                        ).show()

                        startNewGame()
                    })


                // Crea el diálogo
                dialog = builder.create()
            }
            DIALOG_RESET_SCORES_ID -> {
                // Create the quit confirmation dialog
//                builder.setMessage(R.string.quit_question)
                builder.setMessage(R.string.reset_question)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes,
//                        DialogInterface.OnClickListener { dialog, id -> this@AndroidTicTacToeActivity.finish() })
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog.dismiss()
                            humanScore = 0
                            androidScore = 0
                            tiesScore = 0
                            displayScores()
                            startNewGame()
                        })
                    .setNegativeButton(R.string.no, null)
                dialog = builder.create()
            }
            DIALOG_ABOUT_ID -> {
                val context = applicationContext
                val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout: View = inflater.inflate(R.layout.about_dialog, null)
                builder.setView(layout)
                builder.setPositiveButton("OK", null)
                dialog = builder.create()
            }
        }
        return dialog
    }

    /**
     * Llamado al reanudar la actividad.
     * Inicializa los sonidos para los movimientos.
     */
    override fun onResume() {
        super.onResume()
        mHumanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.player_sound)
        mComputerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.computer_sound)
    }

    /**
     * Llamado al pausar la actividad.
     * Libera los recursos asociados a los reproductores de sonido.
     */
    override fun onPause() {
        super.onPause()
        mHumanMediaPlayer!!.release()
        mComputerMediaPlayer!!.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharArray("board", mGame.getBoardState())
        outState.putBoolean("mGameOver", mGameOver)
        outState.putInt("mHumanWins", Integer.valueOf(humanScore))
        outState.putInt("mComputerWins", Integer.valueOf(androidScore))
        outState.putInt("mTies", Integer.valueOf(tiesScore))
        outState.putCharSequence("info", mInfoTextView.text)
        outState.putBoolean("mGoFirst", isHumanTurnFirst)
        outState.putInt("winner", winner)
    }

    override fun onStop() {
        super.onStop()
        // Save the current scores
        val ed = mPrefs!!.edit()
        ed.putInt("mHumanWins", humanScore)
        ed.putInt("mComputerWins", androidScore)
        ed.putInt("mTies", tiesScore)
        ed.apply()
    }
}

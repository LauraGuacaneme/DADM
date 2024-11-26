package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton


class AndroidTicTacToeActivity : Activity() {
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var mInfoTextView: TextView
    private lateinit var mGame: TicTacToeGame
    private lateinit var mReplayButton: MaterialButton

    private lateinit var mTiesScoteTextView: TextView
    private lateinit var mHumanScoteTextView: TextView
    private lateinit var mAndroidScoteTextView: TextView

    private var winner = 0
    private var isHumanTurnFirst = true // Indica si el jugador empieza primero

    private var humanScore = 0
    private var tiesScore = 0
    private var androidScore = 0

    private val DIALOG_DIFFICULTY_ID: Int = 0
    private val DIALOG_ABOUT_ID: Int = 1
    private val DIALOG_QUIT_ID: Int = 2


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar? = findViewById(R.id.tool_bar)
        setActionBar(toolbar)

        mBoardButtons = Array(9) { findViewById<Button>(resources.getIdentifier("button${it + 1}", "id", packageName)) }
        mInfoTextView = findViewById(R.id.information)
        mGame = TicTacToeGame()

        mReplayButton = findViewById<MaterialButton>(R.id.replayButton)

        mTiesScoteTextView = findViewById(R.id.tiesScore)
        mHumanScoteTextView = findViewById(R.id.playerScore)
        mAndroidScoteTextView = findViewById(R.id.androidScore)

        startNewGame()
    }

    private fun startNewGame() {
        mGame.clearBoard()

        // Reiniciar los botones
        mBoardButtons.forEachIndexed { index, button ->
            button.text = " "
            button.isEnabled = true
            button.setOnClickListener(ButtonClickListener(index))
        }

        mReplayButton.isVisible = false
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

    private inner class ButtonClickListener(private val location: Int) : View.OnClickListener {
        override fun onClick(view: View?) {
            if (mBoardButtons[location].isEnabled) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location)

                winner = mGame.checkForWinner()

                if (winner == 0) {
                    mInfoTextView.text = getString(R.string.turn_computer)
                    val move = mGame.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    winner = mGame.checkForWinner()

                }
                if (winner != 0){
                    mBoardButtons.forEachIndexed { _, button ->
                        button.isEnabled = false
                    }

                    mReplayButton.isVisible = true
                }

                when (winner) {
                    0 -> mInfoTextView.text = getString(R.string.turn_human)
                    1 -> {
                        mInfoTextView.text = getString(R.string.result_tie)
                        tiesScore++
                        mTiesScoteTextView.text = "Ties: $tiesScore"
                    }
                    2 -> {
                        mInfoTextView.text = getString(R.string.result_human_wins)
                        humanScore++
                        mHumanScoteTextView.text = "Player: $humanScore"
                    }
                    3 -> {
                        mInfoTextView.text = getString(R.string.result_computer_wins)
                        androidScore++
                        mAndroidScoteTextView.text = "Android: $androidScore"
                    }
                }
            }
        }
    }

    private fun setMove(player: Char, location: Int) {
        mGame.setMove(player, location)
        mBoardButtons[location].isEnabled = false
        mBoardButtons[location].text = player.toString()

        if (player == TicTacToeGame.HUMAN_PLAYER) {
            mBoardButtons[location].setTextColor(Color.GREEN)
        } else {
            mBoardButtons[location].setTextColor(Color.RED)
        }
    }

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

            R.id.quit -> {
                showDialog(DIALOG_QUIT_ID)
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
            DIALOG_QUIT_ID -> {
                // Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes,
//                        DialogInterface.OnClickListener { dialog, id -> this@AndroidTicTacToeActivity.finish() })
                        DialogInterface.OnClickListener { dialog, id -> finishAffinity() })
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


}

package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
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


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

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

}

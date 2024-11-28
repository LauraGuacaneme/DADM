package co.edu.unal.reto1

import java.util.Random


class TicTacToeGame {
    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }

    // The computer's difficulty levels
    enum class DifficultyLevel {
        Easy, Harder, Expert
    };
    // Current difficulty level
    private var mDifficultyLevel = DifficultyLevel.Expert

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val random = Random()
    var isGameOver = false

    // Limpiar el tablero
    fun clearBoard() {
        for (i in board.indices) {
            board[i] = OPEN_SPOT
        }
    }

    // Colocar un movimiento
    fun setMove(player: Char, location: Int): Boolean {
        if (board[location] == OPEN_SPOT) {
            board[location] = player
            return true
        }
        return false
    }

    fun getComputerMove(): Int {
        var move = -1
        if (mDifficultyLevel == DifficultyLevel.Easy) move = getRandomMove()
        else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = getWinningMove()
            if (move == -1) move = getRandomMove()
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {
            // Try to win, but if that's not possible, block.
            // If that's not possible, move anywhere.
            move = getWinningMove()
            if (move == -1) move = getBlockingMove()
            if (move == -1) move = getRandomMove()
        }
        return move
    }

    private fun getRandomMove(): Int {
        var move: Int
        do {
            move = random.nextInt(BOARD_SIZE)
        } while (board[move] != OPEN_SPOT)

        board[move] = COMPUTER_PLAYER
        return move
    }

    private fun getWinningMove(): Int {
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    return i // Movimiento ganador
                }
                board[i] = OPEN_SPOT // Revertimos si no es ganador
            }
        }
        return -1
    }

    private fun getBlockingMove(): Int {
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = HUMAN_PLAYER
                if (checkForWinner() == 2) {
                    board[i] = COMPUTER_PLAYER
                    return i // Movimiento de bloqueo
                }
                board[i] = OPEN_SPOT // Revertimos si no es un bloqueo
            }
        }
        return -1
    }

    fun checkForWinner(): Int {
        // Revisar filas
        for (i in 0..6 step 3) {
            if (board[i] == board[i + 1] && board[i + 1] == board[i + 2]) {
                if (board[i] == HUMAN_PLAYER) return 2
                if (board[i] == COMPUTER_PLAYER) return 3
            }
        }

        // Revisar columnas
        for (i in 0..2) {
            if (board[i] == board[i + 3] && board[i + 3] == board[i + 6]) {
                if (board[i] == HUMAN_PLAYER) return 2
                if (board[i] == COMPUTER_PLAYER) return 3
            }
        }

        // Revisar diagonales
        if (board[0] == board[4] && board[4] == board[8]) {
            if (board[0] == HUMAN_PLAYER) return 2
            if (board[0] == COMPUTER_PLAYER) return 3
        }
        if (board[2] == board[4] && board[4] == board[6]) {
            if (board[2] == HUMAN_PLAYER) return 2
            if (board[2] == COMPUTER_PLAYER) return 3
        }

        // Revisar si hay empate
        return if (board.none { it == OPEN_SPOT }) 1 else 0
    }

    fun getDifficultyLevel(): DifficultyLevel {
        return mDifficultyLevel
    }

    fun setDifficultyLevel(difficultyLevel: DifficultyLevel) {
        mDifficultyLevel = difficultyLevel
    }

    fun getBoardOccupant(location: Int): Char {
        return board[location]
    }

}

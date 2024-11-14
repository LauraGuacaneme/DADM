package co.edu.unal.reto1

import java.util.Random

class TicTacToeGame {
    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val random = Random()

    // Limpiar el tablero
    fun clearBoard() {
        for (i in board.indices) {
            board[i] = OPEN_SPOT
        }
    }

    // Colocar un movimiento
    fun setMove(player: Char, location: Int) {
        if (board[location] == OPEN_SPOT) {
            board[location] = player
        }
    }

    fun getComputerMove(): Int {
        // Primero intentamos encontrar un movimiento para ganar
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    return i // Movimiento ganador
                }
                board[i] = OPEN_SPOT // Revertimos si no es ganador
            }
        }

        // Si no hay un movimiento ganador, intentamos bloquear al jugador
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

        // Si no hay movimiento ganador ni de bloqueo, elegimos una posición aleatoria
        var move: Int
        do {
            move = random.nextInt(BOARD_SIZE)
        } while (board[move] != OPEN_SPOT)

        board[move] = COMPUTER_PLAYER
        return move
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

}

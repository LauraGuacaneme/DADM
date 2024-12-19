package co.edu.unal.reto1

data class Game(
    var id: String = "", // Agregado para almacenar el ID del juego
    var name: String = "",
    var board: List<String> = List(9) { " " },
//    var turn: Int = 0, // Debe ser un Int
    var status: String = "",
    var player1Score: Int = 0, // Debe ser un Int
    var player2Score: Int = 0, // Debe ser un Int
    var tiesScore: Int = 0 // Debe ser un Int
)

package co.edu.unal.reto1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GamesAdapter(private val games: List<Game>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.bind(game)
        holder.itemView.setOnClickListener { onItemClick(game.id) }
    }

    override fun getItemCount(): Int = games.size

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameName: TextView = itemView.findViewById(R.id.game_name)
        private val playerStatus: TextView = itemView.findViewById(R.id.player_status)

        fun bind(game: Game) {
            gameName.text = "${game.name}: Waiting for opponent..."
            playerStatus.text = ""
        }
    }
}

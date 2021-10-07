package com.wechantloup.gamelistoptimization

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.wechantloup.gamelistoptimization.databinding.ItemGameBinding

class GamesAdapter(
    val onGameSetForKids: (Platform, String, Boolean) -> Unit,
    val onGameSetFavorite: (Platform, String, Boolean) -> Unit,
) : RecyclerView.Adapter<GamesAdapter.GameHolder>() {

    private var platform: Platform? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GameHolder(parent)

    override fun onBindViewHolder(holder: GameHolder, position: Int) {
        holder.bind(requireNotNull(platform).gameList.games[position])
    }

    override fun getItemCount(): Int = platform?.gameList?.games?.size ?: 0

    override fun onViewRecycled(holder: GameHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    fun setItems(newPlatform: Platform) {
        platform = newPlatform
        notifyDataSetChanged()
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
        LayoutInflater
            .from(context)
            .inflate(layoutRes, this, attachToRoot)

    inner class GameHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.item_game)) {

        private val binding = ItemGameBinding.bind(itemView)

        fun bind(game: Game) {
            val platform = requireNotNull(platform)
            binding.name.text = game.name
            binding.cbFavorite.isChecked = game.favorite
            binding.cbFavorite.setOnCheckedChangeListener { _, isChecked ->
                if (game.favorite == isChecked) return@setOnCheckedChangeListener

                game.favorite = isChecked
                onGameSetFavorite(platform, game.id, isChecked)
            }
            binding.cbForKids.isChecked = game.kidgame
            binding.cbForKids.setOnCheckedChangeListener { _, isChecked ->
                if (game.kidgame == isChecked) return@setOnCheckedChangeListener

                game.kidgame = isChecked
                onGameSetForKids(platform, game.id, isChecked)
            }
        }

        fun unbind() {
            binding.cbFavorite.setOnCheckedChangeListener(null)
            binding.cbForKids.setOnCheckedChangeListener(null)
        }
    }
}

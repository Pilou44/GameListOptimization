package com.wechantloup.gamelistoptimization

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wechantloup.gamelistoptimization.databinding.ItemGameBinding

class GamesAdapter(
    val onGameSetForKids: (String, Boolean) -> Unit,
    val onGameSetFavorite: (String, Boolean) -> Unit,
) : ListAdapter<Game, GamesAdapter.GameHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GameHolder(parent)

    override fun onBindViewHolder(holder: GameHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: GameHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
        LayoutInflater
            .from(context)
            .inflate(layoutRes, this, attachToRoot)

    inner class GameHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.item_game)) {

        private val binding = ItemGameBinding.bind(itemView)

        fun bind(game: Game) {
            binding.name.text = game.name
            binding.cbFavorite.isChecked = game.favorite
            binding.cbFavorite.setOnCheckedChangeListener { _, isChecked ->
                if (game.favorite == isChecked) return@setOnCheckedChangeListener

                game.favorite = isChecked
                onGameSetFavorite(game.id, isChecked)
            }
            binding.cbForKids.isChecked = game.kidgame
            binding.cbForKids.setOnCheckedChangeListener { _, isChecked ->
                if (game.kidgame == isChecked) return@setOnCheckedChangeListener

                game.kidgame = isChecked
                onGameSetForKids(game.id, isChecked)
            }
        }

        fun unbind() {
            binding.cbFavorite.setOnCheckedChangeListener(null)
            binding.cbForKids.setOnCheckedChangeListener(null)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                oldItem.isSameAs(newItem)

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean =
                oldItem.hasSameContentAs(newItem)
        }
    }
}

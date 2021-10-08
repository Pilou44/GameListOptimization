package com.wechantloup.gamelistoptimization

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.wechantloup.gamelistoptimization.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this)
    }
    private var currentPlatform: Platform? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.initRecyclerView()

        subscribeToUpdates()

        binding.initButtons()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun ActivityMainBinding.initButtons() {
        btnFavorite.setOnClickListener {
            val platform = currentPlatform ?: return@setOnClickListener
            val allFavorite = platform.gameList.games.all { it.favorite == true }
            platform.gameList.games.forEach {
                it.favorite = !allFavorite
            }
            val gameList = platform.gameList.getGamesCopy()
            (rvGames.adapter as GamesAdapter).submitList(gameList)
            lifecycleScope.launch {
                viewModel.savePlatform(platform)
            }
        }
        btnKid.setOnClickListener {
            val platform = currentPlatform ?: return@setOnClickListener
            val allChild = platform.gameList.games.all { it.kidgame == true }
            platform.gameList.games.forEach {
                it.kidgame = !allChild
            }
            val gameList = platform.gameList.getGamesCopy()
            (rvGames.adapter as GamesAdapter).submitList(gameList)
            lifecycleScope.launch {
                viewModel.savePlatform(platform)
            }
        }
    }

    private fun ActivityMainBinding.initRecyclerView() {
        rvGames.adapter = GamesAdapter(
            ::onGameSetForKids,
            ::onGameSetFavorite
        )
    }

    private fun onGameSetForKids(gameId: String, isSelected: Boolean) {
        val platform = currentPlatform ?: return
        viewModel.onGameSetForKids(platform, gameId, isSelected)
    }

    private fun onGameSetFavorite(gameId: String, isSelected: Boolean) {
        val platform = currentPlatform ?: return
        viewModel.onGameSetFavorite(platform, gameId, isSelected)
    }

    private fun subscribeToUpdates() {
        viewModel.stateFlow
            .flowWithLifecycle(lifecycle)
            .onEach {
                showPlatforms(it)
            }
            .launchIn(lifecycleScope)
    }

    private fun showPlatforms(platforms: List<Platform>) {
        ArrayAdapter<Platform>(
            this,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.addAll(platforms)
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.dropdownPlatforms.setAdapter(adapter)
            binding.dropdownPlatforms.addTextChangedListener { text ->
                val selectedPlatform = platforms.firstOrNull {
                    it.toString() == text.toString()
                } ?: return@addTextChangedListener
                Log.i("TOTO", "Item selected: $selectedPlatform")
                currentPlatform = selectedPlatform
                val gameList = selectedPlatform.gameList.getGamesCopy()
                (binding.rvGames.adapter as GamesAdapter).submitList(gameList)
            }

        }
    }
}
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.initButtons()
        binding.initRecyclerView()

        subscribeToUpdates()
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
        btnFavorite.isEnabled = false
        btnKid.isEnabled = false
        btnCopyFromBackup.isEnabled = false
        dropdownSources.isEnabled = false
        dropdownPlatforms.isEnabled = false

        btnCopyFromBackup.setOnClickListener {
            viewModel.copyBackupValues()
        }

        btnFavorite.setOnClickListener {
            viewModel.setAllFavorite()
        }

        btnKid.setOnClickListener {
            viewModel.setAllForKids()
        }
    }

    private fun ActivityMainBinding.initRecyclerView() {
        rvGames.adapter = GamesAdapter(
            viewModel::onGameSetForKids,
            viewModel::onGameSetFavorite
        )
    }

    private fun subscribeToUpdates() {
        viewModel.stateFlow
            .flowWithLifecycle(lifecycle)
            .onEach {
                binding.showSources(it.sources)
                binding.showPlatforms(it.platforms)
                binding.showGames(it.games, it.hasBackup)
            }
            .launchIn(lifecycleScope)
    }

    private fun ActivityMainBinding.showGames(games: List<Game>, hasBackup: Boolean) {
        (rvGames.adapter as GamesAdapter).submitList(games)
        btnKid.isEnabled = games.isNotEmpty()
        btnFavorite.isEnabled = games.isNotEmpty()
        btnCopyFromBackup.isEnabled = games.isNotEmpty() && hasBackup
    }

    private fun ActivityMainBinding.showPlatforms(platforms: List<Platform>) {
        ArrayAdapter<Platform>(
            this@MainActivity,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.addAll(platforms)
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            dropdownPlatforms.setAdapter(adapter)
        }
        dropdownPlatforms.addTextChangedListener { text ->
            val selectedPlatform = platforms.firstOrNull {
                it.toString() == text.toString()
            } ?: return@addTextChangedListener
            Log.i("TOTO", "Item selected: $selectedPlatform")
            viewModel.setPlatform(selectedPlatform)
        }
        dropdownPlatforms.isEnabled = platforms.isNotEmpty()
    }

    private fun ActivityMainBinding.showSources(sources: List<Source>) {
        ArrayAdapter<Source>(
            this@MainActivity,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.addAll(sources)
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            dropdownSources.setAdapter(adapter)
        }

        dropdownSources.addTextChangedListener { text ->
            val selectedSource = sources.firstOrNull {
                it.toString() == text.toString()
            } ?: return@addTextChangedListener
            dropdownPlatforms.text.clear()
            (rvGames.adapter as GamesAdapter).submitList(emptyList())
            btnKid.isEnabled = false
            btnFavorite.isEnabled = false
            btnCopyFromBackup.isEnabled = false
            spinnerPlatform.isEnabled = false
            viewModel.setSource(selectedSource)
        }

        dropdownSources.isEnabled = sources.isNotEmpty()
    }
}
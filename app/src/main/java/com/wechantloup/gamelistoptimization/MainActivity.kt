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
                (binding.rvGames.adapter as GamesAdapter).setItems(selectedPlatform)
            }

        }
    }
}
// https://github.com/SXaviour/Taste-Journal
package com.griffith

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.griffith.data.AppDatabase
import com.griffith.data.Dish
import com.griffith.data.DishRepository
import com.griffith.ui.theme.components.MealCategoryCircle
import com.griffith.ui.theme.components.RecipeCard
import com.griffith.ui.theme.components.SearchBar
import com.griffith.ui.theme.TasteTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelCurrent = SensorManager.GRAVITY_EARTH
    private var accelLast = SensorManager.GRAVITY_EARTH
    private var shake = 0f
    private var lastShakeTime = 0L
    private val repo by lazy { DishRepository(AppDatabase.get(applicationContext).dishDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setContent {
            TasteTheme(dark = true) {
                HomeScaffold()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        accelLast = accelCurrent
        accelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = accelCurrent - accelLast
        shake = shake * 0.9f + delta

        if (shake > 12f) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 1000L) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    private fun onShake() {
        val intent = Intent(this, ShuffleActivity::class.java)
        startActivity(intent)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(vm: HomeVM = viewModel()) {
    var tab by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row {
                    Text(if (tab == 0) "Home" else "Timeline")
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.launchAdd() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Text("⚡") },
                    label = { Text("Timeline") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2},
                    icon = { Text("👤") },
                    label = { Text("Profile") }
                )
            }
        }
        // I replaced if statement with when statement to hook profile
    ) { pad ->
        when (tab) {
            0 -> HomeContent(pad, vm, query, onQuery = { query = it })
            1 -> com.griffith.TimelineScreen(pad, vm)
            2 -> com.griffith.ProfileScreen(pad)
        }
    }
}

@Composable
private fun HomeContent(
    pad: PaddingValues,
    vm: HomeVM,
    query: String,
    onQuery: (String) -> Unit
) {
    val dishes by vm.all.collectAsState(emptyList())

    Column(
        Modifier.padding(pad).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchBar(query, onQuery, onSearch = { }, onFilter = { })
        Text("Meal Categories", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MealCategoryCircle("Breakfast") { }
            MealCategoryCircle("Lunch") { }
            MealCategoryCircle("Dinner") { }
            MealCategoryCircle("Nigerian") { }
        }
        Text("Your Recipes", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(170.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dishes) { d -> RecipeCard(d) { vm.launchDetails(it.id) } }
        }
    }
}



@Composable
private fun Section(title: String, data: List<Dish>, onOpen: (Dish) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    LazyRow {
        items(data.size) { i ->
            RecipeCard(data[i]) { onOpen(it) }
        }
    }
}

class HomeVM : ViewModel() {
    private val repo = DishRepository(AppDatabase.get(App.app).dishDao())

    private val _all = MutableStateFlow<List<Dish>>(emptyList())
    val all: StateFlow<List<Dish>> = _all

    val recent = repo.recent(10)
    val top = repo.top(10)
    val forgotten = repo.forgotten(4, System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000, 10)

    init {
        viewModelScope.launch {
            repo.all().collect { _all.value = it }
        }
    }

    fun launchAdd() {
        val ctx = App.app
        ctx.startActivity(
            Intent(ctx, AddDishActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun launchDetails(id: Long) {
        val ctx = App.app
        ctx.startActivity(
            Intent(ctx, DetailsActivity::class.java)
                .putExtra("id", id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

package com.griffith


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TasteTheme(dark = false) { HomeScaffold() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(vm: HomeVM = viewModel()) {
    var tab by remember { mutableStateOf(0) }  // 0=Home, 1=Timeline
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row {
                    Text(if (tab==0) "Home" else "Timeline")
                    Spacer(Modifier.width(8.dp))
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.launchAdd() }, containerColor = MaterialTheme.colorScheme.primary) {
                Text("+")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab==0, onClick = { tab=0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab==1, onClick = { tab=1 },
                    icon = { Text("⚡") }, label = { Text("Timeline") }
                )
                NavigationBarItem(
                    selected = false, onClick = { /* profile placeholder */ },
                    icon = { Text("👤") }, label = { Text("") }
                )
            }
        }
    ) { pad ->
        if (tab==0) HomeContent(pad, vm, query, onQuery = { query = it })
        else TimelineContent(pad, vm)
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

    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SearchBar(query, onQuery, onSearch = { /* optional filter state */ }, onFilter = { /* filter sheet */ })

        Text("Meal Categories", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MealCategoryCircle("Breakfast"){}
            MealCategoryCircle("Lunch"){}
            MealCategoryCircle("Dinner"){}
            MealCategoryCircle("Nigerian"){}
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
private fun TimelineContent(pad: PaddingValues, vm: HomeVM) {
    val recent by vm.recent.collectAsState(emptyList())
    val favs by vm.forgotten.collectAsState(emptyList())
    val top by vm.top.collectAsState(emptyList())

    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section("Recently Cooked", recent) { vm.launchDetails(it.id) }
        Section("Forgotten Favorites", favs) { vm.launchDetails(it.id) }
        Section("Top Rated", top) { vm.launchDetails(it.id) }
    }
}

@Composable
private fun Section(title: String, data: List<Dish>, onOpen: (Dish) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    androidx.compose.foundation.lazy.LazyRow {
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
    val forgotten = repo.forgotten(4, System.currentTimeMillis() - 60L*24*60*60*1000, 10)

    init {
        viewModelScope.launch { repo.all().collect { _all.value = it } }
    }

    fun launchAdd() {
        val ctx = App.app
        ctx.startActivity(Intent(ctx, AddDishActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun launchDetails(id: Long) {
        val ctx = App.app
        ctx.startActivity(Intent(ctx, DetailsActivity::class.java).putExtra("id", id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
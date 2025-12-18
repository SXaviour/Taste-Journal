package com.griffith

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.griffith.HomeVM
import com.griffith.ui.theme.components.TimelineSection
import com.griffith.ui.theme.components.AutoScrollDirection

@Composable
fun TimelineScreen(pad: PaddingValues, vm: HomeVM) {
    val top by vm.top.collectAsState(emptyList())
    val recent by vm.recent.collectAsState(emptyList())
    val favs by vm.forgotten.collectAsState(emptyList())

    Column(
        Modifier
            .padding(pad)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Text(
            "Timeline",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))


        TimelineSection(
            title = "Top Rated",
            data = top,
            direction = AutoScrollDirection.RightToLeft,
            onOpen = { vm.launchDetails(it.id) }
        )

        TimelineSection(
            title = "Recently Cooked",
            data = recent,
            direction = AutoScrollDirection.LeftToRight,
            onOpen = { vm.launchDetails(it.id) }
        )

        TimelineSection(
            title = "Forgotten Favorites",
            data = favs,
            direction = AutoScrollDirection.RightToLeft,
            onOpen = { vm.launchDetails(it.id) }
        )
    }
}

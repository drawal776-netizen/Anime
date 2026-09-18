package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MediaCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OttViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun BrowseScreen(
    viewModel: OttViewModel,
    modifier: Modifier = Modifier
) {
    val browseType by viewModel.browseType.collectAsState()
    val browseGenre by viewModel.browseGenre.collectAsState()
    val browseLanguage by viewModel.browseLanguage.collectAsState()
    val browseSort by viewModel.browseSort.collectAsState()

    val browseItems = viewModel.browseFilteredList
    val tabs = listOf("All", "Movies", "Series")
    val selectedTabIndex = tabs.indexOf(browseType).let { if (it >= 0) it else 0 }

    var isSortMenuOpen by remember { mutableStateOf(false) }

    val allGenres = listOf("All") + viewModel.allUniqueGenres
    val allLanguages = listOf("All") + viewModel.allUniqueLanguages

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- 1. Top Bar & Tabs ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse Catalog",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Box {
                    Button(
                        onClick = { isSortMenuOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("browse_sort_button")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(browseSort.displayName, color = TextPrimary, fontSize = 11.sp)
                    }

                    DropdownMenu(
                        expanded = isSortMenuOpen,
                        onDismissRequest = { isSortMenuOpen = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        SortOption.values().forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = opt.displayName,
                                            color = if (browseSort == opt) FlameOrange else TextPrimary,
                                            fontSize = 12.sp
                                        )
                                        if (browseSort == opt) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setBrowseSort(opt)
                                    isSortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Primary Tabs: All | Movies | Series
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = FlameOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = FlameOrange,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.setBrowseType(title) },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) FlameOrange else TextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        }

        // --- 2. Filter Rows: Genres & Languages ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Genre Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allGenres.forEach { genre ->
                    val isSelected = browseGenre == genre
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) FlameOrange else DarkCardBg,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, if (isSelected) FlameOrange else DarkBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setBrowseGenre(genre) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Language Filter Chips
            if (allLanguages.size > 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allLanguages.forEach { lang ->
                        val isSelected = browseLanguage == lang
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(0.5.dp, if (isSelected) CyberCyan else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setBrowseLanguage(lang) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = lang,
                                color = if (isSelected) CyberCyan else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${browseItems.size} items found",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (browseGenre != "All" || browseLanguage != "All") {
                    Text(
                        text = "Clear filters",
                        color = FlameOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            viewModel.setBrowseGenre("All")
                            viewModel.setBrowseLanguage("All")
                        }
                    )
                }
            }
        }

        // --- 3. Grid of Content ---
        if (browseItems.isEmpty()) {
            EmptyStateView(
                title = "No matches found",
                message = "Try changing your genre or language filter to discover more titles.",
                actionButtonText = "Reset Filters",
                onActionClick = {
                    viewModel.setBrowseType("All")
                    viewModel.setBrowseGenre("All")
                    viewModel.setBrowseLanguage("All")
                }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 105.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(browseItems, key = { it.id }) { item ->
                    MediaCard(
                        item = item,
                        cardWidth = 110,
                        cardHeight = 160,
                        onClick = { viewModel.openDetails(item) }
                    )
                }
            }
        }
    }
}

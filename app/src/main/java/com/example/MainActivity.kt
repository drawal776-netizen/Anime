package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AllAnimeScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.DetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyListScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OttTab
import com.example.ui.viewmodel.OttViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: OttViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AniStreamApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AniStreamApp(viewModel: OttViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeDetailsItem by viewModel.activeDetailsItem.collectAsState()
    val activePlayerItem by viewModel.activePlayerItem.collectAsState()
    val activeTrailerItem by viewModel.activeTrailerItem.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val refreshMessage by viewModel.refreshMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(refreshMessage) {
        refreshMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissRefreshMessage()
        }
    }

    // Handle Hardware / Gesture Back Press
    BackHandler(enabled = activePlayerItem != null || activeTrailerItem != null || activeDetailsItem != null || selectedTab != OttTab.HOME) {
        when {
            activePlayerItem != null -> viewModel.closePlayer()
            activeTrailerItem != null -> viewModel.closeTrailer()
            activeDetailsItem != null -> viewModel.closeDetails()
            selectedTab != OttTab.HOME -> viewModel.selectTab(OttTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (activePlayerItem == null && activeTrailerItem == null) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkSurface
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.selectTab(OttTab.HOME) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(FlameOrange, Color(0xFFFF8C38))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AniStream",
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    actions = {
                        // Global Search shortcut button
                        IconButton(
                            onClick = { viewModel.selectTab(OttTab.SEARCH) },
                            modifier = Modifier.testTag("topbar_search_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (selectedTab == OttTab.SEARCH) FlameOrange else TextSecondary
                            )
                        }

                        // Profile Avatar Button
                        val avatarEmojis = listOf("🦊", "⚡", "🌸", "🐉", "🍙")
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkBorder)
                                .border(1.dp, if (selectedTab == OttTab.PROFILE) FlameOrange else Color.Transparent, CircleShape)
                                .clickable { viewModel.selectTab(OttTab.PROFILE) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarEmojis.getOrElse(activeProfile.avatarIndex) { "🦊" },
                                fontSize = 16.sp
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (activePlayerItem == null && activeTrailerItem == null) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.height(72.dp)
                ) {
                    // 1. Home
                    NavigationBarItem(
                        selected = selectedTab == OttTab.HOME,
                        onClick = { viewModel.selectTab(OttTab.HOME) },
                        icon = {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FlameOrange,
                            selectedTextColor = FlameOrange,
                            indicatorColor = FlameOrange.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    // 2. Browse
                    NavigationBarItem(
                        selected = selectedTab == OttTab.BROWSE,
                        onClick = { viewModel.selectTab(OttTab.BROWSE) },
                        icon = {
                            Icon(Icons.Default.Explore, contentDescription = "Browse")
                        },
                        label = { Text("Browse", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FlameOrange,
                            selectedTextColor = FlameOrange,
                            indicatorColor = FlameOrange.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_browse")
                    )

                    // 3. All Anime (Special explicit user requirement)
                    NavigationBarItem(
                        selected = selectedTab == OttTab.ALL_ANIME,
                        onClick = { viewModel.selectTab(OttTab.ALL_ANIME) },
                        icon = {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "All Anime")
                        },
                        label = { Text("All Anime", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FlameOrange,
                            selectedTextColor = FlameOrange,
                            indicatorColor = FlameOrange.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_all_anime")
                    )

                    // 4. Search
                    NavigationBarItem(
                        selected = selectedTab == OttTab.SEARCH,
                        onClick = { viewModel.selectTab(OttTab.SEARCH) },
                        icon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        label = { Text("Search", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FlameOrange,
                            selectedTextColor = FlameOrange,
                            indicatorColor = FlameOrange.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_search")
                    )

                    // 5. My List
                    NavigationBarItem(
                        selected = selectedTab == OttTab.MY_LIST,
                        onClick = { viewModel.selectTab(OttTab.MY_LIST) },
                        icon = {
                            if (watchlist.isNotEmpty()) {
                                BadgedBox(badge = {
                                    Badge(containerColor = FlameOrange) {
                                        Text("${watchlist.size}", color = Color.White, fontSize = 9.sp)
                                    }
                                }) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "My List")
                                }
                            } else {
                                Icon(Icons.Default.Bookmark, contentDescription = "My List")
                            }
                        },
                        label = { Text("My List", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FlameOrange,
                            selectedTextColor = FlameOrange,
                            indicatorColor = FlameOrange.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_my_list")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab View
            when (selectedTab) {
                OttTab.HOME -> HomeScreen(viewModel = viewModel)
                OttTab.BROWSE -> BrowseScreen(viewModel = viewModel)
                OttTab.ALL_ANIME -> AllAnimeScreen(viewModel = viewModel)
                OttTab.SEARCH -> SearchScreen(viewModel = viewModel)
                OttTab.MY_LIST -> MyListScreen(viewModel = viewModel)
                OttTab.PROFILE -> ProfileScreen(viewModel = viewModel)
            }

            // Animated Details Screen Overlay
            AnimatedVisibility(
                visible = activeDetailsItem != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                activeDetailsItem?.let { item ->
                    DetailsScreen(
                        item = item,
                        viewModel = viewModel,
                        onBack = { viewModel.closeDetails() }
                    )
                }
            }

            // Animated Video Player Overlay (Full Screen)
            AnimatedVisibility(
                visible = activePlayerItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activePlayerItem?.let { item ->
                    PlayerScreen(
                        item = item,
                        isTrailer = false,
                        viewModel = viewModel,
                        onClose = { viewModel.closePlayer() }
                    )
                }
            }

            // Animated Trailer Player Overlay (Full Screen)
            AnimatedVisibility(
                visible = activeTrailerItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeTrailerItem?.let { item ->
                    PlayerScreen(
                        item = item,
                        isTrailer = true,
                        viewModel = viewModel,
                        onClose = { viewModel.closeTrailer() }
                    )
                }
            }
        }
    }
}

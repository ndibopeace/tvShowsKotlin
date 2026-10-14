package com.watchline.app

import android.os.Bundle
import android.text.Html
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.watchline.app.data.AppState
import com.watchline.app.data.AppTab
import com.watchline.app.data.TrackedShow
import com.watchline.app.data.TvEpisode
import com.watchline.app.data.TvMazeApi
import com.watchline.app.data.TvShow
import com.watchline.app.data.WatchDatabase
import com.watchline.app.data.WatchStatus
import java.util.Locale

private val Night = Color(0xFF101015)
private val Panel = Color(0xFF1B1B24)
private val PanelSoft = Color(0xFF24242F)
private val TextMain = Color(0xFFF6F3F1)
private val TextMuted = Color(0xFFA9A4AE)
private val Amber = Color(0xFFF0A66B)
private val Lavender = Color(0xFFB5A0FF)

class MainActivity : ComponentActivity() {
    private val viewModel: ShowTrackerViewModel by viewModels {
        ShowTrackerViewModel.factory(
            TvMazeApi(),
            WatchDatabase(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchlineTheme {
                WatchlineApp(viewModel)
            }
        }
    }
}

@Composable
private fun WatchlineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Amber,
            onPrimary = Night,
            secondary = Lavender,
            background = Night,
            surface = Panel,
            surfaceVariant = PanelSoft,
            onSurface = TextMain,
            onSurfaceVariant = TextMuted,
        ),
        content = content,
    )
}

@Composable
private fun WatchlineApp(viewModel: ShowTrackerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.selectedShow != null) {
        viewModel.closeShow()
    }

    Scaffold(
        containerColor = Night,
        bottomBar = {
            if (state.selectedShow == null) {
                NavigationBar(
                    containerColor = Night,
                    contentColor = TextMuted,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = state.activeTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon(),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(21.dp),
                                )
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Amber,
                                selectedTextColor = Amber,
                                indicatorColor = Amber.copy(alpha = 0.14f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val show = state.selectedShow
        if (show != null) {
            ShowDetailScreen(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onBack = viewModel::closeShow,
                onStatusChange = viewModel::setStatus,
                onEpisodeToggle = viewModel::toggleEpisode,
                onRetry = { viewModel.openShow(show) },
            )
        } else {
            when (state.activeTab) {
                AppTab.HOME -> HomeScreen(
                    state = state,
                    modifier = Modifier.padding(innerPadding),
                    onShowClick = viewModel::openShow,
                    onRetry = viewModel::refreshDiscovery,
                )

                AppTab.SEARCH -> SearchScreen(
                    state = state,
                    modifier = Modifier.padding(innerPadding),
                    onQueryChange = viewModel::searchShows,
                    onShowClick = viewModel::openShow,
                )

                AppTab.LIBRARY -> LibraryScreen(
                    state = state,
                    modifier = Modifier.padding(innerPadding),
                    onFilterChange = viewModel::setLibraryFilter,
                    onShowClick = viewModel::openShow,
                    onSearchClick = { viewModel.selectTab(AppTab.SEARCH) },
                )
            }
        }
    }
}

private fun AppTab.icon(): ImageVector = when (this) {
    AppTab.HOME -> Icons.Filled.Home
    AppTab.SEARCH -> Icons.Filled.Search
    AppTab.LIBRARY -> Icons.Filled.Bookmark
}

@Composable
private fun HomeScreen(
    state: AppState,
    modifier: Modifier = Modifier,
    onShowClick: (TvShow) -> Unit,
    onRetry: () -> Unit,
) {
    val feature = state.trendingShows.firstOrNull() ?: state.popularShows.firstOrNull()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item { BrandHeader() }
        item {
            Column(Modifier.padding(horizontal = 22.dp)) {
                Text(
                    "Find your next\nfavorite.",
                    color = TextMain,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "A better place to keep up with every story.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (feature != null) {
            item {
                FeatureCard(
                    show = feature,
                    kicker = if (state.trendingShows.isNotEmpty()) "AIRING TODAY" else "POPULAR RIGHT NOW",
                    onClick = { onShowClick(feature) },
                )
            }
        }
        if (state.isLoadingHome && state.popularShows.isEmpty()) {
            item { LoadingPanel("Finding the shows everyone's watching…") }
        }
        if (state.homeError != null) {
            item {
                ErrorPanel(
                    message = state.homeError,
                    buttonLabel = "Try again",
                    onRetry = onRetry,
                )
            }
        }
        if (state.popularShows.isNotEmpty()) {
            item {
                ShowCarousel(
                    title = "Popular right now",
                    subtitle = "The shows viewers are finding",
                    shows = state.popularShows.take(18),
                    onShowClick = onShowClick,
                )
            }
        }
        if (state.trendingShows.isNotEmpty()) {
            item {
                ShowCarousel(
                    title = "Trending today",
                    subtitle = "Shows with new episodes on today's schedule",
                    shows = state.trendingShows.take(18),
                    onShowClick = onShowClick,
                )
            }
        }
        if (state.topRatedShows.isNotEmpty()) {
            item {
                ShowCarousel(
                    title = "Highest rated",
                    subtitle = "The strongest TVMaze audience scores",
                    shows = state.topRatedShows,
                    onShowClick = onShowClick,
                )
            }
        }
        if (state.mostRatedShows.isNotEmpty()) {
            item {
                ShowCarousel(
                    title = "Most rated",
                    subtitle = "The most community votes",
                    shows = state.mostRatedShows,
                    onShowClick = onShowClick,
                )
            }
        }
        item {
            Text(
                "Show information and imagery provided by TVMaze.",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            color = Amber,
            contentColor = Night,
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Tv, contentDescription = null, modifier = Modifier.size(19.dp))
            }
        }
        Text(
            "WATCHLINE",
            color = TextMain,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun FeatureCard(show: TvShow, kicker: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(320.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(Panel),
    ) {
        AsyncImage(
            model = show.imageUrl,
            contentDescription = "${show.name} artwork",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.03f),
                        0.34f to Color.Black.copy(alpha = 0.08f),
                        1.0f to Color.Black.copy(alpha = 0.88f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                kicker,
                color = Amber,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
            Text(
                show.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingLabel(show.rating)
                if (show.genres.isNotEmpty()) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        show.genres.take(2).joinToString("  ·  "),
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                "Explore show",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ShowCarousel(
    title: String,
    subtitle: String,
    shows: List<TvShow>,
    onShowClick: (TvShow) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.padding(horizontal = 22.dp)) {
            Text(
                title,
                color = TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            items(shows, key = { it.id }) { show ->
                ShowPosterCard(show = show, onClick = { onShowClick(show) })
            }
        }
    }
}

@Composable
private fun ShowPosterCard(show: TvShow, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(142.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(204.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PanelSoft),
        ) {
            AsyncImage(
                model = show.imageUrl,
                contentDescription = "${show.name} poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (show.rating != null) {
                Surface(
                    color = Night.copy(alpha = 0.82f),
                    contentColor = TextMain,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Amber, modifier = Modifier.size(13.dp))
                        Text(String.format(Locale.US, "%.1f", show.rating), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Text(
            show.name,
            color = TextMain,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            show.genres.firstOrNull() ?: show.premiered?.take(4).orEmpty(),
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SearchScreen(
    state: AppState,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onShowClick: (TvShow) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    "Search shows",
                    color = TextMain,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Find a series and keep every season in view.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Show title", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = PanelSoft,
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel,
                    cursorColor = Amber,
                ),
            )
        }
        when {
            state.searchQuery.isBlank() -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Popular searches",
                            color = TextMain,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.popularShows.take(6).forEach { show ->
                                AssistChip(
                                    onClick = { onShowClick(show) },
                                    label = { Text(show.name, maxLines = 1) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                )
                            }
                        }
                        Text(
                            "Or type any show name above.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            state.isSearching -> item { LoadingPanel("Searching the TVMaze catalogue…") }
            state.searchError != null -> item {
                ErrorPanel(
                    message = state.searchError,
                    buttonLabel = "Try again",
                    onRetry = { onQueryChange(state.searchQuery) },
                )
            }

            state.searchResults.isEmpty() -> item {
                EmptyPanel(
                    title = "No shows found",
                    message = "Try another title or check the spelling.",
                )
            }

            else -> {
                item {
                    Text(
                        "${state.searchResults.size} results",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                items(state.searchResults, key = { it.id }) { show ->
                    SearchResultRow(show = show, onClick = { onShowClick(show) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(show: TvShow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(Panel)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = show.imageUrl,
            contentDescription = "${show.name} poster",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 68.dp, height = 94.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(PanelSoft),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                show.name,
                color = TextMain,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(show.premiered?.take(4), show.genres.take(2).joinToString(" · ").ifBlank { null })
                    .joinToString("  ·  "),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            RatingLabel(show.rating)
            Text(
                cleanHtml(show.summary).ifBlank { "Show information and episode guide available." },
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LibraryScreen(
    state: AppState,
    modifier: Modifier = Modifier,
    onFilterChange: (WatchStatus?) -> Unit,
    onShowClick: (TvShow) -> Unit,
    onSearchClick: () -> Unit,
) {
    val filters = listOf(null) + WatchStatus.entries
    val shows = state.savedShows.filter { state.libraryFilter == null || it.status == state.libraryFilter }
    val emptyMessage = state.libraryFilter?.label?.lowercase()?.let { label ->
        "Shows you mark as $label will appear here."
    } ?: "Search for a show and add it to your Watchlist, Watching, Completed, or Dropped list."
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "My shows",
                    color = TextMain,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${state.savedShows.size} ${if (state.savedShows.size == 1) "show" else "shows"} in your collection",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filters.forEach { filter ->
                    val selected = state.libraryFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter?.label ?: "All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber.copy(alpha = 0.2f),
                            selectedLabelColor = Amber,
                            labelColor = TextMuted,
                        ),
                    )
                }
            }
        }
        if (shows.isEmpty()) {
            item {
                EmptyPanel(
                    title = if (state.libraryFilter == null) "Your list is ready" else "Nothing here yet",
                    message = emptyMessage,
                    buttonLabel = "Find a show",
                    onAction = onSearchClick,
                )
            }
        } else {
            items(shows, key = { it.show.id }) { tracked ->
                TrackedShowRow(tracked = tracked, onClick = { onShowClick(tracked.show) })
            }
        }
        item {
            Text(
                "Your collection is saved on this device.",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun TrackedShowRow(tracked: TrackedShow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .clickable(onClick = onClick)
            .padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = tracked.show.imageUrl,
            contentDescription = "${tracked.show.name} poster",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 68.dp, height = 96.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(PanelSoft),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                tracked.show.name,
                color = TextMain,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                tracked.show.genres.take(2).joinToString(" · ")
                    .ifBlank { tracked.show.premiered?.take(4) ?: "TV series" },
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StatusPill(tracked.status)
            if (tracked.watchedEpisodes > 0 || tracked.status == WatchStatus.WATCHING) {
                Text(
                    "${tracked.watchedEpisodes} episodes watched",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        RatingLabel(tracked.show.rating)
    }
}

@Composable
private fun ShowDetailScreen(
    state: AppState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onStatusChange: (WatchStatus?) -> Unit,
    onEpisodeToggle: (TvEpisode) -> Unit,
    onRetry: () -> Unit,
) {
    val show = state.selectedShow ?: return
    val seasonGroups = state.episodes.groupBy { it.season ?: 0 }.toSortedMap()
    var expandedSeason by remember(show.id) {
        mutableIntStateOf(seasonGroups.keys.firstOrNull() ?: 1)
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item {
            ShowDetailHero(show = show, onBack = onBack)
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RatingLabel(show.rating)
                    Text(
                        listOfNotNull(
                            show.premiered?.take(4),
                            show.status,
                            show.runtime?.let { "$it min" },
                        ).joinToString("  ·  "),
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (show.genres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        show.genres.take(4).forEach { genre ->
                            Surface(
                                color = PanelSoft,
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Text(
                                    genre,
                                    color = TextMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
                Text(
                    cleanHtml(show.summary).ifBlank { "A full show summary isn't available yet." },
                    color = TextMain.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 23.sp,
                )
                StatusPicker(
                    status = state.selectedStatus,
                    onStatusChange = onStatusChange,
                )
            }
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    "Seasons & episodes",
                    color = TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                val watched = state.watchedEpisodeIds.size
                Text(
                    if (state.episodes.isEmpty()) "Episode guide" else "$watched of ${state.episodes.size} episodes watched",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (state.isLoadingDetails && state.episodes.isEmpty()) {
            item { LoadingPanel("Loading seasons and episodes…") }
        }
        if (state.detailError != null) {
            item {
                ErrorPanel(
                    message = state.detailError,
                    buttonLabel = "Retry",
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        }
        if (!state.isLoadingDetails && state.episodes.isEmpty() && state.detailError == null) {
            item {
                EmptyPanel(
                    title = "No episode guide available",
                    message = "TVMaze doesn't have episode information for this show yet.",
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        }
        seasonGroups.forEach { (season, episodes) ->
            item(key = "season-$season") {
                SeasonHeader(
                    season = season,
                    episodeCount = episodes.size,
                    watchedCount = episodes.count { it.id in state.watchedEpisodeIds },
                    expanded = expandedSeason == season,
                    onClick = {
                        expandedSeason = if (expandedSeason == season) -1 else season
                    },
                )
            }
            if (expandedSeason == season) {
                items(episodes, key = { "episode-${it.id}" }) { episode ->
                    EpisodeRow(
                        episode = episode,
                        watched = episode.id in state.watchedEpisodeIds,
                        onClick = { onEpisodeToggle(episode) },
                    )
                }
            }
        }
        item {
            Text(
                "Show and episode data provided by TVMaze.",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun ShowDetailHero(show: TvShow, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .background(Panel),
    ) {
        AsyncImage(
            model = show.imageUrl,
            contentDescription = "${show.name} artwork",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.55f),
                        0.28f to Color.Transparent,
                        0.52f to Color.Black.copy(alpha = 0.15f),
                        1.0f to Color.Black.copy(alpha = 0.9f),
                    ),
                ),
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp)
                .clip(CircleShape)
                .background(Night.copy(alpha = 0.6f)),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                show.network ?: show.status ?: "TV SERIES",
                color = Amber,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            )
            Text(
                show.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusPicker(
    status: WatchStatus?,
    onStatusChange: (WatchStatus?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber,
                contentColor = Night,
            ),
        ) {
            Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(status?.label ?: "Add to my shows", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(3.dp))
            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            WatchStatus.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(if (option == status) "✓  ${option.label}" else option.label)
                    },
                    onClick = {
                        expanded = false
                        onStatusChange(option)
                    },
                )
            }
            if (status != null) {
                DropdownMenuItem(
                    text = { Text("Remove from my shows", color = Color(0xFFFF8D82)) },
                    onClick = {
                        expanded = false
                        onStatusChange(null)
                    },
                )
            }
        }
    }
}

@Composable
private fun SeasonHeader(
    season: Int,
    episodeCount: Int,
    watchedCount: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (season == 0) "Specials" else "Season $season",
                color = TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "$watchedCount / $episodeCount watched",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Icon(
            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Collapse season" else "Expand season",
            tint = TextMuted,
        )
    }
}

@Composable
private fun EpisodeRow(
    episode: TvEpisode,
    watched: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            if (watched) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (watched) "Mark unwatched" else "Mark watched",
            tint = if (watched) Amber else TextMuted,
            modifier = Modifier
                .size(22.dp)
                .padding(top = 1.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val episodeNumber = episode.number?.let { "E${it.toString().padStart(2, '0')}" }
            Text(
                listOfNotNull(episodeNumber, episode.name).joinToString("  ·  "),
                color = if (watched) TextMuted else TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!episode.airDate.isNullOrBlank()) {
                Text(
                    episode.airDate,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            val summary = cleanHtml(episode.summary)
            if (summary.isNotBlank()) {
                Text(
                    summary,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RatingLabel(rating: Double?) {
    if (rating == null) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = Amber, modifier = Modifier.size(15.dp))
        Text(
            String.format(Locale.US, "%.1f", rating),
            color = TextMain,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusPill(status: WatchStatus) {
    val tint = when (status) {
        WatchStatus.WATCHLIST -> Lavender
        WatchStatus.WATCHING -> Amber
        WatchStatus.COMPLETED -> Color(0xFF8FD8AE)
        WatchStatus.DROPPED -> Color(0xFFE58D88)
    }
    Surface(
        color = tint.copy(alpha = 0.13f),
        contentColor = tint,
        shape = RoundedCornerShape(30.dp),
    ) {
        Text(
            status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun LoadingPanel(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = Amber, modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
        Text(message, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ErrorPanel(
    message: String,
    buttonLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(message, color = TextMain, style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PanelSoft, contentColor = Amber),
        ) {
            Text(buttonLabel)
        }
    }
}

@Composable
private fun EmptyPanel(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    buttonLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Panel)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            title,
            color = TextMain,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(message, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        if (buttonLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Night),
            ) {
                Text(buttonLabel)
            }
        }
    }
}

private fun cleanHtml(raw: String?): String =
    if (raw.isNullOrBlank()) "" else Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT).toString().trim()

package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.signOut
import com.example.data.FounderProfile
import com.example.data.FriendEntry
import com.example.data.LocalPhotoStore
import com.example.data.PortfolioRepository
import com.example.data.UiState
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.LiveWallpaperCanvas
import com.example.ui.screens.FounderScreen
import com.example.ui.screens.FriendDetailModal
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.PublishScreen
import com.example.ui.screens.TrophyHallScreen
import com.example.ui.screens.TrophyUnlockedModal
import com.example.ui.theme.FriendsPortfolioTheme
import com.example.ui.theme.GlassThemeId
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.paletteForTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PortfolioApp(
    currentUserId: String,
    userEmail: String?
) {
    val viewModel: PortfolioViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                PortfolioViewModel(
                    repository = PortfolioRepository(db),
                    photoStore = LocalPhotoStore(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val founderState by viewModel.founderState.collectAsStateWithLifecycle()
    val friendsState by viewModel.friendsState.collectAsStateWithLifecycle()
    val filteredFriends by viewModel.filteredFriends.collectAsStateWithLifecycle()
    val localPhotoPaths by viewModel.localPhotoPaths.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedBloodFilter by viewModel.selectedBloodFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val publishForm by viewModel.publishForm.collectAsStateWithLifecycle()
    val confettiBurstId by viewModel.confettiBurstId.collectAsStateWithLifecycle()
    val newlyUnlockedFriend by viewModel.newlyUnlockedFriend.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { PortfolioTab.entries.size })
    var selectedFriendId by remember { mutableStateOf<String?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val allFriends = (friendsState as? UiState.Success)?.data ?: emptyList()
    val activeFounder = (founderState as? UiState.Success)?.data ?: FounderProfile(userId = currentUserId)
    val selectedFriend: FriendEntry? = remember(selectedFriendId, allFriends) {
        allFriends.firstOrNull { it.id == selectedFriendId }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Back gesture on secondary tabs smoothly slides back to Founder page
    BackHandler(enabled = pagerState.currentPage != PortfolioTab.FOUNDER.index) {
        scope.launch { pagerState.animateScrollToPage(PortfolioTab.FOUNDER.index) }
    }

    fun navigateToTab(tab: PortfolioTab) {
        scope.launch { pagerState.animateScrollToPage(tab.index) }
    }

    FriendsPortfolioTheme(themeId = selectedTheme) {
        val palette = LocalGlassPalette.current

        Box(modifier = Modifier.fillMaxSize()) {
            // Animated Live Glassmorphic Wallpaper Canvas
            LiveWallpaperCanvas(palette = palette)

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 720.dp

                Column(modifier = Modifier.fillMaxSize()) {
                    // Glassmorphic Top Header Bar
                    TopPortfolioBar(
                        selectedTheme = selectedTheme,
                        onBrandClick = { navigateToTab(PortfolioTab.FOUNDER) },
                        onSelectTheme = { viewModel.selectTheme(it) },
                        onSignOutClick = { showSignOutDialog = true }
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (useNavRail) {
                            GlassNavigationRail(
                                currentIndex = pagerState.currentPage,
                                onSelectTab = { navigateToTab(it) }
                            )
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) { page ->
                            when (PortfolioTab.fromIndex(page)) {
                                PortfolioTab.FOUNDER -> FounderScreen(
                                    founderState = founderState,
                                    friends = allFriends,
                                    founderPhotoPath = localPhotoPaths[LocalPhotoStore.FOUNDER_SLOT],
                                    onPickFounderPhoto = { viewModel.saveFounderPhoto(it) },
                                    onOpenTrophyKey = { navigateToTab(PortfolioTab.PUBLISH) },
                                    onOpenTrophyHall = { navigateToTab(PortfolioTab.TROPHIES) },
                                    onSaveFounderProfile = { name, school, blood, aim, city, hobby, motto, goodFriends, onDone ->
                                        viewModel.updateFounderProfile(
                                            name, school, blood, aim, city, hobby, motto, goodFriends, onDone
                                        )
                                    }
                                )
                                PortfolioTab.FRIENDS -> FriendsScreen(
                                    friendsState = friendsState,
                                    filteredFriends = filteredFriends,
                                    localPhotoPaths = localPhotoPaths,
                                    searchQuery = searchQuery,
                                    selectedBloodFilter = selectedBloodFilter,
                                    sortOption = sortOption,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onBloodFilterChange = { viewModel.updateBloodFilter(it) },
                                    onSortOptionChange = { viewModel.updateSortOption(it) },
                                    onNavigateToPublish = { navigateToTab(PortfolioTab.PUBLISH) },
                                    onFriendClick = { selectedFriendId = it.id }
                                )
                                PortfolioTab.PUBLISH -> PublishScreen(
                                    formState = publishForm,
                                    draftPhotoPath = localPhotoPaths[LocalPhotoStore.DRAFT_FRIEND_SLOT],
                                    onUpdateForm = { viewModel.updatePublishForm(it) },
                                    onPickDraftPhoto = { viewModel.saveDraftFriendPhoto(it) },
                                    onClearDraftPhoto = { viewModel.clearDraftFriendPhoto() },
                                    onSubmit = {
                                        viewModel.submitNewFriend(onPublished = {})
                                    }
                                )
                                PortfolioTab.TROPHIES -> TrophyHallScreen(
                                    founder = activeFounder,
                                    friends = allFriends,
                                    onCelebrateFounder = { viewModel.triggerConfetti() },
                                    onCheerFriend = { viewModel.cheerTrophy(it) },
                                    onTrophyClick = { selectedFriendId = it.id },
                                    onNavigateToPublish = { navigateToTab(PortfolioTab.PUBLISH) }
                                )
                            }
                        }
                    }

                    if (!useNavRail) {
                        GlassBottomNavBar(
                            currentIndex = pagerState.currentPage,
                            onSelectTab = { navigateToTab(it) }
                        )
                    }
                }
            }

            // Snackbar notifications above bottom navigation
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 78.dp, start = 16.dp, end = 16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = palette.bg2.copy(alpha = 0.96f),
                    contentColor = palette.textPrimary,
                    actionColor = palette.gold,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Full-bleed Celebratory Confetti Overlay
            ConfettiOverlay(burstId = confettiBurstId)
        }

        // Friend Detail & Edit Modal
        if (selectedFriend != null) {
            FriendDetailModal(
                friend = selectedFriend,
                localPhotoPath = localPhotoPaths[LocalPhotoStore.friendSlot(selectedFriend.id)],
                onDismiss = { selectedFriendId = null },
                onToggleCloseFriend = { viewModel.toggleCloseFriend(it) },
                onCheerTrophy = { viewModel.cheerTrophy(it) },
                onUpdatePhoto = { friendId, uri -> viewModel.saveFriendPhoto(friendId, uri) },
                onEditSave = { id, name, nick, school, blood, city, hobby, aim, note, preset, title, close, done ->
                    viewModel.editFriend(
                        id, name, nick, school, blood, city, hobby, aim, note, preset, title, close, done
                    )
                },
                onDelete = { friend ->
                    viewModel.deleteFriend(friend) {
                        selectedFriendId = null
                    }
                }
            )
        }

        // Celebratory Trophy Unlocked Modal
        if (newlyUnlockedFriend != null) {
            TrophyUnlockedModal(
                friend = newlyUnlockedFriend!!,
                onDismiss = { viewModel.dismissUnlockedModal() },
                onViewTrophyHall = {
                    viewModel.dismissUnlockedModal()
                    navigateToTab(PortfolioTab.TROPHIES)
                }
            )
        }

        // Sign Out Confirmation Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                containerColor = palette.bg2,
                titleContentColor = palette.textPrimary,
                textContentColor = palette.textMuted,
                title = { Text(stringResource(R.string.auth_sign_out)) },
                text = {
                    Text(
                        buildString {
                            append("Sign out of your cloud-synced Friends Portfolio")
                            if (!userEmail.isNullOrBlank()) append(" ($userEmail)")
                            append("?")
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSignOutDialog = false
                            signOut(credentialManager = credentialManager, scope = scope)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = Color(0xFF140D24)
                        ),
                        modifier = Modifier.testTag("confirm_sign_out_button")
                    ) {
                        Text(stringResource(R.string.auth_sign_out), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text("Stay Signed In", color = palette.textPrimary)
                    }
                }
            )
        }
    }
}

@Composable
private fun TopPortfolioBar(
    selectedTheme: GlassThemeId,
    onBrandClick: () -> Unit,
    onSelectTheme: (GlassThemeId) -> Unit,
    onSignOutClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.topBarBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand emblem & title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onBrandClick)
                    .padding(vertical = 2.dp)
                    .testTag("brand_header_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(palette.accent, palette.accent2))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.brand_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.brand_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 4 Interactive Theme Dots + Sign Out
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassThemeId.entries.forEach { themeId ->
                    val isSelected = selectedTheme == themeId
                    val dotScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.18f else 1f,
                        animationSpec = tween(220),
                        label = "dotScale"
                    )
                    val dotPalette = paletteForTheme(themeId)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .clickable(role = Role.RadioButton) { onSelectTheme(themeId) }
                            .semantics {
                                contentDescription = "${themeId.displayName} theme"
                            }
                            .testTag("theme_dot_${themeId.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(dotPalette.accent, dotPalette.accent2)
                                    )
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                IconButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.testTag("sign_out_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(R.string.cd_sign_out),
                        tint = palette.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = palette.glassBorder)
    }
}

private data class NavTabSpec(
    val tab: PortfolioTab,
    val shortLabel: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val NavTabSpecs = listOf(
    NavTabSpec(PortfolioTab.FOUNDER, "Founder", Icons.Filled.Person, Icons.Outlined.Person),
    NavTabSpec(PortfolioTab.FRIENDS, "Friends", Icons.Filled.Groups, Icons.Outlined.Groups),
    NavTabSpec(PortfolioTab.PUBLISH, "Publish", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    NavTabSpec(PortfolioTab.TROPHIES, "Trophies", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
)

@Composable
private fun GlassBottomNavBar(
    currentIndex: Int,
    onSelectTab: (PortfolioTab) -> Unit
) {
    val palette = LocalGlassPalette.current
    Surface(
        color = palette.topBarBg.copy(alpha = 0.86f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = palette.glassBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabSpecs.forEach { spec ->
                    val selected = currentIndex == spec.tab.index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) {
                                    Brush.linearGradient(
                                        listOf(
                                            palette.accent.copy(alpha = 0.28f),
                                            palette.accent2.copy(alpha = 0.24f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable(role = Role.Tab) { onSelectTab(spec.tab) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("bottom_nav_${spec.tab.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                            contentDescription = spec.shortLabel,
                            tint = if (selected) Color.White else palette.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = spec.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else palette.textMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassNavigationRail(
    currentIndex: Int,
    onSelectTab: (PortfolioTab) -> Unit
) {
    val palette = LocalGlassPalette.current
    NavigationRail(
        containerColor = palette.topBarBg.copy(alpha = 0.72f),
        contentColor = palette.textPrimary,
        modifier = Modifier
            .fillMaxHeight()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        NavTabSpecs.forEach { spec ->
            val selected = currentIndex == spec.tab.index
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(spec.tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                        contentDescription = spec.shortLabel
                    )
                },
                label = { Text(spec.shortLabel, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = palette.textMuted,
                    unselectedTextColor = palette.textMuted,
                    indicatorColor = palette.accent.copy(alpha = 0.30f)
                ),
                modifier = Modifier.testTag("rail_nav_${spec.tab.name.lowercase()}")
            )
        }
    }
}

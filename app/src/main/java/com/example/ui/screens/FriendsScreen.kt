package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AvatarPreset
import com.example.data.BloodGroupOptions
import com.example.data.FriendEntry
import com.example.data.FriendSortOption
import com.example.data.LocalPhotoStore
import com.example.data.PortfolioLimits
import com.example.data.TrophyTitleOptions
import com.example.data.UiState
import com.example.ui.components.FriendAvatar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInputField
import com.example.ui.components.GlassPill
import com.example.ui.components.GoldenTrophyEmblem
import com.example.ui.theme.LocalGlassPalette

@Composable
fun FriendsScreen(
    friendsState: UiState<List<FriendEntry>>,
    filteredFriends: List<FriendEntry>,
    localPhotoPaths: Map<String, String>,
    searchQuery: String,
    selectedBloodFilter: String,
    sortOption: FriendSortOption,
    onSearchQueryChange: (String) -> Unit,
    onBloodFilterChange: (String) -> Unit,
    onSortOptionChange: (FriendSortOption) -> Unit,
    onNavigateToPublish: () -> Unit,
    onFriendClick: (FriendEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val filterOptions = remember { listOf("All", "Close") + BloodGroupOptions.dropLast(1) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 270.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1080.dp)
        ) {
            // Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.friends_section_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.textPrimary
                        )
                        Text(
                            text = stringResource(R.string.friends_section_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onNavigateToPublish,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = Color(0xFF140D24)
                        ),
                        modifier = Modifier.testTag("friends_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Join", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search, Filter & Sort Controls
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassInputField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = "Search Friends",
                        placeholder = stringResource(R.string.friends_search_hint),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = palette.textMuted
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = palette.textMuted
                                    )
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("friends_search_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterOptions.forEach { option ->
                            val selected = selectedBloodFilter == option
                            GlassPill(
                                text = if (option == "Close") "★ Good Friends" else option,
                                containerColor = if (selected) palette.accent.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.08f),
                                borderColor = if (selected) palette.accent else palette.glassBorder,
                                textColor = if (selected) Color.White else palette.textMuted,
                                modifier = Modifier.clickable { onBloodFilterChange(option) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort:",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textMuted
                        )
                        FriendSortOption.entries.forEach { option ->
                            val selected = sortOption == option
                            GlassPill(
                                text = option.label,
                                containerColor = if (selected) palette.accent2.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.06f),
                                borderColor = if (selected) palette.accent2 else palette.glassBorder.copy(alpha = 0.7f),
                                textColor = if (selected) Color.White else palette.textMuted,
                                modifier = Modifier.clickable { onSortOptionChange(option) }
                            )
                        }
                    }
                }
            }

            when (friendsState) {
                is UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = palette.accent)
                        }
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = palette.bloodBorder
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Unable to load friends",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.bloodText
                                )
                                Text(
                                    text = friendsState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val allCount = friendsState.data.size
                    if (allCount == 0) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(palette.accent, palette.accent2))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.friends_empty_title),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.friends_empty_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = onNavigateToPublish,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.gold,
                                            contentColor = Color(0xFF1F1300)
                                        ),
                                        modifier = Modifier.testTag("empty_publish_first_friend_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.friends_publish_cta),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else if (filteredFriends.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No matching friend cards",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = "Try clearing your search or switching the blood group filter.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textMuted
                                    )
                                    TextButton(onClick = {
                                        onSearchQueryChange("")
                                        onBloodFilterChange("All")
                                    }) {
                                        Text("Reset Filters", color = palette.accent)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredFriends, key = { it.id }) { friend ->
                            FriendGlassCard(
                                friend = friend,
                                localPhotoPath = localPhotoPaths[LocalPhotoStore.friendSlot(friend.id)],
                                onClick = { onFriendClick(friend) }
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FriendGlassCard(
    friend: FriendEntry,
    localPhotoPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("friend_card_${friend.id}"),
        cornerRadius = 22.dp,
        borderColor = if (friend.closeFriend) palette.gold.copy(alpha = 0.55f) else palette.glassBorder
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FriendAvatar(
                    name = friend.name,
                    localPhotoPath = localPhotoPath,
                    avatarPreset = AvatarPreset.fromId(friend.avatarPreset),
                    size = 64.dp,
                    cornerRadius = 16.dp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = friend.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (friend.closeFriend) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Good Friend",
                                tint = palette.gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = buildString {
                            if (friend.nickname.isNotBlank()) append("\"${friend.nickname}\" • ")
                            append(friend.school)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlassPill(
                            text = friend.bloodGroup,
                            containerColor = palette.bloodBg,
                            borderColor = palette.bloodBorder,
                            textColor = palette.bloodText
                        )
                        if (friend.city.isNotBlank()) {
                            GlassPill(
                                text = friend.city,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                borderColor = palette.glassBorder.copy(alpha = 0.6f),
                                textColor = palette.textMuted
                            )
                        }
                    }
                }
            }

            Text(
                text = friend.aimInLife,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = palette.gold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = friend.trophyId,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.goldSoftText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "View Card",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.accent
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FriendDetailModal(
    friend: FriendEntry,
    localPhotoPath: String?,
    onDismiss: () -> Unit,
    onToggleCloseFriend: (FriendEntry) -> Unit,
    onCheerTrophy: (FriendEntry) -> Unit,
    onUpdatePhoto: (String, Uri) -> Unit,
    onEditSave: (
        friendId: String,
        name: String,
        nickname: String,
        school: String,
        bloodGroup: String,
        city: String,
        hobby: String,
        aimInLife: String,
        note: String,
        avatarPreset: AvatarPreset,
        trophyTitle: String,
        closeFriend: Boolean,
        onComplete: () -> Unit
    ) -> Unit,
    onDelete: (FriendEntry) -> Unit
) {
    val palette = LocalGlassPalette.current
    var isEditing by remember(friend.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUpdatePhoto(friend.id, uri)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp),
            backgroundColor = palette.bg1.copy(alpha = 0.95f),
            borderColor = palette.accent.copy(alpha = 0.55f)
        ) {
            if (!isEditing) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassPill(
                            text = "${friend.trophyId} • ${friend.trophyTitle}",
                            containerColor = palette.gold.copy(alpha = 0.18f),
                            borderColor = palette.gold.copy(alpha = 0.55f),
                            textColor = palette.goldSoftText,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = palette.gold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_close_dialog),
                                tint = palette.textPrimary
                            )
                        }
                    }

                    // Avatar + Core identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box {
                            FriendAvatar(
                                name = friend.name,
                                localPhotoPath = localPhotoPath,
                                avatarPreset = AvatarPreset.fromId(friend.avatarPreset),
                                size = 88.dp,
                                cornerRadius = 22.dp
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent)
                                    .border(1.5.dp, palette.bg1, CircleShape)
                                    .clickable {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Update friend photo",
                                    tint = Color(0xFF140D24),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = friend.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.textPrimary
                            )
                            if (friend.nickname.isNotBlank()) {
                                Text(
                                    text = "\"${friend.nickname}\"",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.accent2
                                )
                            }
                            Text(
                                text = friend.school,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.accent
                            )
                        }
                    }

                    // Pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassPill(
                            text = "Blood: ${friend.bloodGroup}",
                            containerColor = palette.bloodBg,
                            borderColor = palette.bloodBorder,
                            textColor = palette.bloodText
                        )
                        if (friend.city.isNotBlank()) {
                            GlassPill(text = "City: ${friend.city}")
                        }
                        if (friend.hobby.isNotBlank()) {
                            GlassPill(text = "Hobby: ${friend.hobby}")
                        }
                    }

                    // Aim in Life
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, palette.gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AIM IN LIFE",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.goldSoftText
                        )
                        Text(
                            text = "\"${friend.aimInLife}\"",
                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = palette.textPrimary
                        )
                    }

                    if (friend.note.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, palette.glassBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "SPECIAL MEMORY & NOTE",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accent
                            )
                            Text(
                                text = friend.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textPrimary
                            )
                        }
                    }

                    // Trophy Certificate Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.gold.copy(alpha = 0.12f))
                            .border(1.dp, palette.gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            GoldenTrophyEmblem(size = 44.dp)
                            Column {
                                Text(
                                    text = friend.trophyTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.goldSoftText
                                )
                                Text(
                                    text = "${friend.cheersCount} Celebrations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }
                        }
                        Button(
                            onClick = { onCheerTrophy(friend) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.gold,
                                contentColor = Color(0xFF1F1300)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("modal_celebrate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cheer", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bottom Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassPill(
                            text = if (friend.closeFriend) "Good Friend" else "Mark Close",
                            containerColor = if (friend.closeFriend) palette.gold.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                            borderColor = if (friend.closeFriend) palette.gold else palette.glassBorder,
                            textColor = if (friend.closeFriend) palette.goldSoftText else palette.textPrimary,
                            leadingIcon = {
                                Icon(
                                    imageVector = if (friend.closeFriend) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = palette.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier
                                .clickable { onToggleCloseFriend(friend) }
                                .testTag("modal_toggle_close_friend")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .testTag("modal_edit_friend_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit friend",
                                    tint = palette.textPrimary
                                )
                            }
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(palette.bloodBg)
                                    .testTag("modal_delete_friend_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete friend",
                                    tint = palette.bloodText
                                )
                            }
                        }
                    }
                }
            } else {
                EditFriendInlineForm(
                    friend = friend,
                    onCancel = { isEditing = false },
                    onSave = { name, nick, school, blood, city, hobby, aim, note, preset, title, close ->
                        onEditSave(
                            friend.id, name, nick, school, blood, city, hobby, aim, note, preset, title, close
                        ) {
                            isEditing = false
                            onDismiss()
                        }
                    }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = palette.bg2,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textMuted,
            title = { Text("Remove ${friend.name}?") },
            text = { Text("This permanently deletes their friend card and ${friend.trophyId} from your cloud portfolio.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(friend)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF43F5E),
                        contentColor = Color.White
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = palette.textPrimary)
                }
            }
        )
    }
}

@Composable
private fun EditFriendInlineForm(
    friend: FriendEntry,
    onCancel: () -> Unit,
    onSave: (
        name: String,
        nickname: String,
        school: String,
        bloodGroup: String,
        city: String,
        hobby: String,
        aimInLife: String,
        note: String,
        avatarPreset: AvatarPreset,
        trophyTitle: String,
        closeFriend: Boolean
    ) -> Unit
) {
    val palette = LocalGlassPalette.current
    var name by remember { mutableStateOf(friend.name) }
    var nickname by remember { mutableStateOf(friend.nickname) }
    var school by remember { mutableStateOf(friend.school) }
    var bloodGroup by remember { mutableStateOf(friend.bloodGroup) }
    var city by remember { mutableStateOf(friend.city) }
    var hobby by remember { mutableStateOf(friend.hobby) }
    var aimInLife by remember { mutableStateOf(friend.aimInLife) }
    var note by remember { mutableStateOf(friend.note) }
    var avatarPreset by remember { mutableStateOf(AvatarPreset.fromId(friend.avatarPreset)) }
    var trophyTitle by remember { mutableStateOf(friend.trophyTitle) }
    var closeFriend by remember { mutableStateOf(friend.closeFriend) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Edit Friend Card",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.textPrimary
        )

        GlassInputField(
            value = name,
            onValueChange = { name = it.take(PortfolioLimits.MAX_NAME) },
            label = "Full Name *",
            modifier = Modifier.fillMaxWidth()
        )
        GlassInputField(
            value = nickname,
            onValueChange = { nickname = it.take(PortfolioLimits.MAX_NICKNAME) },
            label = "Nickname",
            modifier = Modifier.fillMaxWidth()
        )
        GlassInputField(
            value = school,
            onValueChange = { school = it.take(PortfolioLimits.MAX_SCHOOL) },
            label = "School / College *",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BloodGroupOptions.forEach { option ->
                val selected = bloodGroup == option
                GlassPill(
                    text = option,
                    containerColor = if (selected) palette.accent.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.08f),
                    borderColor = if (selected) palette.accent else palette.glassBorder,
                    modifier = Modifier.clickable { bloodGroup = option }
                )
            }
        }

        GlassInputField(
            value = city,
            onValueChange = { city = it.take(PortfolioLimits.MAX_CITY) },
            label = "City",
            modifier = Modifier.fillMaxWidth()
        )
        GlassInputField(
            value = hobby,
            onValueChange = { hobby = it.take(PortfolioLimits.MAX_HOBBY) },
            label = "Hobby",
            modifier = Modifier.fillMaxWidth()
        )
        GlassInputField(
            value = aimInLife,
            onValueChange = { aimInLife = it.take(PortfolioLimits.MAX_AIM) },
            label = "Aim in Life *",
            singleLine = false,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        GlassInputField(
            value = note,
            onValueChange = { note = it.take(PortfolioLimits.MAX_NOTE) },
            label = "Memory / Note",
            singleLine = false,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrophyTitleOptions.forEach { option ->
                val selected = trophyTitle == option
                GlassPill(
                    text = option,
                    containerColor = if (selected) palette.gold.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f),
                    borderColor = if (selected) palette.gold else palette.glassBorder,
                    textColor = if (selected) palette.goldSoftText else palette.textMuted,
                    modifier = Modifier.clickable { trophyTitle = option }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvatarPreset.entries.forEach { preset ->
                val selected = avatarPreset == preset
                GlassPill(
                    text = preset.label,
                    containerColor = if (selected) palette.accent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                    borderColor = if (selected) palette.accent else palette.glassBorder,
                    modifier = Modifier.clickable { avatarPreset = preset }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Highlight in Good Friends Circle",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary
            )
            Switch(
                checked = closeFriend,
                onCheckedChange = { closeFriend = it },
                colors = SwitchDefaults.colors(checkedThumbColor = palette.gold)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = palette.textMuted)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSave(
                        name, nickname, school, bloodGroup, city, hobby,
                        aimInLife, note, avatarPreset, trophyTitle, closeFriend
                    )
                },
                enabled = name.trim().isNotEmpty() && school.trim().isNotEmpty() && aimInLife.trim().isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = Color(0xFF140D24)
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

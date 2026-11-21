package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.BloodGroupOptions
import com.example.data.FounderProfile
import com.example.data.FriendEntry
import com.example.data.PortfolioLimits
import com.example.data.UiState
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInputField
import com.example.ui.components.GlassPill
import com.example.ui.theme.LocalGlassPalette
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FounderScreen(
    founderState: UiState<FounderProfile>,
    friends: List<FriendEntry>,
    founderPhotoPath: String?,
    onPickFounderPhoto: (Uri) -> Unit,
    onOpenTrophyKey: () -> Unit,
    onOpenTrophyHall: () -> Unit,
    onSaveFounderProfile: (
        name: String,
        school: String,
        bloodGroup: String,
        aimInLife: String,
        city: String,
        hobby: String,
        motto: String,
        goodFriendsRaw: String,
        onComplete: () -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    var showEditDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickFounderPhoto(uri)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1040.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Prominent Main-Page Golden Trophy CTA Banner
            TrophyCtaBanner(
                trophiesCount = friends.size + 1,
                onOpenTrophyKey = onOpenTrophyKey,
                onOpenTrophyHall = onOpenTrophyHall
            )

            when (founderState) {
                is UiState.Loading -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = palette.accent)
                        }
                    }
                }
                is UiState.Error -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = palette.bloodBorder
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Could not load Founder Profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.bloodText
                            )
                            Text(
                                text = founderState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val profile = founderState.data
                    val closeFriendNames = remember(profile.goodFriends, friends) {
                        val dynamicClose = friends.filter { it.closeFriend }.map { it.name }
                        val combined = (dynamicClose + profile.goodFriends)
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                        if (combined.isEmpty()) listOf("Add your close friends!") else combined
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            val isWide = maxWidth >= 660.dp
                            if (isWide) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    FounderPortraitBlock(
                                        photoPath = founderPhotoPath,
                                        onUpdatePhotoClick = {
                                            photoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.width(245.dp)
                                    )
                                    FounderDetailsBlock(
                                        profile = profile,
                                        friendsCount = friends.size,
                                        totalCheers = friends.sumOf { it.cheersCount },
                                        goodFriendNames = closeFriendNames,
                                        onEditClick = { showEditDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    FounderPortraitBlock(
                                        photoPath = founderPhotoPath,
                                        onUpdatePhotoClick = {
                                            photoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.width(210.dp)
                                    )
                                    FounderDetailsBlock(
                                        profile = profile,
                                        friendsCount = friends.size,
                                        totalCheers = friends.sumOf { it.cheersCount },
                                        goodFriendNames = closeFriendNames,
                                        onEditClick = { showEditDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    if (showEditDialog) {
                        EditFounderDialog(
                            profile = profile,
                            onDismiss = { showEditDialog = false },
                            onSave = { name, school, blood, aim, city, hobby, motto, goodFriendsRaw ->
                                onSaveFounderProfile(
                                    name, school, blood, aim, city, hobby, motto, goodFriendsRaw
                                ) {
                                    showEditDialog = false
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TrophyCtaBanner(
    trophiesCount: Int,
    onOpenTrophyKey: () -> Unit,
    onOpenTrophyHall: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val infinite = rememberInfiniteTransition(label = "trophyCtaPulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, palette.gold.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            palette.gold.copy(alpha = 0.20f),
                            palette.accent2.copy(alpha = 0.16f),
                            palette.accent.copy(alpha = 0.16f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.trophy_cta_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEF3C7)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.trophy_cta_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassPill(
                        text = "$trophiesCount Trophies",
                        containerColor = palette.gold.copy(alpha = 0.20f),
                        borderColor = palette.gold.copy(alpha = 0.55f),
                        textColor = palette.goldSoftText,
                        modifier = Modifier
                            .clickable(onClick = onOpenTrophyHall)
                            .testTag("founder_trophy_count_pill")
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenTrophyKey,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF1F1300)
                        ),
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFFBBF24))
                                )
                            )
                            .testTag("trophy_key_cta_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.trophy_cta_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    TextButton(
                        onClick = onOpenTrophyHall,
                        modifier = Modifier.testTag("founder_view_trophy_hall_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = palette.goldSoftText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hall of Fame",
                            color = palette.goldSoftText,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FounderPortraitBlock(
    photoPath: String?,
    onUpdatePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(2.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
    ) {
        if (!photoPath.isNullOrBlank() && File(photoPath).exists()) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = stringResource(R.string.cd_founder_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_founder_arpan_1795225036979),
                contentDescription = stringResource(R.string.cd_founder_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Surface(
            onClick = onUpdatePhotoClick,
            shape = CircleShape,
            color = Color(0xC80F172A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .testTag("founder_update_photo_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = palette.textPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = stringResource(R.string.founder_update_photo),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FounderDetailsBlock(
    profile: FounderProfile,
    friendsCount: Int,
    totalCheers: Long,
    goodFriendNames: List<String>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Badges row + Edit action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                GlassPill(
                    text = stringResource(R.string.founder_badge_creator),
                    containerColor = palette.gold.copy(alpha = 0.18f),
                    borderColor = palette.gold.copy(alpha = 0.50f),
                    textColor = palette.goldSoftText,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = palette.gold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
                if (profile.bloodGroup.isNotBlank()) {
                    GlassPill(
                        text = "Blood: ${profile.bloodGroup}",
                        containerColor = palette.bloodBg,
                        borderColor = palette.bloodBorder,
                        textColor = palette.bloodText,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = palette.bloodText,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    )
                }
                GlassPill(
                    text = stringResource(R.string.founder_badge_host),
                    containerColor = palette.accent.copy(alpha = 0.18f),
                    borderColor = palette.accent.copy(alpha = 0.45f),
                    textColor = palette.textPrimary
                )
            }

            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, palette.glassBorder, CircleShape)
                    .testTag("founder_edit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.founder_edit_profile),
                    tint = palette.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Playfair Display Name & School
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = profile.name.ifBlank { "Arpan" },
                style = MaterialTheme.typography.displayLarge,
                color = palette.textPrimary
            )
            if (profile.school.isNotBlank()) {
                Text(
                    text = profile.school,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Aim in Life callout box with gold accent strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, palette.glassBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(84.dp)
                    .background(palette.gold)
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.founder_aim_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.goldSoftText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\"${profile.aimInLife.ifBlank { "Building inspiring experiences and celebrating friendships." }}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    color = palette.textPrimary
                )
            }
        }

        // Info Grid (2 columns)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FounderInfoCell(
                    label = "CITY",
                    value = profile.city.ifBlank { "Not set" },
                    modifier = Modifier.weight(1f)
                )
                FounderInfoCell(
                    label = "HOBBY",
                    value = profile.hobby.ifBlank { "Not set" },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FounderInfoCell(
                    label = "FRIENDS PUBLISHED",
                    value = "$friendsCount Cards",
                    modifier = Modifier.weight(1f)
                )
                FounderInfoCell(
                    label = "TROPHY CHEERS",
                    value = "$totalCheers Cheers",
                    modifier = Modifier.weight(1f)
                )
            }
            if (profile.motto.isNotBlank()) {
                FounderInfoCell(
                    label = "SIGNATURE MOTTO",
                    value = profile.motto,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Good Friends Circle box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                )
                .border(1.dp, palette.glassBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.founder_good_friends_title).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textMuted
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                goodFriendNames.forEach { friendName ->
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        palette.accent.copy(alpha = 0.28f),
                                        palette.accent2.copy(alpha = 0.24f)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.26f), CircleShape)
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = palette.gold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = friendName,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FounderInfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary
        )
    }
}

@Composable
private fun EditFounderDialog(
    profile: FounderProfile,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        school: String,
        bloodGroup: String,
        aimInLife: String,
        city: String,
        hobby: String,
        motto: String,
        goodFriendsRaw: String
    ) -> Unit
) {
    val palette = LocalGlassPalette.current
    var name by remember { mutableStateOf(profile.name) }
    var school by remember { mutableStateOf(profile.school) }
    var bloodGroup by remember { mutableStateOf(profile.bloodGroup) }
    var aimInLife by remember { mutableStateOf(profile.aimInLife) }
    var city by remember { mutableStateOf(profile.city) }
    var hobby by remember { mutableStateOf(profile.hobby) }
    var motto by remember { mutableStateOf(profile.motto) }
    var goodFriendsRaw by remember { mutableStateOf(profile.goodFriends.joinToString(", ")) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 540.dp),
            backgroundColor = palette.bg1.copy(alpha = 0.94f),
            borderColor = palette.accent.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customize Founder Card",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close_dialog),
                            tint = palette.textPrimary
                        )
                    }
                }

                GlassInputField(
                    value = name,
                    onValueChange = { name = it.take(PortfolioLimits.MAX_NAME) },
                    label = "Founder Name *",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassInputField(
                    value = school,
                    onValueChange = { school = it.take(PortfolioLimits.MAX_SCHOOL) },
                    label = "School / Academy",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Blood Group",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textMuted
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
                            containerColor = if (selected) palette.accent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                            borderColor = if (selected) palette.accent else palette.glassBorder,
                            modifier = Modifier.clickable { bloodGroup = option }
                        )
                    }
                }

                GlassInputField(
                    value = aimInLife,
                    onValueChange = { aimInLife = it.take(PortfolioLimits.MAX_AIM) },
                    label = "Aim in Life",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassInputField(
                    value = city,
                    onValueChange = { city = it.take(PortfolioLimits.MAX_CITY) },
                    label = "City / Hometown",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassInputField(
                    value = hobby,
                    onValueChange = { hobby = it.take(PortfolioLimits.MAX_HOBBY) },
                    label = "Favorite Hobby",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassInputField(
                    value = motto,
                    onValueChange = { motto = it.take(PortfolioLimits.MAX_MOTTO) },
                    label = "Personal Motto",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassInputField(
                    value = goodFriendsRaw,
                    onValueChange = { goodFriendsRaw = it },
                    label = "Good Friends (comma-separated)",
                    placeholder = "e.g., Rohan, Ananya, Kabir",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = palette.textMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(name, school, bloodGroup, aimInLife, city, hobby, motto, goodFriendsRaw)
                        },
                        enabled = name.trim().isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = Color(0xFF140D24)
                        ),
                        modifier = Modifier.testTag("save_founder_profile_button")
                    ) {
                        Text("Save to Cloud", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

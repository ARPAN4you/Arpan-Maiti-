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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AvatarPreset
import com.example.data.BloodGroupOptions
import com.example.data.FriendEntry
import com.example.data.PortfolioLimits
import com.example.data.TrophyTitleOptions
import com.example.ui.PublishFormState
import com.example.ui.components.FriendAvatar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInputField
import com.example.ui.components.GlassPill
import com.example.ui.components.GoldenTrophyEmblem
import com.example.ui.theme.LocalGlassPalette

@Composable
fun PublishScreen(
    formState: PublishFormState,
    draftPhotoPath: String?,
    onUpdateForm: ((PublishFormState) -> PublishFormState) -> Unit,
    onPickDraftPhoto: (Uri) -> Unit,
    onClearDraftPhoto: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickDraftPhoto(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1040.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.publish_section_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = palette.textPrimary
                )
                Text(
                    text = stringResource(R.string.publish_section_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 760.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        PublishFormCard(
                            formState = formState,
                            hasDraftPhoto = !draftPhotoPath.isNullOrBlank(),
                            onUpdateForm = onUpdateForm,
                            onPickPhoto = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClearPhoto = onClearDraftPhoto,
                            onSubmit = onSubmit,
                            modifier = Modifier.weight(1.3f)
                        )
                        LivePreviewCard(
                            formState = formState,
                            draftPhotoPath = draftPhotoPath,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        LivePreviewCard(
                            formState = formState,
                            draftPhotoPath = draftPhotoPath,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PublishFormCard(
                            formState = formState,
                            hasDraftPhoto = !draftPhotoPath.isNullOrBlank(),
                            onUpdateForm = onUpdateForm,
                            onPickPhoto = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClearPhoto = onClearDraftPhoto,
                            onSubmit = onSubmit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LivePreviewCard(
    formState: PublishFormState,
    draftPhotoPath: String?,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    GlassCard(
        modifier = modifier,
        borderColor = palette.accent.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.publish_preview_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.accent,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FriendAvatar(
                    name = formState.name.ifBlank { "New Friend" },
                    localPhotoPath = draftPhotoPath,
                    avatarPreset = formState.avatarPreset,
                    size = 66.dp,
                    cornerRadius = 18.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formState.name.ifBlank { "Your Friend's Name" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (formState.closeFriend) {
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
                            if (formState.nickname.isNotBlank()) append("\"${formState.nickname}\" • ")
                            append(formState.school.ifBlank { "School / Academy" })
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlassPill(
                            text = formState.bloodGroup,
                            containerColor = palette.bloodBg,
                            borderColor = palette.bloodBorder,
                            textColor = palette.bloodText
                        )
                        if (formState.city.isNotBlank()) {
                            GlassPill(text = formState.city)
                        }
                    }
                }
            }

            Text(
                text = formState.aimInLife.ifBlank { "Their Aim in Life will shine right here on their live glass card." },
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = if (formState.aimInLife.isBlank()) palette.textMuted else palette.textPrimary,
                maxLines = 3,
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
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formState.trophyTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.goldSoftText
                    )
                }
                Text(
                    text = "TRP-NEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.gold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PublishFormCard(
    formState: PublishFormState,
    hasDraftPhoto: Boolean,
    onUpdateForm: ((PublishFormState) -> PublishFormState) -> Unit,
    onPickPhoto: () -> Unit,
    onClearPhoto: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Photo picker & Avatar Presets
            Text(
                text = "Portrait & Avatar Aura",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onPickPhoto,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, palette.glassBorder),
                    modifier = Modifier.testTag("publish_pick_photo_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasDraftPhoto) "Change Portrait" else stringResource(R.string.publish_pick_photo),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                }
                if (hasDraftPhoto) {
                    TextButton(onClick = onClearPhoto) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = palette.bloodText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", color = palette.bloodText)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvatarPreset.entries.forEach { preset ->
                    val selected = formState.avatarPreset == preset
                    GlassPill(
                        text = preset.label,
                        containerColor = if (selected) palette.accent.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.07f),
                        borderColor = if (selected) palette.accent else palette.glassBorder,
                        modifier = Modifier.clickable {
                            onUpdateForm { it.copy(avatarPreset = preset) }
                        }
                    )
                }
            }

            GlassInputField(
                value = formState.name,
                onValueChange = { v -> onUpdateForm { it.copy(name = v.take(PortfolioLimits.MAX_NAME)) } },
                label = "Full Name *",
                placeholder = "e.g., Rohan Sen",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassInputField(
                    value = formState.nickname,
                    onValueChange = { v -> onUpdateForm { it.copy(nickname = v.take(PortfolioLimits.MAX_NICKNAME)) } },
                    label = "Nickname",
                    placeholder = "e.g., Spark",
                    modifier = Modifier.weight(1f)
                )
                GlassInputField(
                    value = formState.city,
                    onValueChange = { v -> onUpdateForm { it.copy(city = v.take(PortfolioLimits.MAX_CITY)) } },
                    label = "City",
                    placeholder = "e.g., Kolkata",
                    modifier = Modifier.weight(1f)
                )
            }

            GlassInputField(
                value = formState.school,
                onValueChange = { v -> onUpdateForm { it.copy(school = v.take(PortfolioLimits.MAX_SCHOOL)) } },
                label = "School / College / Academy *",
                placeholder = "e.g., Horizon Science Academy",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_school_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        val selected = formState.bloodGroup == option
                        GlassPill(
                            text = option,
                            containerColor = if (selected) palette.bloodBg else Color.White.copy(alpha = 0.07f),
                            borderColor = if (selected) palette.bloodBorder else palette.glassBorder,
                            textColor = if (selected) palette.bloodText else palette.textMuted,
                            modifier = Modifier.clickable {
                                onUpdateForm { it.copy(bloodGroup = option) }
                            }
                        )
                    }
                }
            }

            GlassInputField(
                value = formState.hobby,
                onValueChange = { v -> onUpdateForm { it.copy(hobby = v.take(PortfolioLimits.MAX_HOBBY)) } },
                label = "Favorite Hobby / Passion",
                placeholder = "e.g., Guitar, Cricket, Photography",
                modifier = Modifier.fillMaxWidth()
            )

            GlassInputField(
                value = formState.aimInLife,
                onValueChange = { v -> onUpdateForm { it.copy(aimInLife = v.take(PortfolioLimits.MAX_AIM)) } },
                label = "Aim in Life *",
                placeholder = "What dream or goal inspires them?",
                singleLine = false,
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_aim_input")
            )

            GlassInputField(
                value = formState.note,
                onValueChange = { v -> onUpdateForm { it.copy(note = v.take(PortfolioLimits.MAX_NOTE)) } },
                label = "Special Friendship Memory / Note",
                placeholder = "A favorite hangout, joke, or wish…",
                singleLine = false,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Golden Trophy Honor Title",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldSoftText
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrophyTitleOptions.forEach { title ->
                        val selected = formState.trophyTitle == title
                        GlassPill(
                            text = title,
                            containerColor = if (selected) palette.gold.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.07f),
                            borderColor = if (selected) palette.gold else palette.glassBorder,
                            textColor = if (selected) palette.goldSoftText else palette.textMuted,
                            modifier = Modifier.clickable {
                                onUpdateForm { it.copy(trophyTitle = title) }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, palette.glassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Highlight in Good Friends Circle",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Adds a golden star badge & displays on Founder page",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
                Switch(
                    checked = formState.closeFriend,
                    onCheckedChange = { checked -> onUpdateForm { it.copy(closeFriend = checked) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF1F1300),
                        checkedTrackColor = palette.gold
                    )
                )
            }

            if (formState.validationError != null) {
                Text(
                    text = formState.validationError,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.bloodText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.bloodBg)
                        .border(1.dp, palette.bloodBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !formState.isSubmitting,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(palette.accent, palette.accent2)))
                    .testTag("publish_submit_button")
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Minting Trophy…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.publish_submit_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun TrophyUnlockedModal(
    friend: FriendEntry,
    onDismiss: () -> Unit,
    onViewTrophyHall: () -> Unit
) {
    val palette = LocalGlassPalette.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 420.dp),
            backgroundColor = palette.bg1.copy(alpha = 0.96f),
            borderColor = palette.gold.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoldenTrophyEmblem(size = 86.dp)

                GlassPill(
                    text = friend.trophyId,
                    containerColor = palette.gold.copy(alpha = 0.22f),
                    borderColor = palette.gold,
                    textColor = palette.goldSoftText
                )

                Text(
                    text = "Golden Trophy Unlocked!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${friend.name} is now featured in your Friends Circle as \"${friend.trophyTitle}\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onViewTrophyHall,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.gold,
                        contentColor = Color(0xFF1F1300)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlocked_view_trophy_hall_button")
                ) {
                    Text("Open Trophy Hall", fontWeight = FontWeight.ExtraBold)
                }

                TextButton(onClick = onDismiss) {
                    Text("Continue", color = palette.textMuted)
                }
            }
        }
    }
}

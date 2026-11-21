package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FounderProfile
import com.example.data.FriendEntry
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GoldenTrophyEmblem
import com.example.ui.theme.LocalGlassPalette

@Composable
fun TrophyHallScreen(
    founder: FounderProfile,
    friends: List<FriendEntry>,
    onCelebrateFounder: () -> Unit,
    onCheerFriend: (FriendEntry) -> Unit,
    onTrophyClick: (FriendEntry) -> Unit,
    onNavigateToPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    val totalCheers = friends.sumOf { it.cheersCount }
    val closeCount = friends.count { it.closeFriend }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 235.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1080.dp)
        ) {
            // Trophy Hall Hero Art Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = palette.gold.copy(alpha = 0.50f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_trophy_banner_1795225041038),
                            contentDescription = stringResource(R.string.cd_trophy_banner),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            palette.bg1.copy(alpha = 0.25f),
                                            palette.bg1.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.trophy_hall_title),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color(0xFFFEF3C7)
                                )
                                Text(
                                    text = stringResource(R.string.trophy_hall_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassPill(
                                    text = "${friends.size + 1} Trophies",
                                    containerColor = palette.gold.copy(alpha = 0.24f),
                                    borderColor = palette.gold.copy(alpha = 0.60f),
                                    textColor = palette.goldSoftText
                                )
                                GlassPill(
                                    text = "$closeCount Good Friends",
                                    containerColor = palette.accent.copy(alpha = 0.22f),
                                    borderColor = palette.accent.copy(alpha = 0.55f)
                                )
                                GlassPill(
                                    text = "$totalCheers Cheers",
                                    containerColor = palette.accent2.copy(alpha = 0.22f),
                                    borderColor = palette.accent2.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }
            }

            // Founder's Founding Golden Trophy (TRP-000)
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trophy_card_founder"),
                    cornerRadius = 22.dp,
                    borderColor = palette.gold.copy(alpha = 0.65f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoldenTrophyEmblem(size = 70.dp)

                        GlassPill(
                            text = "TRP-000 • FOUNDER",
                            containerColor = palette.gold.copy(alpha = 0.20f),
                            borderColor = palette.gold.copy(alpha = 0.60f),
                            textColor = palette.goldSoftText,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = palette.gold,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        )

                        Text(
                            text = founder.name.ifBlank { "Arpan" },
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Founding Architect & Trophy Host",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.accent,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = founder.motto.ifBlank { "Building timeless friendship memories." },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Button(
                            onClick = onCelebrateFounder,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.gold,
                                contentColor = Color(0xFF1F1300)
                            ),
                            modifier = Modifier.testTag("trophy_celebrate_founder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.trophy_celebrate), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Published Friend Trophies
            items(friends, key = { it.id }) { friend ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrophyClick(friend) }
                        .testTag("trophy_card_${friend.id}"),
                    cornerRadius = 22.dp,
                    borderColor = if (friend.closeFriend) palette.gold.copy(alpha = 0.55f) else palette.glassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoldenTrophyEmblem(size = 66.dp)

                        GlassPill(
                            text = friend.trophyId,
                            containerColor = palette.gold.copy(alpha = 0.16f),
                            borderColor = palette.gold.copy(alpha = 0.50f),
                            textColor = palette.goldSoftText
                        )

                        Text(
                            text = friend.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = friend.trophyTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.accent,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = friend.aimInLife,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Button(
                            onClick = { onCheerFriend(friend) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.14f),
                                contentColor = palette.goldSoftText
                            ),
                            modifier = Modifier
                                .border(1.dp, palette.gold.copy(alpha = 0.45f), CircleShape)
                                .testTag("trophy_cheer_${friend.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = palette.gold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cheer (${friend.cheersCount})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Mint Next Trophy Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPublish)
                        .testTag("trophy_mint_new_card"),
                    cornerRadius = 22.dp,
                    borderColor = palette.accent.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(palette.accent, palette.accent2))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "Mint Another Trophy",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Publish a friend card to generate their unique Golden Trophy Key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

package com.example.data

import androidx.annotation.Keep
import com.example.ui.theme.GlassThemeId
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AvatarPreset(val id: String, val label: String) {
    STARLIGHT("starlight", "Starlight Orb"),
    AURORA("aurora", "Aurora Prism"),
    ROSEWOOD("rosewood", "Rose Glow"),
    OCEAN("ocean", "Ocean Wave"),
    GOLD("gold", "Golden Crown");

    companion object {
        fun fromId(raw: String?): AvatarPreset =
            entries.firstOrNull { it.id == raw } ?: STARLIGHT
    }
}

enum class FriendSortOption(val label: String) {
    NEWEST("Newest"),
    NAME_ASC("Name A-Z"),
    TROPHY_ID("Trophy ID"),
    MOST_CHEERS("Most Cheers")
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class FounderProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "Arpan",
    val school: String = "Horizon Science Academy",
    val bloodGroup: String = "O+",
    val aimInLife: String = "To craft meaningful digital experiences and preserve unforgettable friendships forever.",
    val city: String = "Kolkata, India",
    val hobby: String = "Creative Coding, Music & Stargazing",
    val motto: String = "True friends turn ordinary days into golden trophies.",
    val goodFriends: List<String> = listOf("Inner Circle Open"),
    val themeId: String = GlassThemeId.LAVENDER.id,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FriendEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val nickname: String = "",
    val school: String = "",
    val bloodGroup: String = "O+",
    val city: String = "",
    val hobby: String = "",
    val aimInLife: String = "",
    val note: String = "",
    val avatarPreset: String = AvatarPreset.STARLIGHT.id,
    val trophyId: String = "TRP-1001",
    val trophyTitle: String = "Golden Circle Honoree",
    val closeFriend: Boolean = false,
    val cheersCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

val BloodGroupOptions = listOf(
    "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-", "Not Shared"
)

val TrophyTitleOptions = listOf(
    "Golden Circle Honoree",
    "Starlight Ally",
    "Bestie Legend",
    "Dreamer & Doer",
    "Midnight Confidant",
    "Vibe Architect"
)

object PortfolioLimits {
    const val MAX_NAME = 60
    const val MAX_NICKNAME = 40
    const val MAX_SCHOOL = 100
    const val MAX_BLOOD_GROUP = 20
    const val MAX_CITY = 80
    const val MAX_HOBBY = 80
    const val MAX_AIM = 300
    const val MAX_NOTE = 300
    const val MAX_MOTTO = 160
    const val MAX_TROPHY_TITLE = 60
    const val MAX_GOOD_FRIENDS = 20
    const val MAX_GOOD_FRIEND_NAME = 40
}

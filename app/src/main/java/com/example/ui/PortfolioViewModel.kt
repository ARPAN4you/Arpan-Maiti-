package com.example.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AvatarPreset
import com.example.data.FounderProfile
import com.example.data.FriendEntry
import com.example.data.FriendSortOption
import com.example.data.LocalPhotoStore
import com.example.data.PortfolioLimits
import com.example.data.PortfolioRepository
import com.example.data.TrophyTitleOptions
import com.example.data.UiState
import com.example.ui.theme.GlassThemeId
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PortfolioTab(val index: Int, val label: String) {
    FOUNDER(0, "Founder"),
    FRIENDS(1, "Friends"),
    PUBLISH(2, "Join & Publish"),
    TROPHIES(3, "Trophy Hall");

    companion object {
        fun fromIndex(index: Int): PortfolioTab =
            entries.firstOrNull { it.index == index } ?: FOUNDER
    }
}

data class PublishFormState(
    val name: String = "",
    val nickname: String = "",
    val school: String = "",
    val bloodGroup: String = "O+",
    val city: String = "",
    val hobby: String = "",
    val aimInLife: String = "",
    val note: String = "",
    val avatarPreset: AvatarPreset = AvatarPreset.STARLIGHT,
    val trophyTitle: String = TrophyTitleOptions.first(),
    val closeFriend: Boolean = false,
    val isSubmitting: Boolean = false,
    val validationError: String? = null
)

class PortfolioViewModel(
    private val repository: PortfolioRepository,
    private val photoStore: LocalPhotoStore,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedTheme = MutableStateFlow(GlassThemeId.LAVENDER)
    val selectedTheme: StateFlow<GlassThemeId> = _selectedTheme.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedBloodFilter = MutableStateFlow("All")
    val selectedBloodFilter: StateFlow<String> = _selectedBloodFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(FriendSortOption.NEWEST)
    val sortOption: StateFlow<FriendSortOption> = _sortOption.asStateFlow()

    private val _publishForm = MutableStateFlow(PublishFormState())
    val publishForm: StateFlow<PublishFormState> = _publishForm.asStateFlow()

    private val _confettiBurstId = MutableStateFlow(0L)
    val confettiBurstId: StateFlow<Long> = _confettiBurstId.asStateFlow()

    private val _newlyUnlockedFriend = MutableStateFlow<FriendEntry?>(null)
    val newlyUnlockedFriend: StateFlow<FriendEntry?> = _newlyUnlockedFriend.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    val localPhotoPaths: StateFlow<Map<String, String>> = photoStore.photoPathsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyMap()
        )

    val founderState: StateFlow<UiState<FounderProfile>> = repository
        .observeFounderProfile(currentUserId)
        .map<FounderProfile?, UiState<FounderProfile>> { profile ->
            val resolved = profile ?: FounderProfile(userId = currentUserId, themeId = _selectedTheme.value.id)
            val remoteTheme = GlassThemeId.fromId(resolved.themeId)
            if (profile != null && remoteTheme != _selectedTheme.value) {
                _selectedTheme.value = remoteTheme
            }
            UiState.Success(resolved)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing founder profile", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load founder profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val friendsState: StateFlow<UiState<List<FriendEntry>>> = repository
        .observeFriends(currentUserId)
        .map<List<FriendEntry>, UiState<List<FriendEntry>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing friends", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load friends"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val filteredFriends: StateFlow<List<FriendEntry>> = combine(
        friendsState,
        _searchQuery,
        _selectedBloodFilter,
        _sortOption
    ) { state, query, bloodFilter, sort ->
        val raw = (state as? UiState.Success)?.data ?: emptyList()
        val trimmedQuery = query.trim().lowercase()
        val filtered = raw.filter { friend ->
            val matchesBlood = bloodFilter == "All" ||
                (bloodFilter == "Close" && friend.closeFriend) ||
                friend.bloodGroup.equals(bloodFilter, ignoreCase = true)
            val matchesQuery = trimmedQuery.isEmpty() ||
                friend.name.lowercase().contains(trimmedQuery) ||
                friend.nickname.lowercase().contains(trimmedQuery) ||
                friend.school.lowercase().contains(trimmedQuery) ||
                friend.aimInLife.lowercase().contains(trimmedQuery) ||
                friend.hobby.lowercase().contains(trimmedQuery) ||
                friend.trophyId.lowercase().contains(trimmedQuery)
            matchesBlood && matchesQuery
        }
        when (sort) {
            FriendSortOption.NEWEST -> filtered.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            FriendSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            FriendSortOption.TROPHY_ID -> filtered.sortedBy { it.trophyId }
            FriendSortOption.MOST_CHEERS -> filtered.sortedByDescending { it.cheersCount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            photoStore.selectedThemeFlow.collect { localTheme ->
                _selectedTheme.value = localTheme
            }
        }
        viewModelScope.launch {
            repository.ensureFounderProfileExists(_selectedTheme.value)
        }
    }

    fun selectTheme(themeId: GlassThemeId) {
        _selectedTheme.value = themeId
        viewModelScope.launch {
            photoStore.saveLocalTheme(themeId)
            repository.updateTheme(themeId).onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Saved theme locally")
            }
        }
    }

    fun triggerConfetti() {
        _confettiBurstId.value = System.currentTimeMillis()
    }

    fun dismissUnlockedModal() {
        _newlyUnlockedFriend.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateBloodFilter(filter: String) {
        _selectedBloodFilter.value = filter
    }

    fun updateSortOption(option: FriendSortOption) {
        _sortOption.value = option
    }

    fun updatePublishForm(transform: (PublishFormState) -> PublishFormState) {
        _publishForm.update { state ->
            transform(state).copy(validationError = null)
        }
    }

    fun saveFounderPhoto(uri: Uri) {
        viewModelScope.launch {
            photoStore.savePickedPhoto(LocalPhotoStore.FOUNDER_SLOT, uri)
                .onSuccess {
                    _snackbarMessages.tryEmit("Founder portrait updated")
                }
                .onFailure {
                    _snackbarMessages.tryEmit("Could not save selected photo")
                }
        }
    }

    fun saveDraftFriendPhoto(uri: Uri) {
        viewModelScope.launch {
            photoStore.savePickedPhoto(LocalPhotoStore.DRAFT_FRIEND_SLOT, uri)
                .onSuccess {
                    _snackbarMessages.tryEmit("Preview photo attached")
                }
                .onFailure {
                    _snackbarMessages.tryEmit("Could not load selected photo")
                }
        }
    }

    fun clearDraftFriendPhoto() {
        viewModelScope.launch {
            photoStore.clearSlotPhoto(LocalPhotoStore.DRAFT_FRIEND_SLOT)
        }
    }

    fun saveFriendPhoto(friendId: String, uri: Uri) {
        viewModelScope.launch {
            photoStore.savePickedPhoto(LocalPhotoStore.friendSlot(friendId), uri)
                .onSuccess {
                    _snackbarMessages.tryEmit("Friend portrait updated")
                }
                .onFailure {
                    _snackbarMessages.tryEmit("Could not update portrait")
                }
        }
    }

    fun updateFounderProfile(
        name: String,
        school: String,
        bloodGroup: String,
        aimInLife: String,
        city: String,
        hobby: String,
        motto: String,
        goodFriendsRaw: String,
        onComplete: () -> Unit
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            _snackbarMessages.tryEmit("Founder name cannot be empty")
            return
        }
        val goodFriendsList = goodFriendsRaw
            .split(",")
            .map { it.trim().take(PortfolioLimits.MAX_GOOD_FRIEND_NAME) }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(PortfolioLimits.MAX_GOOD_FRIENDS)

        viewModelScope.launch {
            repository.updateFounderDetails(
                name = cleanName,
                school = school,
                bloodGroup = bloodGroup,
                aimInLife = aimInLife,
                city = city,
                hobby = hobby,
                motto = motto,
                goodFriends = goodFriendsList,
                fallbackTheme = _selectedTheme.value
            ).onSuccess {
                _snackbarMessages.tryEmit("Founder profile synced to cloud")
                onComplete()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Failed to update founder profile")
            }
        }
    }

    fun submitNewFriend(onPublished: () -> Unit) {
        val form = _publishForm.value
        val name = form.name.trim()
        val school = form.school.trim()
        val aim = form.aimInLife.trim()

        when {
            name.length < 2 -> {
                _publishForm.update { it.copy(validationError = "Please enter a name (at least 2 characters).") }
                return
            }
            school.length < 2 -> {
                _publishForm.update { it.copy(validationError = "Please enter a school, college, or academy.") }
                return
            }
            aim.length < 5 -> {
                _publishForm.update { it.copy(validationError = "Share an Aim in Life (at least 5 characters).") }
                return
            }
        }

        _publishForm.update { it.copy(isSubmitting = true, validationError = null) }
        viewModelScope.launch {
            repository.publishFriend(
                name = name,
                nickname = form.nickname,
                school = school,
                bloodGroup = form.bloodGroup,
                city = form.city,
                hobby = form.hobby,
                aimInLife = aim,
                note = form.note,
                avatarPreset = form.avatarPreset,
                trophyTitle = form.trophyTitle,
                closeFriend = form.closeFriend
            ).onSuccess { created ->
                photoStore.copySlotPhoto(
                    fromSlotKey = LocalPhotoStore.DRAFT_FRIEND_SLOT,
                    toSlotKey = LocalPhotoStore.friendSlot(created.id)
                )
                _publishForm.value = PublishFormState()
                _newlyUnlockedFriend.value = created
                triggerConfetti()
                _snackbarMessages.tryEmit("Published ${created.name} • Unlocked ${created.trophyId}!")
                onPublished()
            }.onFailure { err ->
                _publishForm.update {
                    it.copy(
                        isSubmitting = false,
                        validationError = err.localizedMessage ?: "Could not publish friend card."
                    )
                }
            }
        }
    }

    fun editFriend(
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
    ) {
        viewModelScope.launch {
            repository.updateFriend(
                friendId = friendId,
                name = name,
                nickname = nickname,
                school = school,
                bloodGroup = bloodGroup,
                city = city,
                hobby = hobby,
                aimInLife = aimInLife,
                note = note,
                avatarPreset = avatarPreset,
                trophyTitle = trophyTitle,
                closeFriend = closeFriend
            ).onSuccess {
                _snackbarMessages.tryEmit("Updated friend card")
                onComplete()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Failed to update friend")
            }
        }
    }

    fun toggleCloseFriend(friend: FriendEntry) {
        viewModelScope.launch {
            val next = !friend.closeFriend
            repository.toggleCloseFriend(friend.id, next)
                .onSuccess {
                    val label = if (next) "Added to Good Friends Circle" else "Removed from Good Friends Circle"
                    _snackbarMessages.tryEmit("${friend.name}: $label")
                }
                .onFailure { err ->
                    _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not update status")
                }
        }
    }

    fun cheerTrophy(friend: FriendEntry) {
        triggerConfetti()
        viewModelScope.launch {
            repository.cheerFriendTrophy(friend.id)
                .onSuccess {
                    _snackbarMessages.tryEmit("Celebrated ${friend.name}'s ${friend.trophyId}!")
                }
                .onFailure { err ->
                    _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not record cheer")
                }
        }
    }

    fun deleteFriend(friend: FriendEntry, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteFriend(friend.id)
                .onSuccess {
                    photoStore.clearSlotPhoto(LocalPhotoStore.friendSlot(friend.id))
                    _snackbarMessages.tryEmit("Removed ${friend.name} from portfolio")
                    onComplete()
                }
                .onFailure { err ->
                    _snackbarMessages.tryEmit(err.localizedMessage ?: "Failed to delete friend")
                }
        }
    }

    private companion object {
        const val TAG = "PortfolioViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}

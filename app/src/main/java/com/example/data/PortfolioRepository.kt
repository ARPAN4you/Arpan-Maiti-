package com.example.data

import android.content.Context
import com.example.R
import com.example.ui.theme.GlassThemeId
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class PortfolioRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Please sign in with Google before accessing Friends Portfolio.")
    }

    private fun founderDocRef(uid: String) =
        db.collection("users").document(uid).collection("founder_profile").document(FOUNDER_DOC_ID)

    private fun friendsCollection(uid: String) =
        db.collection("users").document(uid).collection("friends")

    fun observeFounderProfile(userId: String): Flow<FounderProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/founder_profile/$FOUNDER_DOC_ID"
        emitAll(
            founderDocRef(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            FounderProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeFriends(userId: String): Flow<List<FriendEntry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/friends"
        emitAll(
            friendsCollection(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FriendEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureFounderProfileExists(defaultTheme: GlassThemeId): Result<Unit> {
        var path = "users/unauthenticated/founder_profile/$FOUNDER_DOC_ID"
        return try {
            val uid = requireUserId()
            val docRef = founderDocRef(uid)
            path = docRef.path
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val defaultProfile = FounderProfile(userId = uid, themeId = defaultTheme.id)
                val payload = mapOf(
                    "userId" to uid,
                    "name" to defaultProfile.name.take(PortfolioLimits.MAX_NAME),
                    "school" to defaultProfile.school.take(PortfolioLimits.MAX_SCHOOL),
                    "bloodGroup" to defaultProfile.bloodGroup.take(PortfolioLimits.MAX_BLOOD_GROUP),
                    "aimInLife" to defaultProfile.aimInLife.take(PortfolioLimits.MAX_AIM),
                    "city" to defaultProfile.city.take(PortfolioLimits.MAX_CITY),
                    "hobby" to defaultProfile.hobby.take(PortfolioLimits.MAX_HOBBY),
                    "motto" to defaultProfile.motto.take(PortfolioLimits.MAX_MOTTO),
                    "goodFriends" to defaultProfile.goodFriends
                        .take(PortfolioLimits.MAX_GOOD_FRIENDS)
                        .map { it.take(PortfolioLimits.MAX_GOOD_FRIEND_NAME) },
                    "themeId" to defaultTheme.id,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateFounderDetails(
        name: String,
        school: String,
        bloodGroup: String,
        aimInLife: String,
        city: String,
        hobby: String,
        motto: String,
        goodFriends: List<String>,
        fallbackTheme: GlassThemeId
    ): Result<Unit> {
        var path = "users/unauthenticated/founder_profile/$FOUNDER_DOC_ID"
        return try {
            val uid = requireUserId()
            val cleanName = name.trim().take(PortfolioLimits.MAX_NAME)
            require(cleanName.isNotEmpty()) { "Founder name is required." }

            val cleanSchool = school.trim().take(PortfolioLimits.MAX_SCHOOL)
            val cleanBlood = bloodGroup.trim().take(PortfolioLimits.MAX_BLOOD_GROUP)
            val cleanAim = aimInLife.trim().take(PortfolioLimits.MAX_AIM)
            val cleanCity = city.trim().take(PortfolioLimits.MAX_CITY)
            val cleanHobby = hobby.trim().take(PortfolioLimits.MAX_HOBBY)
            val cleanMotto = motto.trim().take(PortfolioLimits.MAX_MOTTO)
            val cleanFriends = goodFriends
                .map { it.trim().take(PortfolioLimits.MAX_GOOD_FRIEND_NAME) }
                .filter { it.isNotEmpty() }
                .distinct()
                .take(PortfolioLimits.MAX_GOOD_FRIENDS)

            val docRef = founderDocRef(uid)
            path = docRef.path
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "name" to cleanName,
                        "school" to cleanSchool,
                        "bloodGroup" to cleanBlood,
                        "aimInLife" to cleanAim,
                        "city" to cleanCity,
                        "hobby" to cleanHobby,
                        "motto" to cleanMotto,
                        "goodFriends" to cleanFriends,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "name" to cleanName,
                        "school" to cleanSchool,
                        "bloodGroup" to cleanBlood,
                        "aimInLife" to cleanAim,
                        "city" to cleanCity,
                        "hobby" to cleanHobby,
                        "motto" to cleanMotto,
                        "goodFriends" to cleanFriends,
                        "themeId" to fallbackTheme.id,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTheme(themeId: GlassThemeId): Result<Unit> {
        var path = "users/unauthenticated/founder_profile/$FOUNDER_DOC_ID"
        return try {
            val uid = requireUserId()
            val docRef = founderDocRef(uid)
            path = docRef.path
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "themeId" to themeId.id,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ensureFounderProfileExists(themeId).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun publishFriend(
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
    ): Result<FriendEntry> {
        var path = "users/unauthenticated/friends"
        return try {
            val uid = requireUserId()
            val cleanName = name.trim().take(PortfolioLimits.MAX_NAME)
            val cleanSchool = school.trim().take(PortfolioLimits.MAX_SCHOOL)
            val cleanAim = aimInLife.trim().take(PortfolioLimits.MAX_AIM)
            val cleanBlood = bloodGroup.trim().ifEmpty { "Not Shared" }.take(PortfolioLimits.MAX_BLOOD_GROUP)
            val cleanTitle = trophyTitle.trim().ifEmpty { "Golden Circle Honoree" }.take(PortfolioLimits.MAX_TROPHY_TITLE)

            require(cleanName.isNotEmpty()) { "Friend name is required." }
            require(cleanSchool.isNotEmpty()) { "School or college is required." }
            require(cleanAim.isNotEmpty()) { "Aim in life is required." }

            val docRef = friendsCollection(uid).document()
            path = docRef.path
            val trophyNumber = Random.nextInt(1001, 9999)
            val trophyId = "TRP-$trophyNumber"

            val payload = mapOf(
                "userId" to uid,
                "name" to cleanName,
                "nickname" to nickname.trim().take(PortfolioLimits.MAX_NICKNAME),
                "school" to cleanSchool,
                "bloodGroup" to cleanBlood,
                "city" to city.trim().take(PortfolioLimits.MAX_CITY),
                "hobby" to hobby.trim().take(PortfolioLimits.MAX_HOBBY),
                "aimInLife" to cleanAim,
                "note" to note.trim().take(PortfolioLimits.MAX_NOTE),
                "avatarPreset" to avatarPreset.id,
                "trophyId" to trophyId,
                "trophyTitle" to cleanTitle,
                "closeFriend" to closeFriend,
                "cheersCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()

            Result.success(
                FriendEntry(
                    id = docRef.id,
                    userId = uid,
                    name = cleanName,
                    nickname = nickname.trim().take(PortfolioLimits.MAX_NICKNAME),
                    school = cleanSchool,
                    bloodGroup = cleanBlood,
                    city = city.trim().take(PortfolioLimits.MAX_CITY),
                    hobby = hobby.trim().take(PortfolioLimits.MAX_HOBBY),
                    aimInLife = cleanAim,
                    note = note.trim().take(PortfolioLimits.MAX_NOTE),
                    avatarPreset = avatarPreset.id,
                    trophyId = trophyId,
                    trophyTitle = cleanTitle,
                    closeFriend = closeFriend,
                    cheersCount = 0L
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateFriend(
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
        closeFriend: Boolean
    ): Result<Unit> {
        var path = "users/unauthenticated/friends/$friendId"
        return try {
            val uid = requireUserId()
            require(friendId.isNotBlank()) { "Friend ID is missing." }
            val cleanName = name.trim().take(PortfolioLimits.MAX_NAME)
            val cleanSchool = school.trim().take(PortfolioLimits.MAX_SCHOOL)
            val cleanAim = aimInLife.trim().take(PortfolioLimits.MAX_AIM)
            require(cleanName.isNotEmpty()) { "Friend name is required." }
            require(cleanSchool.isNotEmpty()) { "School or college is required." }
            require(cleanAim.isNotEmpty()) { "Aim in life is required." }

            val docRef = friendsCollection(uid).document(friendId)
            path = docRef.path
            docRef.update(
                mapOf(
                    "name" to cleanName,
                    "nickname" to nickname.trim().take(PortfolioLimits.MAX_NICKNAME),
                    "school" to cleanSchool,
                    "bloodGroup" to bloodGroup.trim().ifEmpty { "Not Shared" }.take(PortfolioLimits.MAX_BLOOD_GROUP),
                    "city" to city.trim().take(PortfolioLimits.MAX_CITY),
                    "hobby" to hobby.trim().take(PortfolioLimits.MAX_HOBBY),
                    "aimInLife" to cleanAim,
                    "note" to note.trim().take(PortfolioLimits.MAX_NOTE),
                    "avatarPreset" to avatarPreset.id,
                    "trophyTitle" to trophyTitle.trim().ifEmpty { "Golden Circle Honoree" }.take(PortfolioLimits.MAX_TROPHY_TITLE),
                    "closeFriend" to closeFriend,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleCloseFriend(friendId: String, closeFriend: Boolean): Result<Unit> {
        var path = "users/unauthenticated/friends/$friendId"
        return try {
            val uid = requireUserId()
            require(friendId.isNotBlank()) { "Friend ID is missing." }
            val docRef = friendsCollection(uid).document(friendId)
            path = docRef.path
            docRef.update(
                mapOf(
                    "closeFriend" to closeFriend,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun cheerFriendTrophy(friendId: String): Result<Unit> {
        var path = "users/unauthenticated/friends/$friendId"
        return try {
            val uid = requireUserId()
            require(friendId.isNotBlank()) { "Friend ID is missing." }
            val docRef = friendsCollection(uid).document(friendId)
            path = docRef.path
            docRef.update(
                mapOf(
                    "cheersCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteFriend(friendId: String): Result<Unit> {
        var path = "users/unauthenticated/friends/$friendId"
        return try {
            val uid = requireUserId()
            require(friendId.isNotBlank()) { "Friend ID is missing." }
            val docRef = friendsCollection(uid).document(friendId)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    companion object {
        const val FOUNDER_DOC_ID = "main"
    }
}

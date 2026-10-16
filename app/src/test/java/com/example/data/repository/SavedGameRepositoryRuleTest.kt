package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FirestoreWishlistItem
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import java.util.UUID
import kotlin.test.assertFailsWith

class SavedGameRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveWishlistItem_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreWishlistRepository(firestore)

        val uniqueGameId = "game_${UUID.randomUUID().toString().take(8)}"
        val item = FirestoreWishlistItem(
            gameId = uniqueGameId,
            userId = uid,
            title = "Elden Ring",
            currentPrice = 39.99,
            targetPrice = 29.99,
            priority = "HIGH"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWishlistItem(uid, item)
        }
        assertThat(result.isSuccess).isTrue()

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getWishlistItem(uid, uniqueGameId).getOrThrow()
        }
        assertThat(fetched).isNotNull()
        assertThat(fetched?.title).isEqualTo("Elden Ring")
    }

    @Test
    fun observeWishlist_authenticatedUser_returnsSavedItems() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreWishlistRepository(firestore)

        val uniqueGameId = "game_${UUID.randomUUID().toString().take(8)}"
        val item = FirestoreWishlistItem(
            gameId = uniqueGameId,
            userId = uid,
            title = "Cyberpunk 2077",
            currentPrice = 29.99,
            targetPrice = 19.99
        )

        repository.saveWishlistItem(uid, item)

        val items = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeWishlist(uid).first { list -> list.any { it.gameId == uniqueGameId } }
        }
        assertThat(items.map { it.gameId }).contains(uniqueGameId)
    }

    @Test
    fun getWishlistItem_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreWishlistRepository(firestore)
        val gameId = "game_${UUID.randomUUID().toString().take(8)}"
        aliceRepo.saveWishlistItem(
            aliceUid,
            FirestoreWishlistItem(
                gameId = gameId,
                userId = aliceUid,
                title = "Alice Game"
            )
        )

        // Switch user to Bob
        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreWishlistRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getWishlistItem(aliceUid, gameId)
        }
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(FirebaseFirestoreException::class.java)
        val firestoreException = exception as FirebaseFirestoreException
        assertThat(firestoreException.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    @Test
    fun observeWishlist_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FirestoreWishlistRepository(firestore)

        val exception = assertFailsWith<FirebaseFirestoreException> {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeWishlist("unauth_user").first()
            }
        }
        assertThat(exception.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_gamer@test.com"
        const val BOB_EMAIL = "bob_gamer@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}

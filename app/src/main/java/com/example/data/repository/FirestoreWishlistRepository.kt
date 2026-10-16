package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.FirestoreWishlistItem
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreWishlistRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun requireUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeWishlist(userId: String): Flow<List<FirestoreWishlistItem>> {
        val path = "users/$userId/wishlist"
        return db.collection("users").document(userId).collection("wishlist")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreWishlistItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun saveWishlistItem(userId: String, item: FirestoreWishlistItem): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("wishlist").document(item.gameId)
        return try {
            val payload = item.copy(
                userId = userId,
                updatedAt = null
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteWishlistItem(userId: String, gameId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("wishlist").document(gameId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getWishlistItem(userId: String, gameId: String): Result<FirestoreWishlistItem?> {
        val docRef = db.collection("users").document(userId).collection("wishlist").document(gameId)
        return try {
            val snapshot = docRef.get().await()
            val item = if (snapshot.exists()) {
                snapshot.toObject(FirestoreWishlistItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }
}

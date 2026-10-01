package com.codit.cryptoconverter.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.codit.cryptoconverter.data.local.toDomain
import com.codit.cryptoconverter.data.local.toEntity
import com.codit.cryptoconverter.db.AppDatabase
import com.codit.cryptoconverter.domain.model.FavoritePair
import com.codit.cryptoconverter.domain.repository.FavoritesRepository
import com.codit.cryptoconverter.model.FavouritePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val db: AppDatabase
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<FavoritePair>> =
        db.favPairDao().observeAllFavPairs().map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(from: String, to: String) {
        withContext(Dispatchers.IO) {
            try {
                db.favPairDao().addFavPair(FavouritePair(from, to))
            } catch (e: SQLiteConstraintException) {
                Log.d("FavoritesRepo", "already exists: $from->$to")
            } catch (e: Exception) {
                // Room wraps constraint failures; ignore duplicates
                if (e.message.orEmpty().contains("UNIQUE", ignoreCase = true)) {
                    Log.d("FavoritesRepo", "duplicate ignored: $from->$to")
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun removeFavorite(from: String, to: String) {
        withContext(Dispatchers.IO) {
            val dao = db.favPairDao()
            var deleted = dao.deleteFavPair(FavouritePair(from, to))
            if (deleted == 0) {
                deleted = dao.deleteFavPair(FavouritePair(to, from))
            }
            Log.d("FavoritesRepo", "removed $from->$to count=$deleted")
        }
    }

    override fun observeIsFavorite(from: String, to: String): Flow<Boolean> =
        db.favPairDao().observeIsPairExist(from, to).map { it.isNotEmpty() }
}

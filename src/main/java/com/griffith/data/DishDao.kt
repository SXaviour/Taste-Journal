package com.griffith.data

import androidx.room.*
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(d: Dish): Long
    @Delete suspend fun delete(d: Dish)

    @Query("SELECT * FROM dishes ORDER BY dateCooked DESC")
    fun all(): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE id = :id")
    fun observe(id: Long): Flow<Dish?>

    // Timeline
    @Query("SELECT * FROM dishes ORDER BY dateCooked DESC LIMIT :n")
    fun recent(n: Int): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE rating >= :min AND dateCooked <= :cutoff ORDER BY dateCooked ASC LIMIT :n")
    fun forgotten(min: Int, cutoff: Long, n: Int): Flow<List<Dish>>

    @Query("SELECT * FROM dishes ORDER BY rating DESC, dateCooked DESC LIMIT :n")
    fun top(n: Int): Flow<List<Dish>>
}
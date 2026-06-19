package com.tiantian.wardrobe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecommendationDao {
    @Query("SELECT * FROM daily_recommendation WHERE id = 1")
    fun getRecommendation(): Flow<DailyRecommendation?>

    @Query("SELECT * FROM daily_recommendation WHERE id = 1")
    suspend fun getRecommendationOnce(): DailyRecommendation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recommendation: DailyRecommendation)

    @Query("DELETE FROM daily_recommendation")
    suspend fun deleteAll()
}

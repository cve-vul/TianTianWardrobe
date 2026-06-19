package com.tiantian.wardrobe.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getItemById(id: Long): ClothingItem?

    @Query("SELECT COUNT(*) FROM clothing_items")
    fun getItemCount(): Flow<Int>

    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY createdAt DESC")
    fun getItemsByCategory(category: String): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE season = :season ORDER BY createdAt DESC")
    fun getItemsBySeason(season: String): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE style = :style ORDER BY createdAt DESC")
    fun getItemsByStyle(style: String): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE category IN (:categories) ORDER BY createdAt DESC")
    fun getItemsByCategories(categories: List<String>): Flow<List<ClothingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClothingItem): Long

    @Update
    suspend fun updateItem(item: ClothingItem)

    @Delete
    suspend fun deleteItem(item: ClothingItem)

    @Query("DELETE FROM clothing_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
}

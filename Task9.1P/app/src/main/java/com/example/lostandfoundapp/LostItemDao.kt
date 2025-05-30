package com.example.lostandfoundapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// this interface defines what we can do with our database
@Dao
interface LostItemDao {

    // insert a new lost/found item
    @Insert
    suspend fun insertItem(item: LostItem)

    // get all items from database
    @Query("SELECT * FROM lost_items")
    suspend fun getAllItems(): List<LostItem>

    // delete an item by ID
    @Query("DELETE FROM lost_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)
}

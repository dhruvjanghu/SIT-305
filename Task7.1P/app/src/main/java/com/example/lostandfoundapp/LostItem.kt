package com.example.lostandfoundapp

import androidx.room.Entity
import androidx.room.PrimaryKey

// this class represents a table in the database
@Entity(tableName = "lost_items")
data class LostItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // unique id, will be auto-incremented

    val postType: String,     // Lost or Found
    val name: String,
    val phone: String,
    val description: String,
    val date: String,
    val location: String
)

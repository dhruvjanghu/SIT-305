package com.example.lostandfoundapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// this tells Room to treat LostItem as a table and use version 1
@Database(entities = [LostItem::class], version = 1)
abstract class LostItemDatabase : RoomDatabase() {

    // this function gives access to DAO methods like insert, get, delete
    abstract fun lostItemDao(): LostItemDao

    companion object {
        @Volatile
        private var INSTANCE: LostItemDatabase? = null

        // this gives us a singleton instance of the database (only one in the app)
        fun getDatabase(context: Context): LostItemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LostItemDatabase::class.java,
                    "lost_item_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

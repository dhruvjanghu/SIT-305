package com.example.taskmanagerapp

import android.content.Context
import androidx.room.* // importing everything needed for Room
import androidx.room.TypeConverters

// this tells Room we're using the Task class as a table (entity)
// and also that we'll be using a custom type converter for dates
@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {

    // this function gives us access to all the DAO functions (like insert, delete, etc)
    abstract fun taskDao(): TaskDao

    companion object {
        // this keeps a single instance of the database active (so we don’t make multiple copies)
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // this function either returns the existing database or creates a new one if it doesn't exist
        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                // build the Room database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database" // this is the name of the database file
                ).build()

                // save it to INSTANCE so we don’t build again
                INSTANCE = instance
                instance
            }
        }
    }
}

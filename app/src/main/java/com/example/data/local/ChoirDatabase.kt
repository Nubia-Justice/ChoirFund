package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Campaign
import com.example.data.model.ChoirSettings
import com.example.data.model.Contribution
import com.example.data.model.Expense
import com.example.data.model.Member

@Database(
    entities = [
        Member::class,
        Contribution::class,
        Expense::class,
        Campaign::class,
        ChoirSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ChoirDatabase : RoomDatabase() {
    abstract fun choirDao(): ChoirDao

    companion object {
        @Volatile
        private var INSTANCE: ChoirDatabase? = null

        fun getDatabase(context: Context): ChoirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChoirDatabase::class.java,
                    "choir_funds_cameroon_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

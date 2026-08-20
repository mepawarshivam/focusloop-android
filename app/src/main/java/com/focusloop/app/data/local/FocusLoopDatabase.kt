package com.focusloop.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focusloop.app.data.local.dao.*
import com.focusloop.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GoalEntity::class,
        DistractionSessionEntity::class,
        FocusSessionEntity::class,
        DailyStatsEntity::class,
        LearningQuestionEntity::class,
        LearningAttemptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusLoopDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun distractionSessionDao(): DistractionSessionDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun learningQuestionDao(): LearningQuestionDao

    companion object {
        @Volatile private var INSTANCE: FocusLoopDatabase? = null

        fun getInstance(context: Context): FocusLoopDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FocusLoopDatabase::class.java,
                    "focusloop.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed question bank on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    SeedData.seedQuestions(database.learningQuestionDao())
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

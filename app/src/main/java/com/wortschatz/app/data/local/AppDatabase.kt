package com.wortschatz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VocabularyEntity::class, VocabularyFolderEntity::class, StudyDayEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun folderDao(): FolderDao
    abstract fun studyDayDao(): StudyDayDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Adds folders without touching the existing deck or learning progress.
         * Existing vocabulary starts unfiled; the user can assign it to a folder.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `folders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("ALTER TABLE `vocabulary` ADD COLUMN `folderId` INTEGER")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vocabulary_folderId` " +
                        "ON `vocabulary` (`folderId`)",
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wortschatz.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}

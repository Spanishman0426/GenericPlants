package com.example.plantasapi.managers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.plantasapi.models.Plant
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PlantsDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_PLANTS = "plants"
        private const val TAG = "DatabaseHelper"

        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_WATER_PERIOD = "water_period"
        private const val COLUMN_IMAGE_URI = "image_uri"
        private const val COLUMN_SUGGESTED_NAME = "api_suggested_name"
        private const val COLUMN_PROBABILITY = "probability"

        // Index names
        private const val INDEX_NAME = "idx_plant_name"
        private const val INDEX_SUGGESTED_NAME = "idx_plant_api_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_PLANTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_WATER_PERIOD INT NOT NULL,
                $COLUMN_IMAGE_URI TEXT,
                $COLUMN_SUGGESTED_NAME TEXT,
                $COLUMN_PROBABILITY REAL
            ) STRICT;
        """.trimIndent()
        
        try {
            db.execSQL(createTableQuery)
            createIndexes(db)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating table: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createIndexes(db)
        }
    }

    private fun createIndexes(db: SQLiteDatabase) {
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS $INDEX_NAME ON $TABLE_PLANTS ($COLUMN_NAME);")
            db.execSQL("CREATE INDEX IF NOT EXISTS $INDEX_SUGGESTED_NAME ON $TABLE_PLANTS ($COLUMN_SUGGESTED_NAME);")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating indexes: ${e.message}")
        }
    }

    /**
     * Executes VACUUM to reclaim unused space.
     * Runs on Dispatchers.IO to avoid blocking the main thread.
     */
    suspend fun performMaintenance() = withContext(Dispatchers.IO) {
        try {
            val db = writableDatabase
            val sizeBefore = getDatabaseSize()
            Log.d(TAG, "Database size before VACUUM: $sizeBefore bytes")

            db.execSQL("VACUUM")

            val sizeAfter = getDatabaseSize()
            Log.d(TAG, "Database size after VACUUM: $sizeAfter bytes")
            Log.d(TAG, "Space reclaimed: ${sizeBefore - sizeAfter} bytes")
        } catch (e: Exception) {
            Log.e(TAG, "Error during VACUUM: ${e.message}")
        }
    }

    /**
     * Gets the actual file size of the database on disk.
     */
    fun getDatabaseSize(): Long {
        val dbFile: File = context.getDatabasePath(DATABASE_NAME)
        return if (dbFile.exists()) dbFile.length() else 0L
    }

    fun deletePlant(id: Int): Int {
        val db = this.writableDatabase
        return try {
            db.delete(TABLE_PLANTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting plant: ${e.message}")
            0
        }
    }

    fun insertPlant(plant: Plant): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, plant.name)
            put(COLUMN_WATER_PERIOD, plant.waterPeriod)
            put(COLUMN_IMAGE_URI, plant.imageUri.toString())
            put(COLUMN_SUGGESTED_NAME, plant.apiSuggestedName)
            put(COLUMN_PROBABILITY, plant.probability)
        }

        return try {
            db.insertOrThrow(TABLE_PLANTS, null, values)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting plant: ${e.message}")
            -1
        }
    }
}

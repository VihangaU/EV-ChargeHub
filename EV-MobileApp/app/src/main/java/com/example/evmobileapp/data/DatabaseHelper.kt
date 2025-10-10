package com.example.evmobileapp.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.evmobileapp.model.EvOwner
import com.example.evmobileapp.model.UserSession

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "evcharging.db"
        private const val DATABASE_VERSION = 2  // Incremented to trigger upgrade

        // Table names and columns for EV Owners
        private const val TABLE_EV_OWNERS = "ev_owners"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_VEHICLE_MODEL = "vehicleModel"
        private const val COLUMN_VEHICLE_NUMBER = "vehicleNumber"
        private const val COLUMN_STATUS = "status"  // Active/Inactive

        // Table name and columns for Sessions
        private const val TABLE_SESSIONS = "sessions"
        private const val COLUMN_SESSIONS_EMAIL = "email"
        private const val COLUMN_SESSIONS_ROLE = "role"
        private const val COLUMN_SESSIONS_TOKEN = "token"
        private const val COLUMN_SESSIONS_USERID = "userId"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create table for EV Owners
        val createEvOwnersTableQuery = """
            CREATE TABLE $TABLE_EV_OWNERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_NIC TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_VEHICLE_MODEL TEXT,
                $COLUMN_VEHICLE_NUMBER TEXT,
                $COLUMN_STATUS TEXT
            )
        """
        db.execSQL(createEvOwnersTableQuery)

        // Create table for Sessions
        val createSessionsTableQuery = """
            CREATE TABLE $TABLE_SESSIONS (
                $COLUMN_SESSIONS_EMAIL TEXT PRIMARY KEY,
                $COLUMN_SESSIONS_ROLE TEXT NOT NULL,
                $COLUMN_SESSIONS_TOKEN TEXT NOT NULL,
                $COLUMN_SESSIONS_USERID TEXT
            )
        """
        db.execSQL(createSessionsTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add the new column if it doesn't exist, or drop and recreate for simplicity
            db.execSQL("ALTER TABLE $TABLE_SESSIONS ADD COLUMN $COLUMN_SESSIONS_USERID TEXT")
        }
        // For future upgrades, add more logic here
    }

    // Insert a new EV Owner into the database
    fun insertEvOwner(evOwner: EvOwner): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, evOwner.name)
            put(COLUMN_EMAIL, evOwner.email)
            put(COLUMN_NIC, evOwner.nic)
            put(COLUMN_PHONE, evOwner.phone)
            put(COLUMN_ADDRESS, evOwner.address)
            put(COLUMN_VEHICLE_MODEL, evOwner.vehicleModel)
            put(COLUMN_VEHICLE_NUMBER, evOwner.vehicleNumber)
            put(COLUMN_STATUS, evOwner.status)
        }
        return db.insert(TABLE_EV_OWNERS, null, contentValues)
    }

    // Get EV Owner profile by NIC
    @SuppressLint("Range")
    fun getEvOwnerByNic(nic: String): EvOwner? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_EV_OWNERS,
            null,
            "$COLUMN_NIC = ?",
            arrayOf(nic),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val evOwner = EvOwner(
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                nic = cursor.getString(cursor.getColumnIndex(COLUMN_NIC)),
                phone = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)),
                address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                vehicleModel = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_MODEL)),
                vehicleNumber = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_NUMBER)),
                status = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS))
            )
            cursor.close()
            evOwner
        } else {
            cursor.close()
            null
        }
    }

    // Update EV Owner profile
    fun updateEvOwner(evOwner: EvOwner): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, evOwner.name)
            put(COLUMN_EMAIL, evOwner.email)
            put(COLUMN_PHONE, evOwner.phone)
            put(COLUMN_ADDRESS, evOwner.address)
            put(COLUMN_VEHICLE_MODEL, evOwner.vehicleModel)
            put(COLUMN_VEHICLE_NUMBER, evOwner.vehicleNumber)
            put(COLUMN_STATUS, evOwner.status)
        }
        return db.update(
            TABLE_EV_OWNERS,
            contentValues,
            "$COLUMN_NIC = ?",
            arrayOf(evOwner.nic)
        )
    }

    // Delete EV Owner profile
    fun deleteEvOwner(nic: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_EV_OWNERS,
            "$COLUMN_NIC = ?",
            arrayOf(nic)
        )
    }

    // Save user session
    fun saveSession(email: String, role: String, token: String, userId: String? = null) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSIONS_EMAIL, email)
            put(COLUMN_SESSIONS_ROLE, role)
            put(COLUMN_SESSIONS_TOKEN, token)
            put(COLUMN_SESSIONS_USERID, userId)
        }
        db.insertWithOnConflict(TABLE_SESSIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Get current user session
    fun getSession(): UserSession? {
        val db = readableDatabase
        val cursor = db.query(TABLE_SESSIONS, null, null, null, null, null, null, "1")
        return try {
            if (cursor.moveToFirst()) {
                UserSession(
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSIONS_EMAIL)),
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSIONS_ROLE)),
                    token = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSIONS_TOKEN)),
                    userId = cursor.getString(cursor.getColumnIndex(COLUMN_SESSIONS_USERID)) // Use getString instead of getColumnIndexOrThrow for optional column
                )
            } else null
        } finally {
            cursor.close()
        }
    }

    // Clear user session
    fun clearSession() {
        val db = writableDatabase
        db.delete(TABLE_SESSIONS, null, null)
    }

    // Clear all data (sessions and ev_owners)
    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_SESSIONS, null, null)
        db.delete(TABLE_EV_OWNERS, null, null)
    }
}
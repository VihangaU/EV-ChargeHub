package com.example.evmobileapp.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.evmobileapp.model.EvOwner

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "evcharging.db"
        private const val DATABASE_VERSION = 1

        // Table names and columns
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
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create table for EV Owners
        val createTableQuery = """
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
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EV_OWNERS")
        onCreate(db)
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
}

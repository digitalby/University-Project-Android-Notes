package me.digitalby.lr3

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper



class DBOpenHelper constructor(context: Context): SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_NOTES = "notes"
        const val NOTE_ID = "_id"
        const val NOTE_TITLE = "noteTitle"
        const val NOTE_TEXT = "noteText"
        const val NOTE_CREATED = "noteCreated"
        const val NOTE_TAGSTRING = "noteTagString"

        const val TABLE_TAGS = "tags"
        const val TAG_ID = "_id"
        const val TAG_NAME = "name"

        const val TABLE_LINKS = "links"
        const val LINK_ID = "_id"
        const val LINK_NOTE_ID = "note_id"
        const val LINK_TAG_ID = "tag_id"

        val ALL_COLUMNS_NOTES = arrayOf(
            NOTE_ID,
            NOTE_TITLE,
            NOTE_TEXT,
            NOTE_CREATED,
            NOTE_TAGSTRING
        )
        val ALL_COLUMNS_TAGS = arrayOf(
            TAG_ID,
            TAG_NAME
        )
        val ALL_COLUMNS_LINKS = arrayOf(
            LINK_ID,
            LINK_NOTE_ID,
            LINK_TAG_ID
        )

        private const val NOTES_TABLE_CREATE = "CREATE TABLE $TABLE_NOTES (" +
                "$NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$NOTE_TITLE TEXT NOT NULL, " +
                "$NOTE_TEXT TEXT, " +
                "$NOTE_CREATED TEXT default CURRENT_TIMESTAMP, " +
                "$NOTE_TAGSTRING TEXT" +
                ")"
        private const val TAGS_TABLE_CREATE = "CREATE TABLE $TABLE_TAGS (" +
                "$TAG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$TAG_NAME TEXT UNIQUE NOT NULL" +
                ")"
        private const val LINKS_TABLE_CREATE = "CREATE TABLE $TABLE_LINKS (" +
                "$LINK_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$LINK_NOTE_ID INTEGER NOT NULL, " +
                "$LINK_TAG_ID INTEGER NOT NULL" +
                ")"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(NOTES_TABLE_CREATE)
        db?.execSQL(TAGS_TABLE_CREATE)
        db?.execSQL(LINKS_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TAGS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LINKS")
        onCreate(db)
    }

}
package com.example.lr3

import android.content.ContentProvider
import android.content.ContentValues
import android.content.IntentFilter
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri


public class NotesProvider: ContentProvider() {

    private lateinit var database: SQLiteDatabase

    companion object {
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        private val AUTHORITY = "com.example.LR3.notesprovider"
        private val BASE_PATH = "notes"
        public val CONTENT_URI = Uri.parse("content://$AUTHORITY/$BASE_PATH")

        private val NOTES = 1
        private val NOTES_ID = 2
        init {
            uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES)
            uriMatcher.addURI(AUTHORITY, "$BASE_PATH/#", NOTES_ID)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = database.insert(DBOpenHelper.TABLE_NOTES, null, values)
        val uri = Uri.parse("$BASE_PATH/$id")
        return uri
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return database.query(DBOpenHelper.TABLE_NOTES,
                              DBOpenHelper.ALL_COLUMNS,
                              selection,
                  null,
                      null,
                       null,
                      "${DBOpenHelper.NOTE_CREATED} DESC")

    }

    override fun onCreate(): Boolean {
        val helper = DBOpenHelper(context!!)
        database = helper.writableDatabase
        return true
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return database.update(DBOpenHelper.TABLE_NOTES, values, selection, selectionArgs)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return database.delete(DBOpenHelper.TABLE_NOTES, selection, selectionArgs)
    }

    override fun getType(uri: Uri): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
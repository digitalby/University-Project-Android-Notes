package com.example.lr3

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri


class NotesProvider: ContentProvider() {

    private lateinit var database: SQLiteDatabase

    companion object {
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        private const val AUTHORITY = "com.example.LR3.notesprovider"
        private const val BASE_PATH = "notes"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")

        private const val NOTES = 1
        private const val NOTES_ID = 2

        const val CONTENT_ITEM_TYPE = "Note"

        init {
            uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES)
            uriMatcher.addURI(AUTHORITY, "$BASE_PATH/#", NOTES_ID)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = database.insert(DBOpenHelper.TABLE_NOTES, null, values)
        return Uri.parse("$BASE_PATH/$id")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val newSelection = if(uriMatcher.match(uri) == NOTES_ID) {
            "${DBOpenHelper.NOTE_ID}=${uri.lastPathSegment}"
        } else {
            selection
        }

        return database.query(DBOpenHelper.TABLE_NOTES,
                              DBOpenHelper.ALL_COLUMNS,
                              newSelection,
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
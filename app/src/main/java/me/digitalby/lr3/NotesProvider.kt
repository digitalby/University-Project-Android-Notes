package me.digitalby.lr3

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

        private const val AUTHORITY = "me.digitalby.LR3.notesprovider"
        private const val BASE_PATH_NOTES = "notes"
        private const val BASE_PATH_TAGS = "tags"
        private const val BASE_PATH_LINKS = "links"
        val NOTES_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH_NOTES")
        val TAGS_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH_TAGS")
        val LINKS_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH_LINKS")

        private const val NOTES = 1 //the URI ID to notes path root
        private const val NOTES_ID = 2 //the URI ID to a specific note
        private const val TAGS = 3
        private const val TAGS_ID = 4
        private const val LINKS = 5
        private const val LINKS_ID = 6

        const val NOTE_ITEM_TYPE = "Note"

        init {
            uriMatcher.addURI(
                AUTHORITY,
                BASE_PATH_NOTES,
                NOTES
            )
            uriMatcher.addURI(
                AUTHORITY, "$BASE_PATH_NOTES/#",
                NOTES_ID
            )
            uriMatcher.addURI(
                AUTHORITY,
                BASE_PATH_TAGS,
                TAGS
            )
            uriMatcher.addURI(
                AUTHORITY, "$BASE_PATH_TAGS/#",
                TAGS_ID
            )
            uriMatcher.addURI(
                AUTHORITY,
                BASE_PATH_LINKS,
                LINKS
            )
            uriMatcher.addURI(
                AUTHORITY, "$BASE_PATH_LINKS/#",
                LINKS_ID
            )
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        var id: Long? = null
        var basePath: String? = null
        when(uriMatcher.match(uri)) {
            NOTES -> {
                id = database.insert(DBOpenHelper.TABLE_NOTES, null, values)
                basePath = BASE_PATH_NOTES
            }
            TAGS -> {
                id = database.insert(DBOpenHelper.TABLE_TAGS, null, values)
                basePath = BASE_PATH_TAGS
            }
            LINKS -> {
                id = database.insert(DBOpenHelper.TABLE_LINKS, null, values)
                basePath = BASE_PATH_LINKS
            }
        }
        return Uri.parse("$basePath/$id")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        when(uriMatcher.match(uri)) {
            NOTES -> return database.query(
                DBOpenHelper.TABLE_NOTES,
                DBOpenHelper.ALL_COLUMNS_NOTES,
                selection,
                null,
                null,
                null,
                sortOrder)
            NOTES_ID -> return database.query(
                DBOpenHelper.TABLE_NOTES,
                DBOpenHelper.ALL_COLUMNS_NOTES,
                "${DBOpenHelper.NOTE_ID}=${uri.lastPathSegment}",
                null,
                null,
                null,
                sortOrder)
            TAGS -> return database.query(
                DBOpenHelper.TABLE_TAGS,
                DBOpenHelper.ALL_COLUMNS_TAGS,
                selection,
                null,
                null,
                null,
                "${DBOpenHelper.TAG_NAME} ASC")
            TAGS_ID -> return database.query(
                DBOpenHelper.TABLE_TAGS,
                DBOpenHelper.ALL_COLUMNS_TAGS,
                "${DBOpenHelper.TAG_ID}=${uri.lastPathSegment}",
                null,
                null,
                null,
                "${DBOpenHelper.TAG_NAME} ASC")
            LINKS -> return database.query(
                DBOpenHelper.TABLE_LINKS,
                DBOpenHelper.ALL_COLUMNS_LINKS,
                selection,
                null,
                null,
                null,
                "${DBOpenHelper.LINK_NOTE_ID} DESC")
            LINKS_ID -> return database.query(
                DBOpenHelper.TABLE_LINKS,
                DBOpenHelper.ALL_COLUMNS_LINKS,
                "${DBOpenHelper.LINK_ID}=${uri.lastPathSegment}",
                null,
                null,
                null,
                "${DBOpenHelper.LINK_NOTE_ID} DESC")
        }
        return null
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
        when(uriMatcher.match(uri)) {
            NOTES -> return database.update(DBOpenHelper.TABLE_NOTES, values, selection, selectionArgs)
            TAGS -> return database.update(DBOpenHelper.TABLE_TAGS, values, selection, selectionArgs)
            LINKS -> return database.update(DBOpenHelper.TABLE_LINKS, values, selection, selectionArgs)
        }
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        when(uriMatcher.match(uri)) {
            NOTES -> return database.delete(DBOpenHelper.TABLE_NOTES, selection, selectionArgs)
            TAGS -> database.delete(DBOpenHelper.TABLE_TAGS, selection, selectionArgs)
            LINKS -> database.delete(DBOpenHelper.TABLE_LINKS, selection, selectionArgs)
        }
        return 0
    }

    override fun getType(uri: Uri): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
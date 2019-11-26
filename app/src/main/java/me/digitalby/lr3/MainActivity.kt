package me.digitalby.lr3

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CursorAdapter
import android.widget.EditText
import android.widget.GridView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import me.digitalby.lr3.R

enum class SortMode {
    Date,
    Title
}

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    companion object {
        const val EDITOR_REQUEST_CODE = 1001
    }

    private var currentNoteFilter: String? = null
    private var currentSortMode = SortMode.Date
    private lateinit var cursorAdapter: CursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cursorAdapter = NotesCursorAdapter(this, null, 0)
        val listView: ListView? = findViewById(R.id.list)
        listView?.adapter = cursorAdapter
        val gridView: GridView? = findViewById(R.id.grid)
        gridView?.adapter = cursorAdapter

        listView?.setOnItemClickListener { _, _, _, id ->
            val intent = Intent(this, EditorActivity::class.java)
            val uri = Uri.parse("${NotesProvider.NOTES_URI}/$id")
            intent.putExtra(NotesProvider.NOTE_ITEM_TYPE, uri)
            startActivityForResult(intent,
                EDITOR_REQUEST_CODE
            )
        }

        gridView?.setOnItemClickListener { _, _, _, id ->
            val intent = Intent(this, EditorActivity::class.java)
            val uri = Uri.parse("${NotesProvider.NOTES_URI}/$id")
            intent.putExtra(NotesProvider.NOTE_ITEM_TYPE, uri)
            startActivityForResult(intent,
                EDITOR_REQUEST_CODE
            )
        }

        val searchView: EditText = findViewById(R.id.searchTagsEditText)
        searchView.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search),
            null,
            null,
            null
        )
        searchView.addTextChangedListener { text ->
            val string = text.toString().trim()
            Log.d(null, "sv text changed to $string")
            currentNoteFilter = if(string.isNotEmpty()) {
                val notesCRUDHelper = NotesCRUDHelper(contentResolver)
                val list = notesCRUDHelper.findNotes(text.toString())
                if (list.isNotEmpty()) {
                    "${DBOpenHelper.NOTE_ID} IN (${list.joinToString()})"
                } else {
                    "${DBOpenHelper.NOTE_ID}=-1"
                }
            } else {
                null
            }
            restartLoader()
        }

        supportLoaderManager.initLoader(0, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val sortOrder = if(currentSortMode == SortMode.Date) "${DBOpenHelper.NOTE_CREATED} DESC"
                        else "${DBOpenHelper.NOTE_TITLE} ASC"
        return CursorLoader(this,
            NotesProvider.NOTES_URI,
                                    null,
                                    currentNoteFilter,
                                    null,
                                    sortOrder)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        cursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        cursorAdapter.swapCursor(null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == R.id.sort_by_date -> {
                currentSortMode = SortMode.Date
                restartLoader()
            }
            item.itemId == R.id.sort_by_title -> {
                currentSortMode = SortMode.Title
                restartLoader()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun restartLoader() {
        supportLoaderManager.restartLoader(0, null, this)
    }

    fun openEditorForNewNote(view: View) {
        val intent = Intent(this, EditorActivity::class.java)
        startActivityForResult(intent, EDITOR_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == EDITOR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            restartLoader()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

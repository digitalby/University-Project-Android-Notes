package com.example.lr3

import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var cursorAdapter: SimpleCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val cursor = contentResolver.query(NotesProvider.CONTENT_URI,
//            DBOpenHelper.ALL_COLUMNS,
//            null,
//            null,
//            null,
//            null)
        val from = arrayOf(DBOpenHelper.NOTE_TEXT)
        val to = intArrayOf(android.R.id.text1)

        cursorAdapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0)
        val listView: ListView = findViewById(R.id.list)
        listView.adapter = cursorAdapter

        supportLoaderManager.initLoader(0, null, this)
    }

    private fun insertNote(noteText: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        val noteUri = contentResolver.insert(NotesProvider.CONTENT_URI, values)
        Log.d("MainActivity", "Inserted note ${noteUri?.lastPathSegment}")
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                                    NotesProvider.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null)
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
        if(item.itemId == R.id.action_create_sample) {
            insertSampleData()
        } else if (item.itemId == R.id.action_delete_all) {
            deleteAllNotes()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllNotes() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                contentResolver.delete(
                    NotesProvider.CONTENT_URI,
                    null,
                    null
                    )
                restartLoader()
                Toast.makeText(this, getString(R.string.all_deleted),Toast.LENGTH_SHORT).show()
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.are_you_sure_delete_all_notes))
               .setTitle(getString(R.string.delete_all_notes))
               .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
               .setNegativeButton(getString(android.R.string.no), dialogClickListener)
               .show()
    }

    private fun insertSampleData() {
        insertNote("Simple note")
        insertNote("Multi-line\nnote")
        insertNote("Very long note with a long text that exceeds the width of the screen")
        restartLoader()
    }

    private fun restartLoader() {
        supportLoaderManager.restartLoader(0, null, this)
    }
}

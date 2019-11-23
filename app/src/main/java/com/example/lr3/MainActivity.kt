package com.example.lr3

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CursorAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    companion object {
        const val EDITOR_REQUEST_CODE = 1001
    }

    private lateinit var cursorAdapter: CursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val cursor = contentResolver.query(NotesProvider.CONTENT_URI,
//            DBOpenHelper.ALL_COLUMNS,
//            null,
//            null,
//            null,
//            null)

        cursorAdapter = NotesCursorAdapter(this, null, 0)
        val listView: ListView = findViewById(R.id.list)
        listView.adapter = cursorAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, EditorActivity::class.java)
            val uri = Uri.parse("${NotesProvider.CONTENT_URI}/$id")
            intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri)
            startActivityForResult(intent, EDITOR_REQUEST_CODE)
        }

        supportLoaderManager.initLoader(0, null, this)
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
        if (item.itemId == R.id.action_delete_all) {
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

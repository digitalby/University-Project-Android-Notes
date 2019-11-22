package com.example.lr3

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast

class EditorActivity : AppCompatActivity() {

    private var action = ""
    private lateinit var editor: EditText

    private var noteFilter: String? = null
    private var oldText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        editor = findViewById(R.id.editText)

        val intent = this.intent
        val uri: Uri? = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE)

        if (uri == null) {
            action = Intent.ACTION_INSERT
            title = "New note"
        } else {
            action = Intent.ACTION_EDIT
            noteFilter = "${DBOpenHelper.NOTE_ID}=${uri.lastPathSegment}"

            val cursor = contentResolver.query(uri, DBOpenHelper.ALL_COLUMNS, noteFilter, null, null)
            cursor?.moveToFirst()
            oldText = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT))
            editor.setText(oldText!!)
        }
    }

    private fun finishEditing() {
        val newText = editor.text.toString().trim()

        when(action) {
            Intent.ACTION_INSERT -> {
                if(newText.isEmpty()) {
                    setResult(Activity.RESULT_CANCELED)
                } else {
                    insertNote(newText)
                }
            }
            Intent.ACTION_EDIT -> {
                when {
                    newText.isEmpty() -> deleteNote()
                    oldText.equals(newText) -> setResult(Activity.RESULT_CANCELED)
                    else -> updateNote(newText)
                }
            }
        }
        finish()
    }

    private fun updateNote(noteText: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        contentResolver.update(NotesProvider.CONTENT_URI, values, noteFilter, null)
        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
    }

    private fun insertNote(noteText: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        contentResolver.insert(NotesProvider.CONTENT_URI, values)
        setResult(RESULT_OK)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun deleteNote() {
        contentResolver.delete(NotesProvider.CONTENT_URI, noteFilter, null)
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finishEditing()
            R.id.action_delete -> {
                //TODO make the button cancel when in "create" mode (right now only works in "edit")
                //TODO a confirmation dialog
                deleteNote()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finishEditing()
        super.onBackPressed()
    }
}

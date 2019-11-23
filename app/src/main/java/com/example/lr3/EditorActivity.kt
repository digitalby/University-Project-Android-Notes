package com.example.lr3

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class EditorActivity : AppCompatActivity() {

    private var action = ""
    private lateinit var titleEditText: EditText
    private lateinit var tagsEditText: EditText
    private lateinit var editor: EditText
    private lateinit var dateTimeTextView: TextView

    private lateinit var noteFilter: String
    private var oldText: String? = null
    private var oldTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        editor = findViewById(R.id.mainEditText)
        titleEditText = findViewById(R.id.titleEditText)
        tagsEditText = findViewById(R.id.tagsEditText)
        dateTimeTextView = findViewById(R.id.dateTimeTextView)

        val intent = this.intent
        val uri: Uri? = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE)

        if (uri == null) {
            action = Intent.ACTION_INSERT
            title = "New note"
            dateTimeTextView.text = getCurrentDate()
        } else {
            action = Intent.ACTION_EDIT
            noteFilter = "${DBOpenHelper.NOTE_ID}=${uri.lastPathSegment}"

            val cursor = contentResolver.query(uri, DBOpenHelper.ALL_COLUMNS, noteFilter, null, null)
            cursor?.moveToFirst()
            oldText = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT))
            oldTitle = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE))
            val timestamp = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED))
            dateTimeTextView.text = timestamp

            editor.setText(oldText!!)
            titleEditText.setText(oldTitle!!)
        }
    }

    private fun getCurrentDate(): String {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val date = simpleDateFormat.format(Date())
        return date
    }

    private fun processEmptyTitle(): String {
        val newTitle = getCurrentDate()
        titleEditText.setText(newTitle)
        return newTitle
    }

    private fun finishEditing() {
        val newText = editor.text.toString().trim()
        var newTitle = titleEditText.text.toString().trim()
        if(newTitle.isEmpty()) {
            newTitle = processEmptyTitle()
        }
        when(action) {
            Intent.ACTION_INSERT -> {
                insertNote(newTitle, newText)
            }
            Intent.ACTION_EDIT -> {
                updateNote(newTitle, newText)
            }
        }
        finish()
    }

    private fun updateNote(noteTitle: String, noteText: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle)
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        values.put(DBOpenHelper.NOTE_CREATED, getCurrentDate())
        contentResolver.update(NotesProvider.CONTENT_URI, values, noteFilter, null)
        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
    }

    private fun insertNote(noteTitle: String, noteText: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle)
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        values.put(DBOpenHelper.NOTE_CREATED, getCurrentDate())
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
                val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        when(action) {
                            Intent.ACTION_INSERT -> {
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            }
                            Intent.ACTION_EDIT -> {
                                deleteNote()
                            }
                        }
                    }
                }

                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.are_you_sure_delete_note))
                    .setTitle(getString(R.string.delete_note))
                    .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finishEditing()
        super.onBackPressed()
    }
}

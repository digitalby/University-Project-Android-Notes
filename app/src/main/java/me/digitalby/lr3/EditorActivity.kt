package me.digitalby.lr3

import android.app.Activity
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
import me.digitalby.lr3.R


class EditorActivity : AppCompatActivity() {

    private lateinit var action: String
    private lateinit var crudHelper: NotesCRUDHelper

    private lateinit var titleEditText: EditText
    private lateinit var tagsEditText: EditText
    private lateinit var editor: EditText
    private lateinit var dateTimeTextView: TextView

    private var id: String? = null
    private var oldText: String? = null
    private var oldTitle: String? = null
    private var oldTagString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        editor = findViewById(R.id.mainEditText)
        titleEditText = findViewById(R.id.titleEditText)
        tagsEditText = findViewById(R.id.tagsEditText)
        dateTimeTextView = findViewById(R.id.dateTimeTextView)

        val intent = this.intent
        val uri: Uri? = intent.getParcelableExtra(NotesProvider.NOTE_ITEM_TYPE)
        crudHelper = NotesCRUDHelper(contentResolver)

        if (uri == null) {
            action = Intent.ACTION_INSERT
            title = "New note"
            dateTimeTextView.text = DateTimeHelper.getFormattedDate()
        } else {
            action = Intent.ACTION_EDIT
            id = uri.lastPathSegment
            val noteFilter = "${DBOpenHelper.NOTE_ID}=$id"

            val cursor = contentResolver.query(uri,
                DBOpenHelper.ALL_COLUMNS_NOTES, noteFilter, null, null)
            cursor?.moveToFirst()
            oldText = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT))!!
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE))!!
            oldTagString = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TAGSTRING))!!
            val timestamp = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED))
            dateTimeTextView.text = timestamp

            editor.setText(oldText)
            titleEditText.setText(oldTitle)
            tagsEditText.setText(oldTagString)
            cursor.close()
        }
    }

    private fun processEmptyTitle(): String {
        val newTitle = DateTimeHelper.getFormattedDate()
        titleEditText.setText(newTitle)
        return newTitle
    }

    private fun finishEditing() {
        val newText = editor.text.toString().trim()
        var newTitle = titleEditText.text.toString().trim()
        val newTagString = tagsEditText.text.toString().trim()
        if(newTitle.isEmpty()) {
            newTitle = processEmptyTitle()
        }
        when(action) {
            Intent.ACTION_INSERT -> {
                crudHelper.insertNote(newTitle, newText, newTagString)
                setResult(RESULT_OK)
            }
            Intent.ACTION_EDIT -> {
                val updateTags = oldTagString != newTagString
                crudHelper.updateNote(id!!.toInt(), newTitle, newText, newTagString, updateTags)
                setResult(RESULT_OK)
            }
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finishEditing()
            R.id.action_delete -> {
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        when(action) {
                            Intent.ACTION_INSERT -> {
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            }
                            Intent.ACTION_EDIT -> {
                                crudHelper.deleteNote(id!!.toInt())
                                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
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

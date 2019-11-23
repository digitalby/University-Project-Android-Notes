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

        if (uri == null) {
            action = Intent.ACTION_INSERT
            title = "New note"
            dateTimeTextView.text = getCurrentDate()
        } else {
            action = Intent.ACTION_EDIT
            noteFilter = "${DBOpenHelper.NOTE_ID}=${uri.lastPathSegment}"

            val cursor = contentResolver.query(uri, DBOpenHelper.ALL_COLUMNS_NOTES, noteFilter, null, null)
            cursor?.moveToFirst()
            oldText = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT))
            oldTitle = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE))
            oldTagString = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TAGSTRING))
            val timestamp = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED))
            dateTimeTextView.text = timestamp

            editor.setText(oldText!!)
            titleEditText.setText(oldTitle!!)
            tagsEditText.setText(oldTagString!!)
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
        val newTags = tagsEditText.text.toString().trim()
        if(newTitle.isEmpty()) {
            newTitle = processEmptyTitle()
        }
        when(action) {
            Intent.ACTION_INSERT -> {
                insertNote(newTitle, newText, newTags)
            }
            Intent.ACTION_EDIT -> {
                updateNote(newTitle, newText, newTags)
            }
        }
        finish()
    }

    private fun updateNote(noteTitle: String, noteText: String, noteTagString: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle)
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        values.put(DBOpenHelper.NOTE_CREATED, getCurrentDate())
        if(oldTagString == noteTagString) {
            values.put(DBOpenHelper.NOTE_TAGSTRING, oldTagString)
        } else {
            val uri: Uri? = intent.getParcelableExtra(NotesProvider.NOTE_ITEM_TYPE)
            val id = uri?.lastPathSegment?.toInt()
            unlinkTags(id!!)
            val tagList = tagStringToList(noteTagString)
            val tagString = tagList.joinToString()
            values.put(DBOpenHelper.NOTE_TAGSTRING, tagString)
            val tagsToAdd = insertTagsIfNeeded(tagList)
            linkTags(tagsToAdd, id)
            sanitizeTags()
        }

        contentResolver.update(NotesProvider.NOTES_URI, values, noteFilter, null)
        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
    }

    private fun insertNote(noteTitle: String, noteText: String, noteTagString: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle)
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        values.put(DBOpenHelper.NOTE_CREATED, getCurrentDate())


        val tagList = tagStringToList(noteTagString)
        val tagString = tagList.joinToString()
        values.put(DBOpenHelper.NOTE_TAGSTRING, tagString)
        val uri = contentResolver.insert(NotesProvider.NOTES_URI, values)
        val id = uri?.lastPathSegment?.toInt()
        val tagsToAdd = insertTagsIfNeeded(tagList)
        linkTags(tagsToAdd, id!!)

        setResult(RESULT_OK)
    }

    fun tagStringToList(tagString: String): List<String> {
        var tags = tagString.split(',')
        tags = tags.map { tag -> tag.trim() }
        tags = tags.distinct()
        tags = tags.filter { tag -> !tag.isNullOrEmpty() }
        tags = tags.sorted()
        return tags
    }

    fun linkTags(tagList: List<Int>, noteID: Int) {
        for(tagID in tagList) {
            val linkValues = ContentValues()
            linkValues.put(DBOpenHelper.LINK_NOTE_ID, noteID)
            linkValues.put(DBOpenHelper.LINK_TAG_ID, tagID)
            contentResolver.insert(NotesProvider.LINKS_URI, linkValues)
        }
    }

    fun unlinkTags(noteId: Int) {
        val selection = "${DBOpenHelper.LINK_NOTE_ID} = $noteId"
        val cursor = contentResolver.delete(NotesProvider.LINKS_URI, selection, null)
    }

    fun sanitizeTags() {
        val cursor = contentResolver.query(NotesProvider.TAGS_URI,
            DBOpenHelper.ALL_COLUMNS_TAGS, null, null, null)
        if(cursor?.moveToFirst() == true) {
            val tag = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TAG_NAME))
            val selection = "${DBOpenHelper.TAG_NAME} = '$tag'"
            val cursorId = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.TAG_ID))
            cursor.close()
            var linkSelection = "${DBOpenHelper.LINK_TAG_ID} = $cursorId"
            val cursorLinks = contentResolver.query(NotesProvider.LINKS_URI,
                DBOpenHelper.ALL_COLUMNS_LINKS, linkSelection, null, null)
            if(cursorLinks?.moveToFirst() == false) {
                contentResolver.delete(NotesProvider.TAGS_URI, selection, null)
            }
            cursorLinks?.close()
        }
    }

    fun insertTagsIfNeeded(tags: List<String>): List<Int>{
        val ret = mutableListOf<Int>()

        for(tag in tags) {
            val selection = "${DBOpenHelper.TAG_NAME} = '$tag'"
            val cursor = contentResolver.query(NotesProvider.TAGS_URI,
                DBOpenHelper.ALL_COLUMNS_TAGS, selection, null, null)
            if(cursor?.moveToFirst() == true) {
                ret.add(cursor.getInt(cursor.getColumnIndex(DBOpenHelper.TAG_ID)))
            } else {
                val valuesTags = ContentValues()
                valuesTags.put(DBOpenHelper.TAG_NAME, tag)
                val uri = contentResolver.insert(NotesProvider.TAGS_URI, valuesTags)
                val id = uri?.lastPathSegment?.toInt()
                if(id != null)
                    ret.add(id)
            }
            cursor?.close()
        }

        return ret
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun deleteNote() {
        contentResolver.delete(NotesProvider.NOTES_URI, noteFilter, null)
        val uri: Uri? = intent.getParcelableExtra(NotesProvider.NOTE_ITEM_TYPE)
        val id = uri?.lastPathSegment?.toInt()
        unlinkTags(id!!)
        sanitizeTags()
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

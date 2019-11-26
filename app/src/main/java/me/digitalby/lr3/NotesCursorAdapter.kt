package me.digitalby.lr3

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import me.digitalby.lr3.R

class NotesCursorAdapter(context: Context?, cursor: Cursor?, flags: Int):
    CursorAdapter(context, cursor, flags) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        val item = if(parent?.id == R.id.list) R.layout.note_list_item else R.layout.note_grid_item
            return LayoutInflater.from(context).inflate(item, parent, false)

    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val titleTextView: TextView? = view?.findViewById(R.id.textViewNote)
        val dateTimeTextView: TextView? = view?.findViewById(R.id.textViewDateTime)
        val textViewTags: TextView? = view?.findViewById(R.id.textViewTags)
        val titleTextViewGrid: TextView? = view?.findViewById(R.id.textViewNoteGrid)
        val dateTimeTextViewGrid: TextView? = view?.findViewById(R.id.textViewDateTimeGrid)
        val textViewTagsGrid: TextView? = view?.findViewById(R.id.textViewTagsGrid)

        val noteTitle = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE))
        titleTextView?.text = noteTitle
        titleTextViewGrid?.text = noteTitle
//        if(titleTextViewGrid?.lineCount!! > 2) {
//            titleTextViewGrid.ellipsize
//        } else {
//            titleTextViewGrid.setLines(2)
//        }

        val noteDateTime = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED))
        dateTimeTextView?.text = noteDateTime
        dateTimeTextViewGrid?.text = noteDateTime

        val noteTagString = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TAGSTRING))
        textViewTags?.text = if(noteTagString.isNullOrEmpty()) "<no tags>" else noteTagString
        if(noteTagString.isNullOrEmpty())
            textViewTagsGrid?.text = "<no tags>"
        else {
            val number = noteTagString.count {char -> char == ','} + 1
            if(number == 1)
                textViewTagsGrid?.text = "<1 tag>"
            else
                textViewTagsGrid?.text = "<$number tags>"
        }
    }



}
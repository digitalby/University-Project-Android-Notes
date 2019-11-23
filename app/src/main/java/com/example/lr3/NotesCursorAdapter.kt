package com.example.lr3

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView

class NotesCursorAdapter(context: Context?, cursor: Cursor?, flags: Int):
    CursorAdapter(context, cursor, flags) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val noteTitle = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE))
        val titleTextView: TextView? = view?.findViewById(R.id.textViewNote)
        titleTextView?.text = noteTitle

        val noteDateTime = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED))
        val dateTimeTextView: TextView? = view?.findViewById(R.id.textViewDateTime)
        dateTimeTextView?.text = noteDateTime
    }

}
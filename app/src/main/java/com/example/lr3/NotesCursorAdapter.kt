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
        var noteText = cursor?.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT))
        val lineFeedPosition = noteText?.indexOf('\n')
        if(lineFeedPosition != -1) {
            noteText = "${noteText?.substring(0, lineFeedPosition!!)}…"
        }
        val textView: TextView? = view?.findViewById(R.id.textViewNote)
        textView?.text = noteText
    }

}
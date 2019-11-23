package com.example.lr3

import android.content.ContentResolver
import android.content.ContentValues

class NotesCRUDHelper(private val contentResolver: ContentResolver) {

    fun insertNote(noteTitle: String, noteText: String, noteTagString: String) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle)
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        values.put(DBOpenHelper.NOTE_CREATED, DateTimeHelper.getFormattedDate())


        val tagList = tagStringToList(noteTagString)
        val tagString = tagList.joinToString()
        values.put(DBOpenHelper.NOTE_TAGSTRING, tagString)
        val uri = contentResolver.insert(NotesProvider.NOTES_URI, values)
        val id = uri?.lastPathSegment?.toInt()
        val tagsToAdd = insertTagsIfNeeded(tagList)
        linkTags(tagsToAdd, id!!)
    }

    fun updateNote(noteId: Int, noteTitle: String, noteText: String, noteTagString: String, updateTags: Boolean = true) {
        val values = ContentValues()
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle)
        values.put(DBOpenHelper.NOTE_TEXT, noteText)
        values.put(DBOpenHelper.NOTE_CREATED, DateTimeHelper.getFormattedDate())

        if(updateTags) {
            unlinkTags(noteId)
            val tagList = tagStringToList(noteTagString)
            val tagString = tagList.joinToString()
            values.put(DBOpenHelper.NOTE_TAGSTRING, tagString)
            val tagsToAdd = insertTagsIfNeeded(tagList)
            linkTags(tagsToAdd, noteId)
            sanitizeTags()
        } else {
            values.put(DBOpenHelper.NOTE_TAGSTRING, noteTagString)
        }

        val noteFilter = "${DBOpenHelper.NOTE_ID}=$noteId"
        contentResolver.update(NotesProvider.NOTES_URI, values, noteFilter, null)
    }

    fun deleteNote(noteId: Int) {
        val noteFilter = "${DBOpenHelper.NOTE_ID}=$noteId"
        contentResolver.delete(NotesProvider.NOTES_URI, noteFilter, null)
        unlinkTags(noteId)
        sanitizeTags()
    }

    private fun tagStringToList(tagString: String): List<String> {
        var tags = tagString.split(',')
        tags = tags.map { tag -> tag.trim() }
        tags = tags.distinct()
        tags = tags.filter { tag -> tag.isNotEmpty() }
        tags = tags.sorted()
        return tags
    }

    private fun linkTags(tagList: List<Int>, noteID: Int) {
        for(tagID in tagList) {
            val linkValues = ContentValues()
            linkValues.put(DBOpenHelper.LINK_NOTE_ID, noteID)
            linkValues.put(DBOpenHelper.LINK_TAG_ID, tagID)
            contentResolver.insert(NotesProvider.LINKS_URI, linkValues)
        }
    }

    private fun unlinkTags(noteId: Int) {
        val selection = "${DBOpenHelper.LINK_NOTE_ID} = $noteId"
        contentResolver.delete(NotesProvider.LINKS_URI, selection, null)
    }

    private fun sanitizeTags() {
        val cursor = contentResolver.query(NotesProvider.TAGS_URI,
            DBOpenHelper.ALL_COLUMNS_TAGS, null, null, null)
        if(cursor?.moveToFirst() == true) {
            val tag = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TAG_NAME))
            val selection = "${DBOpenHelper.TAG_NAME} = '$tag'"
            val cursorId = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.TAG_ID))
            cursor.close()
            val linkSelection = "${DBOpenHelper.LINK_TAG_ID} = $cursorId"
            val cursorLinks = contentResolver.query(NotesProvider.LINKS_URI,
                DBOpenHelper.ALL_COLUMNS_LINKS, linkSelection, null, null)
            if(cursorLinks?.moveToFirst() == false) {
                contentResolver.delete(NotesProvider.TAGS_URI, selection, null)
            }
            cursorLinks?.close()
        }
    }

    private fun insertTagsIfNeeded(tags: List<String>): List<Int>{
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
}
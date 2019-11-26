# Android Notes (University Project)

![screenshot][screenshot]

The end goal of the assigment was to create a note-taking app for Android. The project has been created within three days. The following topics had to be covered:

  * **Building a CRUD app**
  * **Material design**
  * **Better app architecture**
  * **Data persistency**
  * **Intents**
  * **ListView & GridView**
  * **Date & Time**

SQLite is used for data persistency, with a ```NotesProvider``` implemented to handle CRUD calls to the database. This allows to retrieve data to be displayed in a ```ListView``` (portrait) or ```GridView``` (landscape) using a custom ```NotesCursorAdapter```.

All CRUD operations are done asynchronously with the help of a ```CursorLoader```. ```NotesCRUDHelper``` provides an even higher level of abstraction to facilitate inserting, updating, deleting, and querying notes and/or their tags. The user can also sort their notes, either by date or by title.

The app features Material Design elements (specifically, a ```FloatingActionButton```), with a blue color theme and pre-imported icons created by Google.

[screenshot]: screenshot.png
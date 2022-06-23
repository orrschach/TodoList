package com.example.todolist

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri


class TodoProvider : ContentProvider() {

    private val todoDir = 0
    private val todoItem = 1

    lateinit var helper:DBHelper

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    private val authority = "com.example.todolist.provider"

    init{
        uriMatcher.addURI(authority,"todo",todoDir)
        uriMatcher.addURI(authority,"todo/#",todoItem)
    }

    override fun onCreate(): Boolean {
        helper = DBHelper(context,"todo.db",3)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        var rs:Cursor? = null
        val db= helper.readableDatabase
        when(uriMatcher.match(uri)){
            todoDir->{
                rs = db.query(Todo.TABLE,projection,selection,selectionArgs,null,null,sortOrder)
            }
            todoItem->{
                val id = ContentUris.parseId(uri)
                rs = db.query(Todo.TABLE,projection,"id=?", arrayOf(id.toString()),null,null,sortOrder)
            }
        }
        return rs
    }

    override fun getType(uri: Uri): String? {
        val dir = "vnd.android.cursor.dir/vnd.$authority.todo"
        val item = "vnd.android.cursor.dir/vnd.$authority.todo"
        return when(uriMatcher.match(uri)){
            todoDir->dir
            todoItem->item
            else-> dir
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        var rs:Uri? = null

        val db = helper.writableDatabase
        when(uriMatcher.match(uri)){
            todoDir->{
                val id = db.insert(Todo.TABLE,null,values)
                rs = Uri.parse("content://$authority/todo/$id")
            }
        }
        db.close()
        return rs
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        var rs = 0
        val db = helper.writableDatabase
        when(uriMatcher.match(uri)){
//            todoDir->{}
            todoItem->{
                val id = ContentUris.parseId(uri)
                rs = db.delete(Todo.TABLE,"id=?", arrayOf(id.toString()))
            }
        }
        db.close()
        return rs
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}
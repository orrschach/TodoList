package com.example.todolist

import android.annotation.SuppressLint
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.StringBuilder
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    val TAG = "@@MAIN"
    val KEY_ITEM = "item"

    lateinit var helper:DBHelper
    lateinit var adapter: TodoAdapter

    private var toUpdate:Todo? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        helper = DBHelper(this,"todo.db",3)

        adapter = TodoAdapter()

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

//        readFromSp()
//        readInFile()
        readInDb()

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            findViewById<EditText>(R.id.ipt_text).setVisibility(View.GONE)
            findViewById<Button>(R.id.btn_save).setVisibility(View.GONE)
            saveInput()
        }
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showEdit()
        }
    }

    private fun showEdit() {
        Log.d(TAG,"clicked!")
        findViewById<EditText>(R.id.ipt_text).setVisibility(View.VISIBLE)
        findViewById<Button>(R.id.btn_save).setVisibility(View.VISIBLE)
    }

    @SuppressLint("Range")
    private fun readInDb() {
        val db = helper.readableDatabase
        val cursor = db.query(Todo.TABLE,null,null,null,null,null,
            "${Todo.COL_ID} desc ")
        val arr = arrayListOf<Todo>()
        if(cursor.moveToFirst()){
            do{
                arr.add(
                    Todo(
                        cursor.getString(cursor.getColumnIndex(Todo.COL_CONTENT)),
                        cursor.getLong(cursor.getColumnIndex(Todo.COL_TIME)),
                    ).apply {
                        id = cursor.getInt(cursor.getColumnIndex(Todo.COL_ID))
                    }
                )
            }while (cursor.moveToNext())
        }

        adapter.setData(arr)

        cursor.close()
    }

    private fun readInFile() {
        try {
            val input = this.openFileInput("todo.txt")
            val reader = BufferedReader(InputStreamReader(input))
            val buffer = StringBuilder()
            reader.use {
                it.forEachLine {
                    Log.d(TAG,"read line : $it")
                    buffer.append(it + "\n")
                }
            }
            findViewById<TextView>(R.id.txt_saved).text = buffer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readFromSp() {
        val sp = this.getSharedPreferences("todo", MODE_PRIVATE)
        val txt = sp.getString(KEY_ITEM,"no value")

        findViewById<TextView>(R.id.txt_saved).text = txt
    }

    private fun saveInput() {
        val text = findViewById<EditText>( R.id.ipt_text).text.toString()

//        writeInSp(text)
//        writeInFile(text)

        saveInDb(text)
    }

    private fun saveInDb(text: String) {
        val db = helper.writableDatabase
        val item = Todo(text,System.currentTimeMillis())
        val values = ContentValues().apply {
            put(Todo.COL_CONTENT,item.content)
            put(Todo.COL_TIME,item.createTime)
        }
        var rs = -1
        if(toUpdate != null){
            item.id = toUpdate?.id
            Log.d(TAG,"update row id = $rs")
            rs = db.update(Todo.TABLE,values,"id = ?", arrayOf(toUpdate?.id.toString()))
            if(rs != -1 ){
                adapter.replaceItem(toUpdate?.id,item)
                toUpdate = null
            }
        }else{
            Log.d(TAG,"insert row id = $rs")
            rs = db.insert(Todo.TABLE,null,values).toInt()
            if(rs != -1 ){
                item.id = rs
                adapter.addItem(item)
            }
        }

        Toast.makeText(this,if(rs < 0) "保存失败" else "保存成功" , Toast.LENGTH_LONG).show()

        // 置空
        setInputText("")

    }

    private fun setInputText(text:String){
        findViewById<EditText>(R.id.ipt_text).setText(text)
    }

    private fun writeInFile(text: String) {

        val output = this.openFileOutput("todo.txt", MODE_PRIVATE)
        val writer = BufferedWriter(OutputStreamWriter(output))
        writer.use {
            it.write(text)
        }
//        writer.write(text)
//        writer.close()
    }

    private fun writeInSp(text: String) {
        val sp = this.getSharedPreferences("todo", MODE_PRIVATE)

        sp.edit().let {
            it.putString(KEY_ITEM,text)
            it.apply()
            // it.commit()
        }

    }

    inner class TodoAdapter : RecyclerView.Adapter<TodoViewHolder>() {

        val data  = arrayListOf<Todo>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo,parent,false)
            return TodoViewHolder(view).apply {
                id = view.findViewById(R.id.id)
                content = view.findViewById(R.id.content)
                btnUpdate = view.findViewById(R.id.btn_update)
                btnDelete = view.findViewById(R.id.btn_delete)
            }
        }

        override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
            holder.render(data[position])
            holder.itemView.animation =
                AnimationUtils.loadAnimation(holder.itemView.context, R.anim.anim1)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        fun setData(arr: ArrayList<Todo>) {
            data.addAll(arr)
            notifyDataSetChanged()
        }

        fun addItem(item: Todo) {
            data.add(0,item)
            notifyItemInserted(0)
        }

        fun replaceItem(id: Int?, item: Todo) {
            val idx = findIdx(id)
            if(idx >= 0) {
                data.set(idx, item)
                notifyItemChanged(idx)
            }
        }

        private fun findIdx(id: Int?): Int {
            var idx = -1
            data.forEachIndexed { index, todo ->
                if(todo.id == id){
                    idx = index
                }
            }
            return idx
        }

        fun itemDeleted(id: Int?) {
            val idx = findIdx(id)
            if(idx >= 0){
                data.removeAt(idx)
                notifyItemRemoved(idx)
            }
        }

    }

    inner class TodoViewHolder(view: View): RecyclerView.ViewHolder(view){

        var id:TextView? = null
        var content:TextView? = null
        var btnDelete:TextView? = null
        var btnUpdate:TextView? = null

        fun render(todo: Todo) {
            id?.text = todo.id.toString()
            content?.text = todo.content
            btnDelete?.setOnClickListener {
                Log.d(TAG,"to delete id = ${todo.id} ")
                val db = helper.writableDatabase
                db.delete(Todo.TABLE,"id = ?", arrayOf(todo.id.toString()))
                adapter.itemDeleted(todo.id)
            }
            btnUpdate?.setOnClickListener {
                Log.d(TAG,"to update id = ${todo.id} ")
                toUpdate = todo
                setInputText(todo.content)
            }
        }
    }
}
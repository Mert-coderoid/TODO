package com.cem.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Paint
import android.content.ContentValues
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private lateinit var editTextTask: EditText
    private lateinit var buttonAdd: Button
    private lateinit var recyclerViewTodo: RecyclerView
    private val todoList = mutableListOf<TodoItem>()
    private val adapter = TodoAdapter(todoList, this)

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId != -1) {
            val dbHelper = TodoDatabaseHelper(this)
            val userTodos = dbHelper.getTodosByUserId(userId)
            todoList.addAll(userTodos)
            adapter.notifyDataSetChanged()
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Oturum bilgisini sıfırlayın ve giriş ekranına yönlendirin
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // MainActivity'yi kapat
        }

        if (!isUserLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        editTextTask = findViewById(R.id.editTextTask)
        buttonAdd = findViewById(R.id.buttonAdd)
        recyclerViewTodo = findViewById(R.id.recyclerViewTodo)

        buttonAdd.setOnClickListener {
            val taskText = editTextTask.text.toString()
            if (taskText.isNotEmpty()) {
                val dbHelper = TodoDatabaseHelper(this)

                val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                val userId = sharedPreferences.getInt("userId", -1)

                if (userId != -1) {
                    val newItemId = dbHelper.addTodoItem(TodoItem(0, taskText, userId, false))
                    val newItem = TodoItem(newItemId.toInt(), taskText, userId, false)
                    todoList.add(newItem)
                    adapter.notifyDataSetChanged() // Listeyi günceller
                    editTextTask.text.clear() // Metin kutusunu temizler
                }
            }
        }
        recyclerViewTodo.adapter = adapter
        recyclerViewTodo.layoutManager = LinearLayoutManager(this)
    }
}

class TodoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ${TodoContract.TodoEntry.TABLE_NAME} (" +
                "${TodoContract.TodoEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${TodoContract.TodoEntry.COLUMN_TASK} TEXT," +
                "${TodoContract.TodoEntry.COLUMN_USER_ID} INTEGER," +
                "${TodoContract.TodoEntry.COLUMN_COMPLETED} INTEGER)")

        db.execSQL("CREATE TABLE ${UserContract.UserEntry.TABLE_NAME} (" +
                "${UserContract.UserEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${UserContract.UserEntry.COLUMN_USERNAME} TEXT," +
                "${UserContract.UserEntry.COLUMN_PASSWORD} TEXT)")
    }

    fun getUserId(username: String): Int {
        val db = this.readableDatabase
        val cursor = db.query(UserContract.UserEntry.TABLE_NAME, arrayOf(UserContract.UserEntry.COLUMN_ID),
            "${UserContract.UserEntry.COLUMN_USERNAME}=?", arrayOf(username), null, null, null)
        if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_ID))
            cursor.close()
            return userId
        }
        cursor.close()
        return -1 // Kullanıcı bulunamadı
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${TodoContract.TodoEntry.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${UserContract.UserEntry.TABLE_NAME}")
        onCreate(db)
    }

    fun addTodoItem(item: TodoItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(TodoContract.TodoEntry.COLUMN_TASK, item.description)
            put(TodoContract.TodoEntry.COLUMN_USER_ID, item.userId)
            put(TodoContract.TodoEntry.COLUMN_COMPLETED, item.isCompleted)
        }
        return db.insert(TodoContract.TodoEntry.TABLE_NAME, null, values)
    }
    fun addUser(username: String, password: String) : Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(UserContract.UserEntry.COLUMN_USERNAME, username)
            put(UserContract.UserEntry.COLUMN_PASSWORD, password)
        }
        return db.insert(UserContract.UserEntry.TABLE_NAME, null, values)
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(UserContract.UserEntry.TABLE_NAME, null,
            "${UserContract.UserEntry.COLUMN_USERNAME}=? AND ${UserContract.UserEntry.COLUMN_PASSWORD}=?",
            arrayOf(username, password), null, null, null)
        val userExist = cursor.count > 0
        cursor.close()
        return userExist
    }

    fun userExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            UserContract.UserEntry.TABLE_NAME, arrayOf(UserContract.UserEntry.COLUMN_ID),
            "${UserContract.UserEntry.COLUMN_USERNAME}=?", arrayOf(username), null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun updateTodoItem(item: TodoItem) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(TodoContract.TodoEntry.COLUMN_COMPLETED, if (item.isCompleted) 1 else 0)
        }
        db.update(TodoContract.TodoEntry.TABLE_NAME, values, "${TodoContract.TodoEntry.COLUMN_ID}=?", arrayOf(item.id.toString()))
        db.close()
    }

    fun getTodosByUserId(userId: Int): List<TodoItem> {
        val todos = mutableListOf<TodoItem>()
        val db = this.readableDatabase
        val cursor = db.query(
            TodoContract.TodoEntry.TABLE_NAME, null,
            "${TodoContract.TodoEntry.COLUMN_USER_ID}=?", arrayOf(userId.toString()), null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_ID))
                val task = cursor.getString(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_TASK))
                val completed = cursor.getInt(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_COMPLETED)) == 1
                val todoItem = TodoItem(id, task, userId, completed)
                todos.add(todoItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return todos
    }

    fun deleteTodoItem(id: Int) {
        val db = writableDatabase
        db.delete(TodoContract.TodoEntry.TABLE_NAME, "${TodoContract.TodoEntry.COLUMN_ID}=?", arrayOf(id.toString()))
        db.close()
    }

    companion object {
        const val DATABASE_VERSION = 4 // Sürümü artırın
        const val DATABASE_NAME = "TodoDatabase.db"
    }
}

class TodoAdapter(private val items: MutableList<TodoItem>, private val context: Context) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewItem)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.description

        // Görev tamamlandıysa metin üzerine çizgi çek
        holder.textView.paintFlags = if (item.isCompleted) {
            holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Silme butonu için tıklama işleyicisi
        holder.deleteButton.setOnClickListener {
            // Veritabanından ve listeden sil
            val dbHelper = TodoDatabaseHelper(context)
            dbHelper.deleteTodoItem(item.id)
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }

        // Metin üzerine tıklandığında tamamlanma durumunu güncelle
        holder.textView.setOnClickListener {
            item.isCompleted = !item.isCompleted
            val dbHelper = TodoDatabaseHelper(context)
            dbHelper.updateTodoItem(item)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = items.size
}

data class TodoItem(val id: Int, val description: String, val userId: Int, var isCompleted: Boolean)

object TodoContract {
    object TodoEntry {
        const val TABLE_NAME = "todo"
        const val COLUMN_ID = "id"
        const val COLUMN_TASK = "task"
        const val COLUMN_COMPLETED = "completed"
        const val COLUMN_USER_ID = "user_id"
    }
}

object UserContract {
    object UserEntry {
        const val TABLE_NAME = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
    }
}

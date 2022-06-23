package com.example.todolist

data class Todo(
    val content: String,
    val createTime: Long,
) {
    companion object {
        val TABLE = "Todo"
        val COL_ID = "id"
        val COL_CONTENT = "content"
        val COL_TIME = "createTime"
    }

    var id: Int? = null

}
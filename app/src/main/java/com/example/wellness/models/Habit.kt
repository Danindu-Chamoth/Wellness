package com.example.wellness.models

data class Habit(
    val id: Int,
    var name: String,
    var isCompleted: Boolean = false
) {
    companion object {
        private var idCounter = 0
        fun getNextId(): Int {
            return idCounter++
        }
    }
}

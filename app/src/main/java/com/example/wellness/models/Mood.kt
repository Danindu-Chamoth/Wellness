package com.example.wellness.models

import java.util.Date

data class Mood(
    val emoji: String,
    val note: String,
    val dateTime: Date,
    val moodType: String
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "emoji" to emoji,
            "note" to note,
            "dateTime" to dateTime.time,
            "moodType" to moodType
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Mood {
            return Mood(
                emoji = map["emoji"] as String,
                note = map["note"] as String,
                dateTime = Date(map["dateTime"] as Long),
                moodType = map["moodType"] as String
            )
        }
    }
}

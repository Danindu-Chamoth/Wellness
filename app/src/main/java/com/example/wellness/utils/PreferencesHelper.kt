package com.example.wellness.utils

import android.content.Context
import com.example.wellness.models.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("WellnessPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMoods(moods: List<Mood>) {
        val moodsJson = gson.toJson(moods)
        sharedPreferences.edit().putString("moods", moodsJson).apply()
    }

    fun getMoods(): List<Mood> {
        val moodsJson = sharedPreferences.getString("moods", null)
        return if (moodsJson != null) {
            val type = object : TypeToken<List<Mood>>() {}.type
            gson.fromJson(moodsJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
}

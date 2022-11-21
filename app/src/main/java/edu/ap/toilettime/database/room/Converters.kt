package edu.ap.toilettime.database.room

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import edu.ap.toilettime.model.User

class Converters {
    @TypeConverter
    fun listFromString(value: String?): ArrayList<User?>? {
        val listType = object : TypeToken<ArrayList<User?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun stringFromArrayList(list: ArrayList<User?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}
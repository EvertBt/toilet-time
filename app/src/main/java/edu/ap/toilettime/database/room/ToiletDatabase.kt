package edu.ap.toilettime.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.ap.toilettime.model.Toilet

@Database(entities = [Toilet::class], version = 1)
@TypeConverters(Converters::class)
abstract class ToiletDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDAO
}
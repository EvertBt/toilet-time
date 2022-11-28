package edu.ap.toilettime.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.ap.toilettime.model.Toilet

@Dao
interface ToiletDAO {
    @Query("SELECT * FROM toilet")
    fun getAll(): List<Toilet>

    @Insert
    fun insertOne(toilet: Toilet)

    @Insert
    fun insertAll(toilets: List<Toilet>)

    @Update
    fun update(toilet: Toilet)

    @Delete
    fun delete(toilet: Toilet)
}
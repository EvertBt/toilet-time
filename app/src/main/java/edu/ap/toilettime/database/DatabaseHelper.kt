package edu.ap.toilettime.database

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import edu.ap.toilettime.activities.MainActivity
import edu.ap.toilettime.activities.NearbyToiletsActivity
import edu.ap.toilettime.database.room.ToiletDatabase
import edu.ap.toilettime.model.Toilet

class DatabaseHelper(private val mainActivity: MainActivity?, private val nearbyToiletsActivity: NearbyToiletsActivity?) {

    var activity: AppCompatActivity = mainActivity ?: nearbyToiletsActivity!!

    fun getAllToilets(): ArrayList<Toilet>{

        Thread{
            getFirebaseToilets()
        }.start()

        return getLocalToilets()
    }

    private fun getLocalToilets(): ArrayList<Toilet>{

        val toiletList = ArrayList<Toilet>()
        val localDb = Room.databaseBuilder(
            activity.applicationContext,
            ToiletDatabase::class.java, "toilet-database"
        ).build()

        val toiletDao = localDb.toiletDao()
        toiletList.addAll(toiletDao.getAll())
        localDb.close()

        return toiletList
    }

    private fun getFirebaseToilets(){

        val toiletList = ArrayList<Toilet>()
        val db = ToiletFirebaseRepository()
        db.allToilets()?.let {
            updateLocalDatabase(it)
            toiletList.addAll(it)
        }
    }

    private fun updateLocalDatabase(updatedToilets: ArrayList<Toilet>){

        for (localToilet in ArrayList(getLocalToilets())){
            updatedToilets.removeAll { it.id == localToilet.id }
        }

        if (updatedToilets.isNotEmpty()){

            Log.d("DATABASEHELPER", "${updatedToilets.size} toilets need to be updated!")

            val localDb = Room.databaseBuilder(
                activity.applicationContext,
                ToiletDatabase::class.java, "toilet-database"
            ).build()

            val toiletDao = localDb.toiletDao()
            toiletDao.insertAll(updatedToilets)
            localDb.close()

            //Update all values
            when (activity) {
                is MainActivity -> {
                    MainActivity.onDatabaseUpdate(mainActivity!!)
                }
                is NearbyToiletsActivity -> {
                    NearbyToiletsActivity.onDatabaseUpdate(nearbyToiletsActivity!!)
                }
            }
        }
    }
}
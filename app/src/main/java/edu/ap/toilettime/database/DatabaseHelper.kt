package edu.ap.toilettime.database

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import edu.ap.toilettime.activities.MainActivity
import edu.ap.toilettime.activities.NearbyToiletsActivity
import edu.ap.toilettime.database.room.ToiletDatabase
import edu.ap.toilettime.model.Toilet

class DatabaseHelper(private val activity: AppCompatActivity) {

    fun getAllToilets(): ArrayList<Toilet>{

        //Retrieve all toilets from firebase async
        Thread{
            getFirebaseToilets()
        }.start()

        //Return all toilets saved locally first
        return getLocalToilets()
    }

    fun addToilet(toilet: Toilet){

        //Add toilet to Firebase
        val db = ToiletFirebaseRepository()
        val id = db.addToilet(toilet)

        //Sync local database with Firebase
        if (id != null){
            toilet.id = id
            updateLocalDatabase(ArrayList(listOf(toilet)))
        }
    }

    fun updateToilet(toilet: Toilet){

        //Update local database
        val localDb = Room.databaseBuilder(
            activity.applicationContext,
            ToiletDatabase::class.java, "toilet-database"
        ).build()

        val toiletDao = localDb.toiletDao()
        toiletDao.update(toilet)
        localDb.close()

        //Reload data on the current activity
        when (activity) {
            is MainActivity -> activity.loadToiletData()
            is NearbyToiletsActivity -> activity.loadToiletData()
        }

        //Update firebase
        val db = ToiletFirebaseRepository()
        db.updateToilet(toilet)
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

        val db = ToiletFirebaseRepository()
        db.allToilets()?.let {
            //Try to update local database
            updateLocalDatabase(it)
        }
    }

    private fun updateLocalDatabase(updatedToilets: ArrayList<Toilet>){

        //Check for differences between local and firebase
        for (localToilet in ArrayList(getLocalToilets())){
            updatedToilets.removeAll { it.id == localToilet.id }
        }

        //Local database needs to be updated
        if (updatedToilets.isNotEmpty()){

            val localDb = Room.databaseBuilder(
                activity.applicationContext,
                ToiletDatabase::class.java, "toilet-database"
            ).build()

            val toiletDao = localDb.toiletDao()
            toiletDao.insertAll(updatedToilets)
            localDb.close()

            //Reload toilet data on the current activity
            when (activity) {
                is MainActivity -> activity.loadToiletData()
                is NearbyToiletsActivity -> activity.loadToiletData()
            }
        }
    }
}
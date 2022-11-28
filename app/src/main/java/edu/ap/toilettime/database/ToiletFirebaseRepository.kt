package edu.ap.toilettime.database

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ap.toilettime.api.APIHelper
import edu.ap.toilettime.model.Toilet
import edu.ap.toilettime.model.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ToiletFirebaseRepository {

    private val firebaseTag = "Firebase"
    private val db = Firebase.firestore

    fun allToilets() : ArrayList<Toilet>? = runBlocking{
        val toiletsArrayList = ArrayList<Toilet>()
        val task = db.collection(COLLECTION_TOILETS).get()
        val document = task.await()

        if (task.isSuccessful){

            for (doc in document.documents) {
                val data = doc.data

                val lat = data?.get(Toilet.LAT) as Double
                val long = data[Toilet.LONG] as Double
                val street = data[Toilet.STREET] as String
                val houseNr = data[Toilet.HOUSE_NR] as String
                val district = data[Toilet.DISTRICT] as String
                val districtCode = data[Toilet.DISTRICT_CODE] as String
                val menAccessible = data[Toilet.MEN_ACCESSIBLE] as Boolean
                val womenAccessible = data[Toilet.WOMEN_ACCESSIBLE] as Boolean
                val wheelchairAccessible = data[Toilet.WHEELCHAIR_ACCESSIBLE] as Boolean
                val changingTable = data[Toilet.CHANGING_TABLE] as Boolean
                val reporterEmailsStrings = data[Toilet.REPORTER_EMAILS] as List<*>

                val reporterEmails = ArrayList<User>()
                for (email in reporterEmailsStrings){
                    reporterEmails.add(User(email as String))
                }

                toiletsArrayList.add(Toilet(doc.id, lat, long, street, houseNr, district, districtCode, menAccessible, womenAccessible, wheelchairAccessible, changingTable, reporterEmails))
            }

            if (document.documents.isEmpty()){

                Log.d(firebaseTag, "Database is empty, retrieving data from API")
                val apiHelper = APIHelper()
                val toilets = apiHelper.getToilets()

                addAllToilets(toilets)

                Log.i(firebaseTag, "Added ${toilets.size} toilets from API")
            }

            toilets = toiletsArrayList

            Log.d(firebaseTag, "Toilets loaded")

            return@runBlocking toiletsArrayList

        }else{

            Log.e(firebaseTag, "Error getting all toilets", task.exception)
            return@runBlocking null
        }

    }

    fun getToilet(id: String) : Toilet? = runBlocking{

        var toilet: Toilet? = null
        val document = db.collection(COLLECTION_TOILETS).document(id).get().await()

        if (document != null) {
            val data = document.data

            if (data != null){

                val lat = data[Toilet.LAT] as Double
                val long = data[Toilet.LONG] as Double
                val street = data[Toilet.STREET] as String
                val houseNr = data[Toilet.HOUSE_NR] as String
                val district = data[Toilet.DISTRICT] as String
                val districtCode = data[Toilet.DISTRICT_CODE] as String
                val menAccessible = data[Toilet.MEN_ACCESSIBLE] as Boolean
                val womenAccessible = data[Toilet.WOMEN_ACCESSIBLE] as Boolean
                val wheelchairAccessible = data[Toilet.WHEELCHAIR_ACCESSIBLE] as Boolean
                val changingTable = data[Toilet.CHANGING_TABLE] as Boolean
                val reporterEmailsStrings = data[Toilet.REPORTER_EMAILS] as List<*>

                val reporterEmails = ArrayList<User>()
                for (email in reporterEmailsStrings){
                    reporterEmails.add(User(email as String))
                }

                toilet = Toilet(document.id, lat, long, street, houseNr, district, districtCode, menAccessible, womenAccessible, wheelchairAccessible, changingTable, reporterEmails)

            }else{
                Log.e(firebaseTag, "No toilet with this id was found")
            }
        }else{
            Log.e(firebaseTag, "There was a problem getting toilet")
        }

        return@runBlocking toilet
    }

    fun addToilet(toilet : Toilet) : Boolean = runBlocking{

        // Create a new toilet hashmap
        val emailsAsString = ArrayList<String>()
        for (user in toilet.reporterEmails){
            emailsAsString.add(user.email)
        }

        val toiletMap = hashMapOf(
            Toilet.LAT to toilet.latitude,
            Toilet.LONG to toilet.longitude,
            Toilet.STREET to toilet.street,
            Toilet.HOUSE_NR to toilet.houseNr,
            Toilet.DISTRICT to toilet.district,
            Toilet.DISTRICT_CODE to toilet.districtCode,
            Toilet.MEN_ACCESSIBLE to toilet.menAccessible,
            Toilet.WOMEN_ACCESSIBLE to toilet.womenAccessible,
            Toilet.WHEELCHAIR_ACCESSIBLE to toilet.wheelchairAccessible,
            Toilet.CHANGING_TABLE to toilet.changingTable,
            Toilet.REPORTER_EMAILS to emailsAsString,
        )

        // Add a new document with a generated ID
        val docRef = db.collection(COLLECTION_TOILETS).add(toiletMap).await()

        if (docRef != null){
            Log.d(firebaseTag, "Toilet added with ID: ${docRef.id}")
        } else {
            Log.w(firebaseTag, "Error adding toilet")
            return@runBlocking false
        }

        return@runBlocking true
    }

    fun addAllToilets(toilets : List<Toilet>) : Boolean = runBlocking{

        val batch = db.batch()

        for (toilet in toilets){
            //Create new document reference
            val docRef: DocumentReference =  db.collection(COLLECTION_TOILETS).document()

            // Create a new toilet hashmap
            val emailsAsString = ArrayList<String>()
            for (user in toilet.reporterEmails){
                emailsAsString.add(user.email)
            }

            val toiletMap = hashMapOf(
                Toilet.LAT to toilet.latitude,
                Toilet.LONG to toilet.longitude,
                Toilet.STREET to toilet.street,
                Toilet.HOUSE_NR to toilet.houseNr,
                Toilet.DISTRICT to toilet.district,
                Toilet.DISTRICT_CODE to toilet.districtCode,
                Toilet.MEN_ACCESSIBLE to toilet.menAccessible,
                Toilet.WOMEN_ACCESSIBLE to toilet.womenAccessible,
                Toilet.WHEELCHAIR_ACCESSIBLE to toilet.wheelchairAccessible,
                Toilet.CHANGING_TABLE to toilet.changingTable,
                Toilet.REPORTER_EMAILS to emailsAsString,
            )

            //Add to batch
            batch.set(docRef, toiletMap)
        }

        val task = batch.commit()
        task.await()

        if (task.isSuccessful){
            Log.d(firebaseTag, "Added multiple toilets")
            return@runBlocking true
        }else{
            Log.w(firebaseTag, "Error adding toilet")
            return@runBlocking false
        }
    }

    fun updateToilet(toilet: Toilet) : Toilet? = runBlocking{

        // Create a new toilet hashmap
        val emailsAsString = ArrayList<String>()
        for (user in toilet.reporterEmails){
            emailsAsString.add(user.email)
        }

        val toiletMap : Map<String, Any> = hashMapOf(

            Toilet.LAT to toilet.latitude,
            Toilet.LONG to toilet.longitude,
            Toilet.STREET to toilet.street!!,
            Toilet.HOUSE_NR to toilet.houseNr!!,
            Toilet.DISTRICT to toilet.district!!,
            Toilet.DISTRICT_CODE to toilet.districtCode!!,
            Toilet.MEN_ACCESSIBLE to toilet.menAccessible,
            Toilet.WOMEN_ACCESSIBLE to toilet.womenAccessible,
            Toilet.WHEELCHAIR_ACCESSIBLE to toilet.wheelchairAccessible,
            Toilet.CHANGING_TABLE to toilet.changingTable,
            Toilet.REPORTER_EMAILS to emailsAsString,
        )

        val task: Task<Void> = db.collection(COLLECTION_TOILETS).document(toilet.id!!).update(toiletMap)
        task.await()

        if (task.isSuccessful){
            Log.d(firebaseTag, "Toilet with id ${toilet.id} successfully updated!")
            return@runBlocking toilet
        }else{
            Log.w(firebaseTag, "Error deleting toilet")
            return@runBlocking null
        }
    }

    fun deleteToilet(id : String) : Boolean = runBlocking{

        val task = db.collection(COLLECTION_TOILETS).document(id).delete()
        task.await()

        if (task.isSuccessful) {
            Log.d(firebaseTag, "Toilet with id $id successfully deleted!")
            return@runBlocking true
        }else {
            Log.w(firebaseTag, "Error deleting toilet")
            return@runBlocking false
        }
    }

    companion object {
        private val COLLECTION_TOILETS = "toilets"

        private var toilets : ArrayList<Toilet>? = null
    }
}
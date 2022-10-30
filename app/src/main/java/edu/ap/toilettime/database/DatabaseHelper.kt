package edu.ap.toilettime.database

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ap.toilettime.model.Toilet
import edu.ap.toilettime.model.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class DatabaseHelper {

    private val firebaseTag = "Firebase"
    private val db = Firebase.firestore

    fun allToilets() : ArrayList<Toilet> = runBlocking{

        val toiletsArrayList = ArrayList<Toilet>()
        val document = db.collection(COLLECTION_TOILETS).get().await()

        if (document != null) {
            for (doc in document.documents) {
                val data = doc.data

                val street = data?.get(STREET) as String
                val district = data[DISTRICT] as String
                val menAccessible = data[MEN_ACCESSIBLE] as Boolean
                val womenAccessible = data[WOMEN_ACCESSIBLE] as Boolean
                val wheelchairAccessible = data[WHEELCHAIR_ACCESSIBLE] as Boolean
                val changingTable = data[CHANGING_TABLE] as Boolean
                val reporterEmailsStrings = data[REPORTER_EMAILS] as List<*>

                val reporterEmails = ArrayList<User>()
                for (email in reporterEmailsStrings){
                    reporterEmails.add(User(email as String))
                }

                toiletsArrayList.add(Toilet(doc.id, street, district, menAccessible, womenAccessible, wheelchairAccessible, changingTable, reporterEmails))
            }
        }

        return@runBlocking toiletsArrayList
    }

    fun getToilet(id: String) : Toilet? = runBlocking{

        var toilet: Toilet? = null
        val document = db.collection(COLLECTION_TOILETS).document(id).get().await()

        if (document != null) {
            val data = document.data

            if (data != null){

                val street = data[STREET] as String
                val district = data[DISTRICT] as String
                val menAccessible = data[MEN_ACCESSIBLE] as Boolean
                val womenAccessible = data[WOMEN_ACCESSIBLE] as Boolean
                val wheelchairAccessible = data[WHEELCHAIR_ACCESSIBLE] as Boolean
                val changingTable = data[CHANGING_TABLE] as Boolean
                val reporterEmailsStrings = data[REPORTER_EMAILS] as List<*>

                val reporterEmails = ArrayList<User>()
                for (email in reporterEmailsStrings){
                    reporterEmails.add(User(email as String))
                }

                toilet = Toilet(document.id, street, district, menAccessible, womenAccessible, wheelchairAccessible, changingTable, reporterEmails)

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
            STREET to toilet.street,
            DISTRICT to toilet.district,
            MEN_ACCESSIBLE to toilet.menAccessible,
            WOMEN_ACCESSIBLE to toilet.womenAccessible,
            WHEELCHAIR_ACCESSIBLE to toilet.wheelchairAccessible,
            CHANGING_TABLE to toilet.changingTable,
            REPORTER_EMAILS to emailsAsString,
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

    fun deleteToilet(id : String){

        db.collection(COLLECTION_TOILETS).document(id).delete().addOnSuccessListener {
            Log.d(firebaseTag, "Toilet with id $id successfully deleted!")
        }.addOnFailureListener { e ->
            Log.w(firebaseTag, "Error deleting toilet", e)
        }
    }

    companion object {
        private val COLLECTION_TOILETS = "toilets"
        private val STREET = "street"
        private val DISTRICT = "district"
        private val MEN_ACCESSIBLE = "men_accessible"
        private val WOMEN_ACCESSIBLE = "women_accessible"
        private val WHEELCHAIR_ACCESSIBLE = "wheelchair_accessible"
        private val CHANGING_TABLE = "changing_accessible"
        private val REPORTER_EMAILS = "reporter_emails"
    }
}
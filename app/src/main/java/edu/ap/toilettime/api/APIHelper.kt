package edu.ap.toilettime.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import edu.ap.toilettime.model.Toilet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

class APIHelper {

    private val apiTag = "API"

    private fun getURLContentAsString(url: URL) : String{

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        val response = call.execute()
        return response.body!!.string()
    }

    fun getToilets() : List<Toilet>{

        val url = URL("https://geodata.antwerpen.be/arcgissql/rest/services/P_Portal/portal_publiek1/MapServer/8/query?where=1%3D1&outFields=*&outSR=4326&f=json")
        var result : String? = null
        val myHandler = Handler(Looper.getMainLooper())

        myHandler.post{
            Log.i(apiTag, "Loading toilets from API")
        }

        //Run blocking
        result = getURLContentAsString(url)
        val toilets: ArrayList<Toilet> = ArrayList()

        val jObject = JSONObject(result)
        val allToiletsJsonArray = jObject.getJSONArray("features")

        for (i in 0 until allToiletsJsonArray.length()){
            val toiletObject = allToiletsJsonArray.getJSONObject(i).getJSONObject("attributes")

            val toilet = Toilet(
                lat = getLat(allToiletsJsonArray.getJSONObject(i).getJSONObject("geometry")),
                long = getLong(allToiletsJsonArray.getJSONObject(i).getJSONObject("geometry")),
                street = toiletObject.getString("STRAAT"),
                houseNr =  toiletObject.getString("HUISNUMMER"),
                district = toiletObject.getString("DISTRICT"),
                districtCode = toiletObject.getString("POSTCODE"),
                menAccessible = toiletObject.getString("DOELGROEP").contains("man") || toiletObject.getString("TYPE").equals("urinoir"),
                womenAccessible = toiletObject.getString("DOELGROEP").contains("vrouw"),
                wheelchairAccessible = toiletObject.getString("INTEGRAAL_TOEGANKELIJK").equals("ja"),
                changingTable = toiletObject.getString("LUIERTAFEL").equals("ja")
            )

            toilets.add(toilet)
        }

        return toilets
        //
    }

    private fun getLat(obj: JSONObject) : Double{

        return try {
            val latString = obj.getJSONObject("y").getString("value")
            latString.toDouble()

        }catch (e: JSONException){
            obj.getDouble("y")
        }
    }

    fun getLong(obj: JSONObject) : Double{

        return try {
            val latString = obj.getJSONObject("x").getString("value")
            latString.toDouble()

        }catch (e: JSONException){
            obj.getDouble("x")
        }
    }

}
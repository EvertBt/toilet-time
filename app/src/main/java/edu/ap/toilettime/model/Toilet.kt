package edu.ap.toilettime.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Toilet  (
    @PrimaryKey var id: String,
    @ColumnInfo(name = "added_by") var addedBy: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "street") var street: String?,
    @ColumnInfo(name = "house_nr") var houseNr: String?,
    @ColumnInfo(name = "district") var district: String?,
    @ColumnInfo(name = "district_code") var districtCode: String?,
    @ColumnInfo(name = "men_accessible") var menAccessible: Boolean,
    @ColumnInfo(name = "women_acessible") var womenAccessible: Boolean,
    @ColumnInfo(name = "wheelchair_accessible") var wheelchairAccessible: Boolean,
    @ColumnInfo(name = "changing_accessible") var changingTable: Boolean,
    @ColumnInfo(name = "reporter_emails") var reporterEmails: ArrayList<User>,
    var distance: Double? = null
){
    companion object {
        var TOILET = "toilet"
        var LOCATION = "location"
        var ID = "id"
        var ADDED_BY = "added_by"
        var STREET = "street"
        var HOUSE_NR = "house_nr"
        var DISTRICT_CODE = "district_code"
        var DISTRICT = "district"
        var MEN_ACCESSIBLE = "men_accessible"
        var WOMEN_ACCESSIBLE = "women_accessible"
        var WHEELCHAIR_ACCESSIBLE = "wheelchair_accessible"
        var CHANGING_TABLE = "changing_accessible"
        var REPORTER_EMAILS = "reporter_emails"
        var LAT = "latitude"
        var LONG = "longitude"
    }
}
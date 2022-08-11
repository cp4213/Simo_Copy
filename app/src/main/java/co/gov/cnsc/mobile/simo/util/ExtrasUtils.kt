package co.gov.cnsc.mobile.simo.util

import android.app.Application
import co.gov.cnsc.mobile.simo.models.Deptos
import co.gov.cnsc.mobile.simo.models.Mncipios
import org.joda.time.format.DateTimeFormat
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ExtrasUtils : Application() {

    fun isConvocatoryClosed(inDate : String) : Boolean ? {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        if(inDate === "null"){
            return true;
        }else{
            var convDate= formatter.parseLocalDate( inDate );
            var nowDate = formatter.parseLocalDate( nowDate().toString() );
            var compare : Boolean  = nowDate.isAfter(convDate);
            return compare;
        }
    }

    fun isConvocatoryOpened(inDate : String) : Boolean ? {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        if(inDate === "null"){
            return true;
        }else{
            var convDate= formatter.parseLocalDate( inDate );
            var nowDate = formatter.parseLocalDate( nowDate().toString() );
            var compare : Boolean  = nowDate.isAfter(convDate);
            return compare;
        }
    }

    fun convertDateFromRNEC(inDate : String) : String? {
        val inputFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        val outputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date = inputFormat.parse(inDate)
        val outputDateStr: String = outputFormat.format(date)
        return outputDateStr;
    }

    /*fun convertDateToSIMO(inDate : String) : String? {
        val inputFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
        val outputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date = inputFormat.parse(inDate)
        val outputDateStr: String = outputFormat.format(date)
        return outputDateStr;
    }*/

    fun convertDateFromSIMO(inDate : String) : String? {
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
        val date: Date = inputFormat.parse(inDate)
        val outputDateStr: String = outputFormat.format(date)
        return outputDateStr;
    }

    private fun nowDate(): String? {
        var dateNow: Date = Date();
        var dateformat = SimpleDateFormat("yyyy-MM-dd");
        return dateformat.format(dateNow);
    }

    fun generateDataDeptos(dataArrayObj: JSONArray) : ArrayList<Deptos> {
        var size = dataArrayObj.length()-1;
        var result = ArrayList<Deptos>()
        //Log.d("Extras", "Size: " + size + " Deptos data array: " + dataArrayObj);
        for (i in 0..size) {
            val objJson    : JSONObject = JSONObject(dataArrayObj[i].toString())                         // Obtengo cada elemento del Array
            var depto: Deptos = Deptos(objJson.get("id").toString(), objJson.get("nombre").toString())   // Obtengo el id y la descripción
            result.add(depto)
        }
        return result
    }

    fun generateDataMncipios(dataArrayObj: JSONArray) : ArrayList<Mncipios> {
        var size = dataArrayObj.length()-1;
        var result = ArrayList<Mncipios>()
        for (i in 0..size) {
            val objJson = JSONObject(dataArrayObj[i].toString())                                                                        // Obtengo cada elemento del Array
            var mncipio = Mncipios(objJson.get("id").toString(), objJson.get("codDane").toString(), objJson.get("nombre").toString())   // Obtengo el id y la descripción
            result.add(mncipio)
        }
        return result
    }

    var result = "";

    fun searchTestsData(dataArrayObj: JSONArray, idPrueba: String) : JSONObject {
        for (i in 0..(dataArrayObj.length()-1)) {
            val objToString= dataArrayObj[i].toString();
            var existTextMatch: Boolean = objToString.contains(idPrueba);
            if(existTextMatch){
                result = objToString
            }
        }
        return JSONObject(result)
    }

    fun isAuthenticated (cookie :  String): Boolean {
        return cookie != "null"
    }
}
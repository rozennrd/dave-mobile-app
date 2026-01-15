import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.dave.ui.screens.PlantDetail
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ApiService {
    private val client = OkHttpClient()
    private val apiKey = "sk-vp1f6960ae43c7a0a14258"

    fun fetchPlantList(onResult: (List<Pair<Int, String>>) -> Unit) {
        val url = "https://perenual.com/api/v2/species-list?key=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val dataArray = json.getJSONArray("data")
                    val plants = mutableListOf<Pair<Int, String>>()

                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        plants.add(item.getInt("id") to item.getString("common_name"))
                    }

                    // CRUCIAL : On renvoie le résultat sur le thread principal pour l'UI
                    Handler(Looper.getMainLooper()).post {
                        onResult(plants)
                    }
                }
            }
        })
    }

    /*fun fetchPlantDetails(plantId: Int, onResult: (PlantDetail) -> Unit) {
        val url = "https://perenual.com/api/v2/species/details/$plantId?key=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)

                    // 1. Extraction du sol (Directement à la racine selon ton JSON)
                    val soilArray = json.optJSONArray("soil")
                    val soilList = soilArray?.let { array ->
                        List(array.length()) { i -> array.getString(i) }
                    } ?: listOf("Not specified")

                    // On crée l'objet PlantDetail avec les infos du JSON
                    val detailedPlant = PlantDetail(
                        id = json.getInt("id"),
                        commonName = json.getString("common_name"),
                        scientificName = listOf(json.getJSONArray("scientific_name").getString(0)),
                        family = json.optString("family", "Unknown"),
                        type = json.optString("type", "Unknown"),
                        imageUrl = json.optJSONObject("default_image")?.optString("original_url"),
                        careLevel = json.optString("care_level"),
                        sunlight = listOf("Full sun"),
                        watering = json.optString("watering"),
                        indoor = json.optBoolean("indoor"),
                        poisonousToHumans = json.optInt("poisonous_to_humans") > 0,
                        poisonousToPets = json.optInt("poisonous_to_pets") > 0,
                        droughtTolerant = json.optBoolean("drought_tolerant"),
                        soil = soilList,
                        surname = "", // Vide au début
                        notes = ""    // Vide au début
                    )

                    Handler(Looper.getMainLooper()).post {
                        onResult(detailedPlant)
                    }
                }
            }
        })
    }*/

    fun fetchPlantDetails(plantId: Int, onResult: (PlantDetail) -> Unit) {
        val url = "https://perenual.com/api/v2/species/details/$plantId?key=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)

                    // SOIL
                    // Extraction manuelle et sécurisée du sol
                    val soilList = mutableListOf<String>()
                    val soilArray = json.optJSONArray("soil")

                    // On vérifie si l'array existe ET s'il n'est pas vide
                    if (soilArray != null && soilArray.length() > 0) {
                        for (i in 0 until soilArray.length()) {
                            soilList.add(soilArray.getString(i))
                        }
                    } else {
                        soilList.add("Not specified")
                    }

                    // SUNLIGHT
                    // 1. Extraction de la liste Sunlight
                    val sunlightArray = json.optJSONArray("sunlight")
                    val sunlightList = mutableListOf<String>()

                    if (sunlightArray != null && sunlightArray.length() > 0) {
                        for (i in 0 until sunlightArray.length()) {
                            sunlightList.add(sunlightArray.getString(i))
                        }
                    } else {
                        // Si l'API ne renvoie rien, on met une valeur par défaut
                        sunlightList.add("part shade")
                    }

                    val detailedPlant = PlantDetail(
                        id = json.getInt("id"),
                        commonName = json.optString("common_name", "Unknown"),
                        scientificName = listOf(json.getJSONArray("scientific_name").optString(0, "Unknown")),
                        family = json.optString("family", "Unknown"),
                        type = json.optString("type", "Unknown"),
                        imageUrl = json.optJSONObject("default_image")?.optString("original_url"),
                        careLevel = json.optString("care_level", "Unknown"),
                        sunlight = sunlightList,
                        watering = json.optString("watering", "Unknown"),
                        indoor = json.optBoolean("indoor", false),
                        poisonousToHumans = json.optBoolean("poisonous_to_humans", false),
                        poisonousToPets = json.optBoolean("poisonous_to_pets", false),
                        droughtTolerant = json.optBoolean("drought_tolerant", false),
                        soil = soilList,
                        surname = "",
                        notes = ""
                    )

                    Handler(Looper.getMainLooper()).post {
                        onResult(detailedPlant)
                    }
                }
            }
        })
    }
}
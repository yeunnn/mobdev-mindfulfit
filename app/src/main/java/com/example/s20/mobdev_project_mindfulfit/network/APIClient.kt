package com.example.s20.mobdev_project_mindfulfit.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class APIClient {

    private val client = OkHttpClient()
    private val apiUrl = "https://zenquotes.io/api/random"
    private val jokeApiUrl = "https://v2.jokeapi.dev/joke/Any?type=single"

    suspend fun fetchDailyQuote(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apiUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrEmpty()) {
                        // Parse the response to extract quote and author
                        val jsonArray = JSONArray(responseBody)
                        val quoteObject = jsonArray.getJSONObject(0)
                        val quote = quoteObject.getString("q")
                        val author = quoteObject.getString("a")
                        return@withContext "\"$quote\" - $author"
                    }
                }
                Log.e("APIClient", "Error fetching quote: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("APIClient", "Exception fetching quote", e)
        }
        return@withContext null
    }

    suspend fun fetchJoke(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(jokeApiUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        return@withContext jsonObject.getString("joke")
                    }
                }
                Log.e("APIClient", "Error fetching joke: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("APIClient", "Exception fetching joke", e)
        }
        return@withContext null
    }
}

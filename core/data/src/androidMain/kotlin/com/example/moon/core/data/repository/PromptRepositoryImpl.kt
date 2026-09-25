package com.example.moon.core.data.repository

import android.content.Context
import com.example.moon.core.domain.model.IfsPrompt
import com.example.moon.core.domain.model.IfsPromptProvider
import com.example.moon.core.domain.repository.PromptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class PromptRepositoryImpl(context: Context) : PromptRepository {
    private val prefs = context.getSharedPreferences("moon_prompts_cache", Context.MODE_PRIVATE)

    override fun loadCachedPrompts() {
        val cachedJson = prefs.getString("cached_prompts_json", null) ?: return
        try {
            val prompts = parsePromptsJson(cachedJson)
            if (prompts.isNotEmpty()) {
                IfsPromptProvider.updatePrompts(prompts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun checkAndUpdatePrompts(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://moon-is.web.app/prompts.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val prompts = parsePromptsJson(jsonText)
                if (prompts.isNotEmpty()) {
                    prefs.edit().putString("cached_prompts_json", jsonText).apply()
                    IfsPromptProvider.updatePrompts(prompts)
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    private fun parsePromptsJson(jsonStr: String): List<IfsPrompt> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<IfsPrompt>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val title = obj.optString("title", "")
            val prompt = obj.optString("prompt", "")
            if (title.isNotEmpty() || prompt.isNotEmpty()) {
                list.add(IfsPrompt(title, prompt))
            }
        }
        return list
    }
}

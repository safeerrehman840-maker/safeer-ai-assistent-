package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedAssistantResponse(
    val replyText: String,
    val identifiedAction: AssistantAction? = null,
    val language: String = "Roman Urdu"
)

sealed class AssistantAction {
    data class SendWhatsApp(val contactName: String?, val message: String) : AssistantAction()
    data class DraftEmail(val recipient: String?, val subject: String?, val body: String) : AssistantAction()
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : AssistantAction()
    data class SetReminder(val title: String, val delayMinutes: Int) : AssistantAction()
    data class CreateGoal(val title: String, val category: String) : AssistantAction()
    data class SearchYouTube(val query: String) : AssistantAction()
    data class SearchWeb(val query: String) : AssistantAction()
    data class OpenApp(val appTarget: String) : AssistantAction()
}

class GeminiAssistantClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    suspend fun getAssistantResponse(
        userPrompt: String,
        recentHistory: List<Pair<String, String>> = emptyList()
    ): ParsedAssistantResponse = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext fallbackLocalNlp(userPrompt)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = """
                You are Safeer (سفیر), a friendly, highly intelligent personal AI assistant on Android for users who speak Urdu, Roman Urdu, and English.
                Respond naturally in the same language or dialect the user uses (Urdu script, Roman Urdu, or English).
                Keep responses concise, helpful, and courteous (use respectful terms like 'Jee', 'Zaroor', 'Aapka hukam').
                
                You can help users control their phone actions:
                1. WhatsApp message: [ACTION:WHATSAPP:contact_name:message_text]
                2. Email draft: [ACTION:EMAIL:recipient:subject:body]
                3. Set Alarm: [ACTION:ALARM:hour(0-23):minute(0-59):label]
                4. Set Reminder: [ACTION:REMINDER:minutes_from_now:title]
                5. Create Daily Goal: [ACTION:GOAL:category:title]
                6. YouTube search: [ACTION:YOUTUBE:query]
                7. Google / Chrome search: [ACTION:WEB:query]
                8. Open App: [ACTION:OPEN_APP:whatsapp|youtube|chrome|gmail|settings]
                
                If an action is detected, append the action tag at the very end of your answer on a new line. Never fabricate confirmations; explain that Safeer will prepare the action for user review.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Include system instruction
            val systemObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            }

            // Append last few turns of conversation
            for ((role, text) in recentHistory.takeLast(6)) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (role == "assistant") "model" else "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            // Append current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userPrompt) })
                })
            })

            val jsonBody = JSONObject().apply {
                put("systemInstruction", systemObj)
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext fallbackLocalNlp(userPrompt)
            }

            val rawJson = response.body?.string() ?: ""
            val jsonRoot = JSONObject(rawJson)
            val candidates = jsonRoot.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseAssistantOutput(rawText)
        } catch (e: Exception) {
            fallbackLocalNlp(userPrompt)
        }
    }

    private fun parseAssistantOutput(text: String): ParsedAssistantResponse {
        var cleanText = text.trim()
        var action: AssistantAction? = null

        val actionRegex = Regex("\\[ACTION:([A-Z_]+):(.*)\\]", RegexOption.IGNORE_CASE)
        val match = actionRegex.find(cleanText)

        if (match != null) {
            val type = match.groupValues[1].uppercase()
            val payload = match.groupValues[2]
            cleanText = cleanText.replace(match.value, "").trim()

            action = when (type) {
                "WHATSAPP" -> {
                    val parts = payload.split(":", limit = 2)
                    AssistantAction.SendWhatsApp(
                        contactName = parts.getOrNull(0)?.trim()?.ifEmpty { null },
                        message = parts.getOrNull(1)?.trim() ?: ""
                    )
                }
                "EMAIL" -> {
                    val parts = payload.split(":", limit = 3)
                    AssistantAction.DraftEmail(
                        recipient = parts.getOrNull(0)?.trim()?.ifEmpty { null },
                        subject = parts.getOrNull(1)?.trim()?.ifEmpty { null },
                        body = parts.getOrNull(2)?.trim() ?: ""
                    )
                }
                "ALARM" -> {
                    val parts = payload.split(":", limit = 3)
                    val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 7
                    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                    val label = parts.getOrNull(2)?.trim() ?: "Safeer Alarm"
                    AssistantAction.SetAlarm(h, m, label)
                }
                "REMINDER" -> {
                    val parts = payload.split(":", limit = 2)
                    val mins = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 15
                    val title = parts.getOrNull(1)?.trim() ?: "Reminder"
                    AssistantAction.SetReminder(title, mins)
                }
                "GOAL" -> {
                    val parts = payload.split(":", limit = 2)
                    val cat = parts.getOrNull(0)?.trim() ?: "General"
                    val title = parts.getOrNull(1)?.trim() ?: payload
                    AssistantAction.CreateGoal(title, cat)
                }
                "YOUTUBE" -> AssistantAction.SearchYouTube(payload.trim())
                "WEB" -> AssistantAction.SearchWeb(payload.trim())
                "OPEN_APP" -> AssistantAction.OpenApp(payload.trim())
                else -> null
            }
        }

        return ParsedAssistantResponse(
            replyText = cleanText.ifEmpty { "Jee, main aapki madad ke liye tayyar hoon." },
            identifiedAction = action
        )
    }

    /**
     * Local offline rule-based NLP fallback when offline or no API key is set
     */
    fun fallbackLocalNlp(input: String): ParsedAssistantResponse {
        val lower = input.lowercase()

        // 1. WhatsApp
        if (lower.contains("whatsapp") || lower.contains("message") || lower.contains("paigham")) {
            val contact = if (lower.contains("ali")) "Ali"
            else if (lower.contains("ahmed")) "Ahmed"
            else if (lower.contains("bhai")) "Bhai"
            else null

            return ParsedAssistantResponse(
                replyText = "Jee bilkul! WhatsApp message tayyar kar diya hai. Aap review karke send kar sakte hain.",
                identifiedAction = AssistantAction.SendWhatsApp(contact, input)
            )
        }

        // 2. Alarm
        if (lower.contains("alarm") || lower.contains("baje") || lower.contains("subah") || lower.contains("wake")) {
            val hour = if (lower.contains("6")) 6 else if (lower.contains("7")) 7 else if (lower.contains("8")) 8 else 7
            return ParsedAssistantResponse(
                replyText = "Jee, aapka alarm $hour:00 baje ke liye schedule kar diya gaya hai.",
                identifiedAction = AssistantAction.SetAlarm(hour, 0, "Morning Alarm")
            )
        }

        // 3. YouTube
        if (lower.contains("youtube") || lower.contains("video") || lower.contains("tutorial")) {
            val query = input.replace(Regex("(?i)(youtube|par|chalao|search|karo|video)"), "").trim()
            return ParsedAssistantResponse(
                replyText = "YouTube par search khola ja raha hai: ${if (query.isNotEmpty()) query else "Python tutorial"}",
                identifiedAction = AssistantAction.SearchYouTube(if (query.isNotEmpty()) query else "Python tutorial")
            )
        }

        // 4. Chrome / Web
        if (lower.contains("chrome") || lower.contains("google") || lower.contains("browser") || lower.contains("weather") || lower.contains("kholo")) {
            val query = input.replace(Regex("(?i)(chrome|mein|google|par|kholo|search|karo)"), "").trim()
            return ParsedAssistantResponse(
                replyText = "Chrome browser mein search khol raha hoon: $query",
                identifiedAction = AssistantAction.SearchWeb(if (query.isNotEmpty()) query else "Lahore weather")
            )
        }

        // 5. Goals / Plan
        if (lower.contains("goal") || lower.contains("plan") || lower.contains("maqsad") || lower.contains("english practice")) {
            return ParsedAssistantResponse(
                replyText = "Boht khoob! Aapka daily goal add kar diya gaya hai. Daily goals section mein progress check kar sakte hain.",
                identifiedAction = AssistantAction.CreateGoal(input, "English Practice")
            )
        }

        // 6. Reminder
        if (lower.contains("yaad") || lower.contains("remind") || lower.contains("paani") || lower.contains("break")) {
            return ParsedAssistantResponse(
                replyText = "Jee, aapka reminder set kar diya gaya hai. Safeer aapko waqt par aagah kar dega.",
                identifiedAction = AssistantAction.SetReminder(input, 30)
            )
        }

        // Default conversational reply
        return ParsedAssistantResponse(
            replyText = "Assalam-o-Alaikum! Main Safeer AI Assistant hoon. Main aapke WhatsApp messages, alarms, reminders, goals, YouTube aur web searches mein madad kar sakta hoon. Aap mujhse Urdu, Roman Urdu, ya English mein baat kar sakte hain."
        )
    }
}

package com.example

import com.example.ai.AssistantAction
import com.example.ai.GeminiAssistantClient
import org.junit.Assert.*
import org.junit.Test

class SafeerAssistantTest {

    private val geminiClient = GeminiAssistantClient()

    @Test
    fun testFallbackNlpWhatsAppParsing() {
        val result = geminiClient.fallbackLocalNlp("Ali ko WhatsApp par message likho")
        assertNotNull(result.identifiedAction)
        assertTrue(result.identifiedAction is AssistantAction.SendWhatsApp)
        val action = result.identifiedAction as AssistantAction.SendWhatsApp
        assertEquals("Ali", action.contactName)
    }

    @Test
    fun testFallbackNlpAlarmParsing() {
        val result = geminiClient.fallbackLocalNlp("Kal subah 7 baje alarm lagao")
        assertNotNull(result.identifiedAction)
        assertTrue(result.identifiedAction is AssistantAction.SetAlarm)
        val action = result.identifiedAction as AssistantAction.SetAlarm
        assertEquals(7, action.hour)
    }

    @Test
    fun testFallbackNlpYouTubeParsing() {
        val result = geminiClient.fallbackLocalNlp("YouTube par Python tutorial chalao")
        assertNotNull(result.identifiedAction)
        assertTrue(result.identifiedAction is AssistantAction.SearchYouTube)
    }

    @Test
    fun testFallbackNlpWebParsing() {
        val result = geminiClient.fallbackLocalNlp("Chrome mein Lahore weather search karo")
        assertNotNull(result.identifiedAction)
        assertTrue(result.identifiedAction is AssistantAction.SearchWeb)
    }

    @Test
    fun testFallbackNlpGoalParsing() {
        val result = geminiClient.fallbackLocalNlp("Mera aaj ka goal English practice hai")
        assertNotNull(result.identifiedAction)
        assertTrue(result.identifiedAction is AssistantAction.CreateGoal)
    }
}

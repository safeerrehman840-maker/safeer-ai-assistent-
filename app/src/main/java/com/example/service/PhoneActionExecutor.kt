package com.example.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import java.net.URLEncoder

object PhoneActionExecutor {

    fun openWhatsApp(context: Context, contactNumberOrName: String? = null, message: String? = null): Boolean {
        try {
            if (!contactNumberOrName.isNullOrBlank() && contactNumberOrName.matches(Regex("^[+0-9]+$"))) {
                val cleanNumber = contactNumberOrName.replace("+", "").replace(" ", "")
                val encodedMsg = if (!message.isNullOrBlank()) URLEncoder.encode(message, "UTF-8") else ""
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.whatsapp")
                }
                context.startActivity(intent)
                return true
            }

            // General WhatsApp share intent
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                if (!message.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, message)
                }
            }
            context.startActivity(sendIntent)
            return true
        } catch (e: ActivityNotFoundException) {
            // WhatsApp not installed, open Play Store or web link
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp"))
                context.startActivity(marketIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_LONG).show()
            }
            return false
        }
    }

    fun openEmailComposer(
        context: Context,
        recipient: String? = null,
        subject: String? = null,
        body: String? = null
    ): Boolean {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                if (!recipient.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                }
                if (!subject.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                if (!body.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, body)
                }
            }
            context.startActivity(Intent.createChooser(intent, "Choose Email App"))
            return true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found on your phone.", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun openYouTube(context: Context, searchQueryOrUrl: String): Boolean {
        try {
            if (searchQueryOrUrl.startsWith("http://") || searchQueryOrUrl.startsWith("https://")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchQueryOrUrl)).apply {
                    setPackage("com.google.android.youtube")
                }
                context.startActivity(intent)
                return true
            }

            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", searchQueryOrUrl)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            // Fallback to web browser YouTube search
            val webUrl = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(searchQueryOrUrl, "UTF-8")
            openWebBrowser(context, webUrl)
            return true
        }
    }

    fun openWebBrowser(context: Context, queryOrUrl: String): Boolean {
        val targetUri = if (queryOrUrl.startsWith("http://") || queryOrUrl.startsWith("https://")) {
            Uri.parse(queryOrUrl)
        } else {
            Uri.parse("https://www.google.com/search?q=" + URLEncoder.encode(queryOrUrl, "UTF-8"))
        }

        try {
            val chromeIntent = Intent(Intent.ACTION_VIEW, targetUri).apply {
                setPackage("com.android.chrome")
            }
            context.startActivity(chromeIntent)
            return true
        } catch (e: ActivityNotFoundException) {
            // Fallback to any installed browser
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, targetUri)
                context.startActivity(genericIntent)
                return true
            } catch (ex: Exception) {
                Toast.makeText(context, "No browser found.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
    }

    fun setSystemAlarm(context: Context, hour: Int, minute: Int, message: String): Boolean {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "System Alarm Clock app not available.", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun openPhoneSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}

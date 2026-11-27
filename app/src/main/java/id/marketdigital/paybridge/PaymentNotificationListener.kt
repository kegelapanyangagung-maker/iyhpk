package id.marketdigital.paybridge

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.os.Bundle
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.regex.Pattern

class PaymentNotificationListener : NotificationListenerService() {
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("bridge", MODE_PRIVATE) }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.gojek.gopaymerchant") return
        val extras: Bundle = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val body = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()).orEmpty().trim()
        if (!title.contains("pembayaran", true) || !title.contains("diterima", true)) return
        val amount = parseAmount(body) ?: return
        val eventId = sha256("${sbn.packageName}|${sbn.id}|${sbn.tag}|${sbn.postTime}|$title|$body")
        val key = "sent_$eventId"
        if (prefs.getBoolean(key, false)) return
        val endpoint = prefs.getString("url", "")?.trim().orEmpty()
        val secret = prefs.getString("secret", "").orEmpty()
        if (!endpoint.startsWith("https://") || secret.isBlank()) return
        val payload = JSONObject().apply {
            put("status", "paid")
            put("provider", "GoPay Merchant")
            put("package_name", sbn.packageName)
            put("title", title)
            put("body", body)
            put("amount", amount)
            put("event_id", eventId)
            put("notification_id", sbn.id)
            put("posted_at", sbn.postTime)
        }
        executor.execute {
            try {
                val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"; connectTimeout = 10000; readTimeout = 15000
                    doOutput = true; setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("x-webhook-secret", secret)
                }
                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                if (code in 200..299) prefs.edit().putBoolean(key, true).apply()
                conn.disconnect()
            } catch (_: Exception) { /* event can be retried if Android posts it again */ }
        }
    }
    private fun parseAmount(text: String): Long? {
        val m = Pattern.compile("(?i)Rp\\s*([0-9][0-9.,]*)").matcher(text)
        if (!m.find()) return null
        val digits = m.group(1).replace(".", "").replace(",", "")
        return digits.toLongOrNull()
    }
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}

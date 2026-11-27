package id.marketdigital.paybridge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("bridge", MODE_PRIVATE)
    }

    private lateinit var url: EditText
    private lateinit var secret: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 42, 36, 36)
            gravity = Gravity.TOP
        }

        fun label(t: String) {
            root.addView(
                TextView(this).apply {
                    text = t
                    textSize = 15f
                    setPadding(0, 18, 0, 8)
                }
            )
        }

        root.addView(
            TextView(this).apply {
                text = "Market Digital Pay Bridge"
                textSize = 24f
                setPadding(0, 0, 0, 8)
            }
        )

        root.addView(
            TextView(this).apply {
                text = "Meneruskan notifikasi pembayaran GoPay Merchant ke webhook bot."
                textSize = 14f
            }
        )

        label("Webhook URL")

        url = EditText(this).apply {
            hint = "https://webhook.marketdigital.web.id/webhook/payment"

            setText(
                prefs.getString(
                    "url",
                    "https://webhook.marketdigital.web.id/webhook/payment"
                )
            )

            // Perbaikan dari TYPE_CLASS_URI
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_URI
        }

        root.addView(
            url,
            ViewGroup.LayoutParams(-1, -2)
        )

        label("Webhook Secret (harus sama dengan config.js bot)")

        secret = EditText(this).apply {
            hint = "Masukkan secret webhook"

            setText(
                prefs.getString(
                    "secret",
                    ""
                )
            )

            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        root.addView(
            secret,
            ViewGroup.LayoutParams(-1, -2)
        )

        val save = Button(this).apply {
            text = "Simpan konfigurasi"

            setOnClickListener {
                saveConfig()
                status.text = "Konfigurasi tersimpan"
            }
        }

        root.addView(save)

        root.addView(
            Button(this).apply {
                text = "Aktifkan akses notifikasi"

                setOnClickListener {
                    saveConfig()

                    startActivity(
                        Intent(
                            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                        )
                    )
                }
            }
        )

        status = TextView(this).apply {
            text = "Status: siap dikonfigurasi"
            textSize = 14f
            setPadding(0, 20, 0, 0)
        }

        root.addView(status)

        root.addView(
            TextView(this).apply {
                text = "Catatan: hanya notifikasi dari com.gojek.gopaymerchant dengan judul pembayaran diterima yang diteruskan. Order harus memiliki nominal unik. Jangan bagikan secret webhook."
                textSize = 12f
                setPadding(0, 22, 0, 0)
            }
        )

        setContentView(root)
    }

    private fun saveConfig() {
        prefs.edit()
            .putString(
                "url",
                url.text.toString().trim()
            )
            .putString(
                "secret",
                secret.text.toString()
            )
            .apply()
    }
}

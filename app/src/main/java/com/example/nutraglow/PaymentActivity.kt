package com.example.nutraglow

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PaymentActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var paymentSummary: TextView
    private lateinit var totalPayAmount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cancelPaymentButton: Button

    private var totalAmount: Double = 0.0
    private var totalItems: Int = 0

    private val publicKey = "1a1f01a8d3ef430dba6f37bd87b7eff9"
    private val secretKey = "3a747e4f0d264bd78621da19e8925338"
    private val returnUrl = "https://example.com/payment/return/"
    private val baseUrl = "https://a.khalti.com/api/v2/epayment/init/"
    private val verifyUrl = "https://a.khalti.com/api/v2/epayment/lookup/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        webView = findViewById(R.id.khaltiWebView)
        paymentSummary = findViewById(R.id.paymentSummary)
        totalPayAmount = findViewById(R.id.totalPayAmount)
        progressBar = findViewById(R.id.progressBar)
        cancelPaymentButton = findViewById(R.id.cancelPaymentButton)

        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        totalItems = intent.getIntExtra("TOTAL_ITEMS", 0)

        paymentSummary.text = "You are paying for $totalItems item(s)"
        totalPayAmount.text = "Total Amount: Rs. $totalAmount"

        cancelPaymentButton.setOnClickListener {
            Toast.makeText(this, "Payment cancelled.", Toast.LENGTH_SHORT).show()
            finish()
        }

        initiateKhaltiPayment()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(pidx: String) {
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                if (url?.startsWith(returnUrl) == true) {
                    Toast.makeText(this@PaymentActivity, "Payment Completed. Verifying...", Toast.LENGTH_SHORT).show()
                    verifyPayment(pidx)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Toast.makeText(this@PaymentActivity, "Error loading payment page.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@PaymentActivity, PaymentFailedActivity::class.java))
                finish()
            }
        }
        webView.loadUrl("https://a.khalti.com/#/checkout/$pidx")
    }

    private fun initiateKhaltiPayment() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("return_url", returnUrl)
                    put("website_url", "https://example.com")
                    put("amount", (totalAmount * 100).toLong())
                    put("purchase_order_id", System.currentTimeMillis().toString())
                    put("purchase_order_name", "NutraGlow Items")
                    put("customer_info", JSONObject().apply {
                        put("name", "Test User")
                        put("email", "test@example.com")
                        put("phone", "9800000001")
                    })
                }

                val url = URL(baseUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Key $publicKey")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val pidx = jsonResponse.getString("pidx")

                withContext(Dispatchers.Main) {
                    loadWebView(pidx)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@PaymentActivity, "Payment initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@PaymentActivity, PaymentFailedActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun verifyPayment(pidx: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("pidx", pidx)
                }

                val url = URL(verifyUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Key $secretKey")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                val status = jsonResponse.getString("status")
                val amountPaid = jsonResponse.getLong("total_amount")

                withContext(Dispatchers.Main) {
                    if (status == "Completed") {
                        sendPaymentToFirebase(pidx, amountPaid)
                        clearCartFromFirebase()
                        startActivity(Intent(this@PaymentActivity, ThankYouActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this@PaymentActivity, PaymentFailedActivity::class.java))
                        finish()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PaymentActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@PaymentActivity, PaymentFailedActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun sendPaymentToFirebase(pidx: String, amount: Long) {
        val ref = FirebaseDatabase.getInstance().getReference("payments")
        val paymentMap = mapOf(
            "pidx" to pidx,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Completed",
            "user" to (FirebaseAuth.getInstance().currentUser?.email ?: "guest")
        )
        ref.push().setValue(paymentMap)
    }

    private fun clearCartFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("cart")
        ref.removeValue()
    }
}

package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.nutraglow.R

class ThankYouActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

        val trackButton: Button = findViewById(R.id.trackOrderButton)
        trackButton.setOnClickListener {
            startActivity(Intent(this, OrderTrackingActivity::class.java))
        }
    }
}

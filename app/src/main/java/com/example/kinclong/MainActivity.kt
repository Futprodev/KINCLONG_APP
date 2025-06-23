package com.example.kinclong

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseReference: DatabaseReference = firebaseDatabase.getReference("IoT/")

    private var isRunning = false
    private var isPaused = false
    private var selectedMode: String? = null
    private var secondsElapsed = 0

    private lateinit var handler: Handler
    private lateinit var timerRunnable: Runnable
    private lateinit var temperatureRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timerView = findViewById<TextView>(R.id.timerView)
        val modeStatus = findViewById<TextView>(R.id.modeStatus)
        val temperatureText = findViewById<TextView>(R.id.text_temperature)
        val btnStartPause = findViewById<Button>(R.id.btnStartPause)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnModeA = findViewById<Button>(R.id.btnModeA)
        val btnModeB = findViewById<Button>(R.id.btnModeB)
        val btnModeC = findViewById<Button>(R.id.btnModeC)

        handler = Handler(Looper.getMainLooper())

        // Timer tiap detik
        timerRunnable = object : Runnable {
            override fun run() {
                secondsElapsed++
                timerView.text = formatTime(secondsElapsed)
                firebaseReference.child("timer").setValue(secondsElapsed)
                handler.postDelayed(this, 1000)
            }
        }

        // Get temperature from Firebase
        temperatureRunnable = object : Runnable {
            override fun run() {
                if (isRunning && !isPaused) {
                    firebaseReference.child("temp").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val temp = snapshot.getValue(Int::class.java)
                            temperatureText.text = "Temperature: $temp °C"
                        }

                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                            // Handle error
                            Toast.makeText(this@MainActivity, "Failed to read temperature: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                handler.postDelayed(this, 5000)
            }
        }

        handler.post(temperatureRunnable)

        btnStartPause.setOnClickListener {
            if (selectedMode == null) {
                Toast.makeText(this, "Pilih mode terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when {
                !isRunning -> {
                    isRunning = true
                    isPaused = false
                    handler.post(timerRunnable)
                    btnStartPause.text = "Pause"
                    firebaseReference.child("status").setValue("running")
                }
                isPaused -> {
                    isPaused = false
                    handler.post(timerRunnable)
                    btnStartPause.text = "Pause"
                    firebaseReference.child("status").setValue("running")
                }
                else -> {
                    isPaused = true
                    handler.removeCallbacks(timerRunnable)
                    btnStartPause.text = "Resume"
                    firebaseReference.child("status").setValue("paused")
                }
            }
        }

        btnStop.setOnClickListener {
            isRunning = false
            isPaused = false
            handler.removeCallbacks(timerRunnable)
            secondsElapsed = 0
            timerView.text = "00:00"
            btnStartPause.text = "Start"
            firebaseReference.child("status").setValue("stopped")

            handler.removeCallbacks(temperatureRunnable)
            temperatureText.text = "Temperature: -- °C"
        }

        btnModeA.setOnClickListener {
            selectedMode = "Smart"
            firebaseReference.child("mode").setValue("Smart")
            modeStatus.text = "Selected Mode: Smart"
            resetTimer(timerView, btnStartPause, temperatureText)
            handler.post(temperatureRunnable)
        }

        btnModeB.setOnClickListener {
            selectedMode = "Snake"
            firebaseReference.child("mode").setValue("Snake")
            modeStatus.text = "Selected Mode: Snake"
            resetTimer(timerView, btnStartPause, temperatureText)
            handler.post(temperatureRunnable)
        }

        btnModeC.setOnClickListener {
            selectedMode = "Random"
            firebaseReference.child("mode").setValue("Random")
            modeStatus.text = "Selected Mode: Random"
            resetTimer(timerView, btnStartPause, temperatureText)
            handler.post(temperatureRunnable)
        }
    }

    private fun resetTimer(timerView: TextView, btnStartPause: Button, temperatureText: TextView) {
        isRunning = false
        isPaused = false
        handler.removeCallbacks(timerRunnable)
        secondsElapsed = 0
        timerView.text = "00:00"
        btnStartPause.text = "Start"
        firebaseReference.child("status").setValue("stopped")
        temperatureText.text = "Temperature: -- °C"
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}



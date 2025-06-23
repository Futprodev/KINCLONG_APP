package com.example.kinclong

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kinclong.R

class MainActivity : AppCompatActivity() {

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
                handler.postDelayed(this, 1000)
            }
        }

        // Simulasi suhu tiap 5 detik
        temperatureRunnable = object : Runnable {
            override fun run() {
                if (isRunning && !isPaused) {
                    val temp = (25..35).random()
                    temperatureText.text = "Temperature: $temp °C"
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
                }
                isPaused -> {
                    isPaused = false
                    handler.post(timerRunnable)
                    btnStartPause.text = "Pause"
                }
                else -> {
                    isPaused = true
                    handler.removeCallbacks(timerRunnable)
                    btnStartPause.text = "Resume"
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

            handler.removeCallbacks(temperatureRunnable)
            temperatureText.text = "Temperature: -- °C"
        }

        btnModeA.setOnClickListener {
            selectedMode = "Smart"
            modeStatus.text = "Selected Mode: Smart"
            resetTimer(timerView, btnStartPause, temperatureText)
            handler.post(temperatureRunnable)
        }

        btnModeB.setOnClickListener {
            selectedMode = "Snake"
            modeStatus.text = "Selected Mode: Snake"
            resetTimer(timerView, btnStartPause, temperatureText)
            handler.post(temperatureRunnable)
        }

        btnModeC.setOnClickListener {
            selectedMode = "Random"
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
        temperatureText.text = "Temperature: -- °C"
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}



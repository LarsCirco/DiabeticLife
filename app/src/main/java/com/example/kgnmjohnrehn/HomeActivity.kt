package com.example.kgnmjohnrehn

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kgnmjohnrehn.recycler.LogAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class HomeActivity : AppCompatActivity() {


    private lateinit var currentBG: EditText
    private lateinit var targetBG: EditText
    private lateinit var carbs: EditText
    private lateinit var carbRatio: EditText
    private lateinit var correction: EditText
    private lateinit var calculateButton: Button
    private lateinit var viewLogsButton: Button
    private lateinit var resultText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var deleteAllButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        currentBG = findViewById(R.id.currentBG)
        targetBG = findViewById(R.id.targetBG)
        carbs = findViewById(R.id.carbs)
        carbRatio = findViewById(R.id.CarbRatio)
        correction = findViewById(R.id.Correction)
        calculateButton = findViewById(R.id.calculateButton)
        viewLogsButton = findViewById(R.id.viewLogsButton)
        resultText = findViewById(R.id.resultText)
        recyclerView = findViewById(R.id.recyclerView)
        deleteAllButton = findViewById(R.id.deleteAllButton)


        calculateButton.setOnClickListener {
            val totalInsulin: Float
            try {
                val currentBGValue = currentBG.text.toString().toFloat()
                val targetBGValue = targetBG.text.toString().toFloat()
                val carbsValue = carbs.text.toString().toFloat()
                val carbRatioValue = carbRatio.text.toString().toFloat()
                val correctionValue = correction.text.toString().toFloat()

                if (currentBGValue <= 0 || targetBGValue <= 0 || carbsValue <= 0 || carbRatioValue <= 0 || correctionValue <= 0) {
                    resultText.text = getString(R.string.enter_valid_numbers)
                    return@setOnClickListener
                }

                val timestamp = System.currentTimeMillis()
                val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

                val insulinForCarbs = carbsValue / carbRatioValue
                val insulinForCorrection = (currentBGValue - targetBGValue) / correctionValue
                totalInsulin = insulinForCarbs + insulinForCorrection

                val logEntry = "$formattedDate - =Total Insulin:${"%.2f".format(totalInsulin)}   =Carbs: ${"%.2f".format(insulinForCarbs)}  =Correction:  ${"%.2f".format(insulinForCorrection)}"

                val resultStringBuilder = StringBuilder()
                resultStringBuilder.append("Insulin for Carbs: ")
                resultStringBuilder.append("%.2f".format(insulinForCarbs))
                resultStringBuilder.append("\n")
                resultStringBuilder.append("Insulin for Correction: ")
                resultStringBuilder.append("%.2f".format(insulinForCorrection))
                resultStringBuilder.append("\n")
                resultStringBuilder.append("Total Insulin: ")
                resultStringBuilder.append("%.2f".format(totalInsulin))
                resultText.text = resultStringBuilder.toString()



                saveLog(logEntry)
                currentBG.text.clear()
                targetBG.text.clear()
                carbs.text.clear()
                carbRatio.text.clear()
                correction.text.clear()


            } catch (e: NumberFormatException) {
                resultText.text = getString(R.string.invalid_input)
            }
        }




        viewLogsButton.setOnClickListener {
            if (viewLogsButton.text == getString(R.string.view)) {
                toggleViewMode(true)
                loadLogs()
            } else {
                toggleViewMode(false)
            }
        }
        deleteAllButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null)
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            val dialog = builder.create()
            dialog.show()

            val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

            confirmButton.setOnClickListener {
                clearAllLogs()
                loadLogs()
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }




    }
    private fun toggleViewMode(showLogs: Boolean) {
        if (showLogs) {
            viewLogsButton.text = getString(R.string.home)
            currentBG.visibility = View.GONE
            targetBG.visibility = View.GONE
            carbs.visibility = View.GONE
            carbRatio.visibility = View.GONE
            correction.visibility = View.GONE
            calculateButton.visibility = View.GONE
            resultText.visibility=View.GONE
            recyclerView.visibility = View.VISIBLE
            deleteAllButton.visibility=View.VISIBLE
        } else {
            viewLogsButton.text = getString(R.string.view)
            currentBG.visibility = View.VISIBLE
            targetBG.visibility = View.VISIBLE
            carbs.visibility = View.VISIBLE
            carbRatio.visibility = View.VISIBLE
            correction.visibility = View.VISIBLE
            calculateButton.visibility = View.VISIBLE
            resultText.visibility=View.VISIBLE
            recyclerView.visibility = View.GONE
            deleteAllButton.visibility=View.GONE

        }
    }
    private fun loadLogs() {
        val sharedPreferences = getSharedPreferences("app_logs", MODE_PRIVATE)
        val allLogs = sharedPreferences.getString("logs", "") ?: ""
        val logs = allLogs.split("\n") // Split by newline to get individual log entries
        val logEntries = mutableListOf<Pair<Long, String>>()

        for (log in logs) {
            if (log.isNotBlank()) {
                val parts = log.split(" - ", limit = 2)
                if (parts.size == 2) {
                    try {
                        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(parts[0])?.time ?: continue
                        val logText = parts[1]
                        val formattedLogText = logText.replace("=" ,"\n")
                        logEntries.add(Pair(timestamp, formattedLogText))
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error parsing log entry: $log", e)
                    }
                }
            }
        }


        // Debugging Log the number of parsed log entries
        Log.d("HomeActivity", "Loaded ${logEntries.size} log entries")

        val adapter = LogAdapter(logEntries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }







    // Move these functions to the top level of your class
    private fun saveLog(logEntry: String) {
        val sharedPreferences = getSharedPreferences("app_logs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val existingLogs = sharedPreferences.getString("logs", "") ?: ""

        // Check if the log entry already contains a timestamp
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logWithTimestamp = if (logEntry.contains(formattedDate)) {
            logEntry // Log entry already contains a timestamp
        } else {
            "$formattedDate - $logEntry" // Add timestamp to the log entry
        }

        val updatedLogs = if (existingLogs.isEmpty()) logWithTimestamp else "$existingLogs\n$logWithTimestamp"
        editor.putString("logs", updatedLogs)
        editor.apply()
    }



    private fun clearAllLogs() {
        val sharedPreferences = getSharedPreferences("app_logs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        // Notify the user that all logs have been deleted (optional)
        Toast.makeText(this, "All logs have been deleted", Toast.LENGTH_SHORT).show()
    }



}

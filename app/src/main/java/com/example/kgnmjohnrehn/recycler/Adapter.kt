package com.example.kgnmjohnrehn.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kgnmjohnrehn.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class LogAdapter(private val logs: List<Pair<Long, String>>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    // ViewHolder class
    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logTextView: TextView = itemView.findViewById(R.id.logTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val (timestamp, logText) = logs[position]
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        val logEntryText = holder.itemView.context.getString(R.string.log_entry_format, formattedDate, logText)
        holder.logTextView.text = logEntryText
    }


    override fun getItemCount() = logs.size
}





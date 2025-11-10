package com.griffith.util

import java.text.SimpleDateFormat
import java.util.*

object DateFmt {
    private val full = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
    private val short = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    fun full(ts: Long) = full.format(Date(ts))
    fun short(ts: Long) = short.format(Date(ts))
}
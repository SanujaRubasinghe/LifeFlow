package com.example.healthtracker.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.healthtracker.R

object GreetingHelper {

    fun setGreeting(context: Context, textView: TextView, fragmentName: String) {
        if (fragmentName.equals("Profile", ignoreCase = true)) {
            textView.visibility = TextView.GONE
            return
        }

        val greetingText = getGreetingText(context)
        textView.visibility = TextView.VISIBLE

        val spannable = android.text.SpannableString(greetingText)
        val usernameStart = greetingText.indexOf(",") + 2
        val usernameEnd = greetingText.length
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.primary_color)
            ),
            usernameStart,
            usernameEnd,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

    private fun getGreetingText(context: Context): String {
        val sharedPref = HealthPreferenceManager.getInstance(context)
        val userProfile = sharedPref.getUserProfile()

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }

        return "$greeting, ${userProfile?.name}!"
    }
}

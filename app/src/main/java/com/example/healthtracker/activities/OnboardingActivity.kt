package com.example.healthtracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.healthtracker.adapters.OnboardingAdapter
import com.example.healthtracker.R
import com.example.healthtracker.activities.RegisterActivity
import com.example.healthtracker.utils.HealthPreferenceManager
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: WormDotsIndicator
    private lateinit var getStartedBtn: Button
    private lateinit var adapter: OnboardingAdapter

    private val layouts = listOf(
        R.layout.onboarding_screen_1,
        R.layout.onboarding_screen_2,
        R.layout.onboarding_screen_3,
        R.layout.onboarding_screen_4,
        R.layout.onboarding_screen_5
    )

    private lateinit var preferenceManager: HealthPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        preferenceManager = HealthPreferenceManager.Companion.getInstance(this)

        viewPager = findViewById(R.id.view_pager)
        dotsIndicator = findViewById(R.id.dots_indicator)
        getStartedBtn = findViewById(R.id.get_started_btn)

        adapter = OnboardingAdapter(layouts)
        viewPager.adapter = adapter
        dotsIndicator.attachTo(viewPager)

        updateButton(viewPager.currentItem)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButton(position)
            }
        })

        getStartedBtn.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateButton(position: Int) {
        if (position == layouts.lastIndex) {
            getStartedBtn.visibility = View.VISIBLE
        } else {
            getStartedBtn.visibility = View.GONE
        }
    }

    private fun finishOnboarding() {
        preferenceManager.setFirstLaunch(false)
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
}
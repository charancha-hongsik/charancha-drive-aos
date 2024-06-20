package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.charancha.drive.ImageSliderAdapter
import com.charancha.drive.R

class OnBoardingActivity: AppCompatActivity() {
    lateinit var viewpagerOnbaording: ViewPager2
    lateinit var tvOnbaordingText1: TextView
    lateinit var tvOnbaordingText2: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        setResources()
    }

    fun setResources(){
        tvOnbaordingText1 = findViewById(R.id.tv_onbaording_text1)
        tvOnbaordingText2 = findViewById(R.id.tv_onbaording_text2)


        viewpagerOnbaording = findViewById(R.id.viewpager_onboarding)
        viewpagerOnbaording.adapter = ImageSliderAdapter(getImages())
        viewpagerOnbaording.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        viewpagerOnbaording.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Called when the scroll state changes (starting, stopping, or changing position)
            }

            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> {
                        tvOnbaordingText1.setText(resources.getText(R.string.onboarding_title1))
                        tvOnbaordingText2.setText(resources.getText(R.string.onboarding_contents1))
                    }

                    1 -> {
                        tvOnbaordingText1.setText(resources.getText(R.string.onboarding_title2))
                        tvOnbaordingText2.setText(resources.getText(R.string.onboarding_contents2))
                    }

                    2 -> {
                        tvOnbaordingText1.setText(resources.getText(R.string.onboarding_title3))
                        tvOnbaordingText2.setText(resources.getText(R.string.onboarding_contents3))
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Called when the page is scrolled
            }
        })
    }


    private fun getImages(): ArrayList<Int> {
        return arrayListOf(
            R.drawable.onboarding1,
            R.drawable.onboarding2,
            R.drawable.onboarding3,)
    }

    private fun setTimer(){

    }
}
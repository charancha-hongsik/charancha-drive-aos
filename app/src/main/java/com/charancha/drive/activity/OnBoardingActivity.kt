package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.charancha.drive.CustomSpeedLinearSmoothScroller
import com.charancha.drive.ImageSliderAdapter
import com.charancha.drive.R

class OnBoardingActivity: AppCompatActivity() {
    lateinit var viewpagerOnbaording: ViewPager2
    lateinit var tvOnbaordingText1: TextView
    lateinit var tvOnbaordingText2: TextView
    lateinit var IvIndicator:ImageView
    lateinit var layoutNext: ConstraintLayout
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        setResources()
        setTimer()
    }

    fun setResources(){
        tvOnbaordingText1 = findViewById(R.id.tv_onbaording_text1)
        tvOnbaordingText2 = findViewById(R.id.tv_onbaording_text2)

        IvIndicator = findViewById(R.id.iv_indicator)

        layoutNext = findViewById(R.id.layout_next)
        layoutNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        viewpagerOnbaording = findViewById(R.id.viewpager_onboarding)
        viewpagerOnbaording.adapter = ImageSliderAdapter(getImages())
        viewpagerOnbaording.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        viewpagerOnbaording.isUserInputEnabled = false

        viewpagerOnbaording.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Called when the scroll state changes (starting, stopping, or changing position)
            }

            override fun onPageSelected(position: Int) {
                val realPosition = position % getImages().size
                when(realPosition){
                    0 -> {
                        tvOnbaordingText1.setText(resources.getText(R.string.onboarding_title1))
                        tvOnbaordingText2.setText(resources.getText(R.string.onboarding_contents1))
                        IvIndicator.setImageDrawable(ContextCompat.getDrawable(this@OnBoardingActivity, R.drawable.indicator1))
                    }

                    1 -> {
                        tvOnbaordingText1.setText(resources.getText(R.string.onboarding_title2))
                        tvOnbaordingText2.setText(resources.getText(R.string.onboarding_contents2))
                        IvIndicator.setImageDrawable(ContextCompat.getDrawable(this@OnBoardingActivity, R.drawable.indicator2))

                    }

                    2 -> {
                        tvOnbaordingText1.setText(resources.getText(R.string.onboarding_title3))
                        tvOnbaordingText2.setText(resources.getText(R.string.onboarding_contents3))
                        IvIndicator.setImageDrawable(ContextCompat.getDrawable(this@OnBoardingActivity, R.drawable.indicator3))

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

    private fun setTimer() {
        val updatePage = object : Runnable {
            override fun run() {
                currentPage = (currentPage + 1) % Integer.MAX_VALUE
                setCurrentItem(currentPage, true)
                handler.postDelayed(this, 3000) // 3초마다 페이지 넘김
            }
        }
        handler.postDelayed(updatePage, 3000)
    }

    private fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        viewpagerOnbaording.post {
            try {
                val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
                recyclerViewField.isAccessible = true
                val recyclerView = recyclerViewField.get(viewpagerOnbaording) as RecyclerView
                val layoutManager = recyclerView.layoutManager
                val smoothScroller = CustomSpeedLinearSmoothScroller(this)
                smoothScroller.targetPosition = item
                layoutManager?.startSmoothScroll(smoothScroller)
            } catch (e: Exception) {
                viewpagerOnbaording.setCurrentItem(item, smoothScroll)
            }
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}
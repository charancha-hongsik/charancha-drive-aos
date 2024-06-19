package com.charancha.drive.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.CommonUtil
import com.charancha.drive.R

class TermsOfUseActivity: AppCompatActivity() {
    private lateinit var ibArrowTerms:ImageButton
    private lateinit var btnNext:ConstraintLayout
    private lateinit var ibAllAccept:ImageButton
    private lateinit var ibTerms1:ImageButton
    private lateinit var ibTerms2:ImageButton
    private lateinit var ibTerms3:ImageButton
    private lateinit var ibTerms4:ImageButton
    private lateinit var tvTerms1:TextView
    private lateinit var tvTermsTitle1:TextView
    private lateinit var tvTermsTitle2:TextView
    private lateinit var tvTermsTitle3:TextView
    private lateinit var tvTermsTitle4:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        init()
    }

    private fun init(){
        setResource()
        setListener()
    }

    private fun setResource(){
        ibArrowTerms = findViewById(R.id.ib_arrow_terms)
        btnNext = findViewById(R.id.btn_next)
        ibAllAccept = findViewById(R.id.ib_all_accept)
        ibTerms1 = findViewById(R.id.ib_terms1)
        ibTerms2 = findViewById(R.id.ib_terms2)
        ibTerms3 = findViewById(R.id.ib_terms3)
        ibTerms4 = findViewById(R.id.ib_terms4)
        tvTerms1 = findViewById(R.id.tv_terms1)
        tvTermsTitle1 = findViewById(R.id.tv_terms_title1)
        tvTermsTitle2 = findViewById(R.id.tv_terms_title2)
        tvTermsTitle3 = findViewById(R.id.tv_terms_title3)
        tvTermsTitle4 = findViewById(R.id.tv_terms_title4)


        // TextView에 SpannableString 설정
        tvTerms1.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.first_terms_text), resources.getString(R.string.first_terms_text_red), resources.getColor(R.color.pri_500))
        tvTermsTitle1.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title1), resources.getString(R.string.terms_title1_gray), resources.getColor(R.color.gray_400))
        tvTermsTitle2.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title2), resources.getString(R.string.terms_title2_gray), resources.getColor(R.color.gray_400))
        tvTermsTitle3.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title3), resources.getString(R.string.terms_title3_gray), resources.getColor(R.color.gray_400))
        tvTermsTitle4.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title4), resources.getString(R.string.terms_title4_gray), resources.getColor(R.color.gray_400))

    }

    private fun setListener(){
        ibArrowTerms.setOnClickListener {
            finish()
        }

        btnNext.setOnClickListener {
            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
        }

        btnNext.isClickable = false

        ibAllAccept.setOnClickListener {
            ibAllAccept.isSelected = !ibAllAccept.isSelected

            ibTerms1.isSelected = ibAllAccept.isSelected
            ibTerms2.isSelected = ibAllAccept.isSelected
            ibTerms3.isSelected = ibAllAccept.isSelected
            ibTerms4.isSelected = ibAllAccept.isSelected

            checkAllAccept()
        }

        ibTerms1.setOnClickListener {
            ibTerms1.isSelected = !ibTerms1.isSelected

            if(!ibTerms1.isSelected)
                ibAllAccept.isSelected = false

            checkAllAccept()
        }

        ibTerms2.setOnClickListener {
            ibTerms2.isSelected = !ibTerms2.isSelected

            if(!ibTerms2.isSelected)
                ibAllAccept.isSelected = false

            checkAllAccept()
        }
        ibTerms3.setOnClickListener {
            ibTerms3.isSelected = !ibTerms3.isSelected

            if(!ibTerms3.isSelected)
                ibAllAccept.isSelected = false

            checkAllAccept()
        }

        ibTerms4.setOnClickListener {
            ibTerms4.isSelected = !ibTerms4.isSelected
        }
    }

    private fun checkAllAccept(){
        btnNext.isSelected = ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected
        btnNext.isClickable = ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected
    }
}
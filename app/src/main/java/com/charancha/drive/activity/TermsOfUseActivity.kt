package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R

class TermsOfUseActivity: AppCompatActivity() {
    private lateinit var btnNext:ConstraintLayout
    private lateinit var ibAllAccept:ImageButton
    private lateinit var ibTerms1:ImageButton
    private lateinit var ibTerms2:ImageButton
    private lateinit var ibTerms3:ImageButton
    private lateinit var ibTerms4:ImageButton



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
        btnNext = findViewById(R.id.btn_next)
        ibAllAccept = findViewById(R.id.ib_all_accept)
        ibTerms1 = findViewById(R.id.ib_terms1)
        ibTerms2 = findViewById(R.id.ib_terms2)
        ibTerms3 = findViewById(R.id.ib_terms3)
        ibTerms4 = findViewById(R.id.ib_terms4)
    }

    private fun setListener(){
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
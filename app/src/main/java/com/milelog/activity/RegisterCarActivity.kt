package com.milelog.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.milelog.R


class RegisterCarActivity: BaseActivity() {
    lateinit var view_register_percent1: View
    lateinit var view_register_percent2:View
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var tv_register_car_hint: TextView
    lateinit var btn_next:ConstraintLayout
    lateinit var tv_register_car:TextView
    lateinit var tv_register_car_caution:TextView
    lateinit var tv_confirm:TextView
    lateinit var et_register_car:EditText
    lateinit var btn_register_car:ConstraintLayout
    lateinit var tv_releaseDt:TextView

    var no = 0

    var carNo:String? = null
    var carOwner:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_car)

        init()
    }

    fun init(){
        setResources()
    }

    fun setResources(){
        view_register_percent1 = findViewById(R.id.view_register_percent1)
        view_register_percent2 = findViewById(R.id.view_register_percent2)
        tv_register_car_hint = findViewById(R.id.tv_register_car_hint)

        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)

        tv_confirm = findViewById(R.id.tv_confirm)
        tv_register_car = findViewById(R.id.tv_register_car)
        tv_register_car_caution = findViewById(R.id.tv_register_car_caution)

        et_register_car = findViewById(R.id.et_register_car)
        tv_releaseDt = findViewById(R.id.tv_releaseDt)

        btn_register_car = findViewById(R.id.btn_register_car)
        btn_register_car.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                et_register_car.performClick()
            }
        })

        btn_next = findViewById(R.id.btn_next)

        btn_next.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                when(no) {
                    0 -> {
                        carNo = et_register_car.text.toString()
                        setCarOwnerPage()
                        no++
                    }

                    1 -> {
                        carOwner = et_register_car.text.toString()
                        startActivity(
                            Intent(
                                this@RegisterCarActivity,
                                LoadCarInfoActivity::class.java
                            ).putExtra("carNo", carNo).putExtra("carOwner", carOwner).putExtra("add",intent.getBooleanExtra("add",false))
                        )
                    }

                }
            }
        })

        btn_next.isClickable = false
        btn_next.isSelected = false


        view_register_percent1.isSelected = true
        et_register_car.setOnFocusChangeListener { view, b ->
            if(b){
                tv_register_car_hint.visibility = GONE
            }else{
                tv_register_car_hint.visibility = VISIBLE

            }
        }

        ib_arrow_register_car.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                no--
                when(no){
                    -1 -> {
                        finish()
                    }

                    0 -> {
                        setCarNoPage()
                    }
                }
            }

        })

        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=7){
                        btn_next.isSelected = true
                        btn_next.isClickable = true
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    /**
     * 1. 최대 9자리까지 입력 가능
     * 2. 화면 진입 시 input에 포커스
     * 3.
     */
    fun setCarNoPage(){
        tv_register_car.text = resources.getString(R.string.register_car_no_title)
        tv_register_car_caution.text = resources.getString(R.string.register_car_no_errormessage)
        et_register_car.text = null
        et_register_car.hint = "123가1234"
        carNo = null

        view_register_percent1.isSelected = true
        view_register_percent2.isSelected = false

        showKeyboard(et_register_car)

        et_register_car.filters = arrayOf(InputFilter.LengthFilter(9))


        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=7){
                        btn_next.isSelected = true
                        btn_next.isClickable = true
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


    }

    /**
     * 1. 최대 17자리까지 입력 가능
     * 2. 화면 진입 시 input에 focus
     */
    fun setCarOwnerPage(){
        tv_register_car.text = resources.getString(R.string.register_car_owner_title)
        tv_register_car_caution.text = resources.getString(R.string.register_car_owner_errormessage)
        et_register_car.text = null
        et_register_car.hint = "김마일"
        carOwner = null

        view_register_percent1.isSelected = true
        view_register_percent2.isSelected = true

        showKeyboard(et_register_car)

        et_register_car.filters = arrayOf(InputFilter.LengthFilter(17))

        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=2){
                        btn_next.isSelected = true
                        btn_next.isClickable = true
                    }else{
                        btn_next.isSelected = false
                        btn_next.isClickable = false
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }


    override fun onBackPressed() {
        no--
        when(no){
            -1 -> {
                finish()
            }

            0 -> {
                setCarNoPage()
            }
        }
    }
}
package com.charancha.drive.activity

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.PostMyCarResponse
import com.google.gson.Gson


class RegisterCarActivity: AppCompatActivity() {
    lateinit var view_register_percent1: View
    lateinit var view_register_percent2:View
    lateinit var view_register_percent3:View
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var tv_register_car_hint: TextView
    lateinit var et_register_car: EditText
    lateinit var btn_next:ConstraintLayout
    lateinit var tv_register_car:TextView
    lateinit var tv_register_car_caution:TextView
    lateinit var layout_after_inquiry:ConstraintLayout
    lateinit var layout_before_inquiry:ConstraintLayout
    lateinit var tv_car_no:TextView
    lateinit var tv_car_owner:TextView
    lateinit var tv_car_id:TextView
    lateinit var tv_car_model_name:TextView
    lateinit var tv_car_year:TextView
    lateinit var tv_car_fuel:TextView
    lateinit var tv_confirm:TextView



    lateinit var getPostMyCarResult:ActivityResultLauncher<Intent>


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

        getPostMyCarResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                val jsonString = it.data?.getStringExtra("response")

                val gson = Gson()
                val postMyCarResponse = gson.fromJson(jsonString, PostMyCarResponse::class.java)

                setAfterInquiry(postMyCarResponse)
            } else{
                no--
            }
        }
    }

    fun setResources(){
        view_register_percent1 = findViewById(R.id.view_register_percent1)
        view_register_percent2 = findViewById(R.id.view_register_percent2)
        view_register_percent3 = findViewById(R.id.view_register_percent3)

        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        tv_register_car_hint = findViewById(R.id.tv_register_car_hint)
        et_register_car = findViewById(R.id.et_register_car)

        tv_register_car = findViewById(R.id.tv_register_car)
        tv_register_car_caution = findViewById(R.id.tv_register_car_caution)

        layout_after_inquiry = findViewById(R.id.layout_after_inquiry)
        layout_before_inquiry = findViewById(R.id.layout_before_inquiry)
        tv_car_no = findViewById(R.id.tv_car_no)
        tv_car_owner = findViewById(R.id.tv_car_owner)
        tv_car_id = findViewById(R.id.tv_car_id)
        tv_car_model_name = findViewById(R.id.tv_car_model_name)
        tv_car_year = findViewById(R.id.tv_car_year)
        tv_car_fuel = findViewById(R.id.tv_car_fuel)
        tv_confirm = findViewById(R.id.tv_confirm)



        btn_next = findViewById(R.id.btn_next)
        btn_next.setOnClickListener {

            when(no){
                0 -> {
                    carNo = et_register_car.text.toString()
                    setCarOwnerPage()
                }

                1 -> {
                    carOwner = et_register_car.text.toString()
                    getPostMyCarResult.launch(Intent(this@RegisterCarActivity, LoadCarInfoActivity::class.java).putExtra("carNo",carNo).putExtra("carOwner",carOwner))
                }

                2 -> {

//                    startActivity(Intent(this, MainActivity::class.java).addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK))
//                    finish()
                }
            }

            no++
        }

        view_register_percent1.isSelected = true
        et_register_car.setOnFocusChangeListener { view, b ->
            if(b){
                tv_register_car_hint.visibility = GONE
            }else{
                tv_register_car_hint.visibility = VISIBLE

            }
        }

        ib_arrow_register_car.setOnClickListener {
            no--
            when(no){
                -1 -> {
                    finish()
                }

                0 -> {
                    setCarNoPage()
                }

                1 -> {
                    setCarOwnerPage()
                }

                2 -> {


                }

                3 -> {

                }
            }
        }

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
        view_register_percent3.isSelected = false

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
        view_register_percent3.isSelected = false

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

    fun setAfterInquiry(postMyCarResponse: PostMyCarResponse){
        layout_before_inquiry.visibility = GONE
        layout_after_inquiry.visibility = VISIBLE

        tv_car_fuel.text = postMyCarResponse.fuel
        tv_car_owner.text = postMyCarResponse.ownerName
        tv_car_id.text = postMyCarResponse.vehicleIdentificationNumber
        tv_car_no.text = postMyCarResponse.licensePlateNumber
        tv_car_year.text = postMyCarResponse.carYear
        tv_car_model_name.text = postMyCarResponse.modelName

        tv_confirm.text = "확인했어요"
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

            1 -> {
                setCarOwnerPage()
            }

            2 -> {

            }

            3 -> {

            }
        }
    }

    // EditText에 포커스를 주고 키보드를 올리는 함수
    fun showKeyboard(editText: EditText) {
        // EditText에 포커스 주기
        editText.requestFocus()

        // InputMethodManager를 통해 키보드 올리기
        val imm = editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}
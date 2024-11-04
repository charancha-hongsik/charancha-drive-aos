package com.milelog.activity

import android.content.Context
import android.content.Intent
import android.graphics.ColorSpace.Model
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.CarDetail
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.activity.MyDriveHistoryActivity.DriveHistoryAdapter
import com.milelog.retrofit.response.CarDetailResponse
import com.milelog.retrofit.response.GetRecentDrivingStatisticsResponse
import com.milelog.retrofit.response.GradeDetailResponse
import com.milelog.retrofit.response.GradeResponse
import com.milelog.retrofit.response.MakerResponse
import com.milelog.retrofit.response.ModelDetailResponse
import com.milelog.retrofit.response.ModelResponse
import com.milelog.retrofit.response.PostMyCarResponse
import com.milelog.retrofit.response.TermsSummaryResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

/**
 * 제조사, 모델명이 필수값
 * 필수값 선택 시 저장할게요 활성화
 */
class CarDetailActivity: BaseRefreshActivity() {
    companion object{
        const val FUEL = "fuel"
        const val MAKER = "maker"
        const val MODEL = "model"
        const val MODEL_DETAIL = "modelDetail"
        const val GRADE = "grade"
        const val GRADE_DETAIL = "gradeDetail"
    }

    private lateinit var postMyCarResponse: PostMyCarResponse
    lateinit var tv_confirm_mycar_info1:TextView
    lateinit var listView: ListView
    lateinit var layout_select:CoordinatorLayout
    lateinit var btn_maker:ConstraintLayout
    lateinit var btn_model:ConstraintLayout
    lateinit var btn_model_detail:ConstraintLayout
    lateinit var btn_grade:ConstraintLayout
    lateinit var btn_grade_detail:ConstraintLayout

    lateinit var tv_maker_hint:TextView
    lateinit var tv_maker:TextView
    lateinit var tv_model_hint:TextView
    lateinit var tv_model:TextView
    lateinit var tv_model_detail_hint:TextView
    lateinit var tv_model_detail:TextView
    lateinit var tv_grade_hint:TextView
    lateinit var tv_grade:TextView
    lateinit var tv_grade_detail_hint:TextView
    lateinit var tv_grade_detail:TextView
    lateinit var tv_model_title:ConstraintLayout
    lateinit var tv_model_detail_title:ConstraintLayout
    lateinit var tv_grade_title:ConstraintLayout
    lateinit var tv_grade_detail_title:ConstraintLayout
    lateinit var ib_arrow_register_car:ImageButton

    lateinit var btn_next:ConstraintLayout

    var selectedMaker: MakerResponse? = null
    var selectedModel: ModelResponse? = null
    var selectedModelDetail: ModelDetailResponse? = null
    var selectedGrade: GradeResponse? = null
    var selectedGradeDetail: GradeDetailResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_detail)

        init()
        setListener()
        setInfo()
    }

    private fun init(){
        tv_confirm_mycar_info1 = findViewById(R.id.tv_confirm_mycar_info1)
        layout_select = findViewById(R.id.layout_select)
        listView = findViewById(R.id.listView)
        btn_maker = findViewById(R.id.btn_maker)
        btn_model = findViewById(R.id.btn_model)
        btn_model_detail = findViewById(R.id.btn_model_detail)
        btn_grade = findViewById(R.id.btn_grade)
        btn_grade_detail = findViewById(R.id.btn_grade_detail)
        tv_maker_hint = findViewById(R.id.tv_maker_hint)
        tv_maker = findViewById(R.id.tv_maker)
        tv_model_hint = findViewById(R.id.tv_model_hint)
        tv_model = findViewById(R.id.tv_model)
        tv_model_detail_hint = findViewById(R.id.tv_model_detail_hint)
        tv_model_detail = findViewById(R.id.tv_model_detail)
        tv_grade_hint = findViewById(R.id.tv_grade_hint)
        tv_grade = findViewById(R.id.tv_grade)
        tv_grade_detail_hint = findViewById(R.id.tv_grade_detail_hint)
        tv_grade_detail = findViewById(R.id.tv_grade_detail)
        tv_model_title = findViewById(R.id.tv_model_title)
        tv_model_detail_title = findViewById(R.id.tv_model_detail_title)
        tv_grade_title = findViewById(R.id.tv_grade_title)
        tv_grade_detail_title = findViewById(R.id.tv_grade_detail_title)
        btn_next = findViewById(R.id.btn_next)
        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
    }

    private fun setListener(){
        btn_maker.setOnClickListener {
            setSelector(MAKER)
        }

        btn_model.setOnClickListener {
            setSelector(MODEL)
        }

        btn_model_detail.setOnClickListener {
            setSelector(MODEL_DETAIL)
        }

        btn_grade.setOnClickListener {
            setSelector(GRADE)
        }

        btn_grade_detail.setOnClickListener {
            setSelector(GRADE_DETAIL)

        }

        layout_select.setOnClickListener{
            layout_select.visibility = GONE
        }

        btn_next.setOnClickListener {
            selectedMaker?.let{
                postMyCarResponse.makerCd = it.makerCd
                postMyCarResponse.makerNm = it.makerNm
            }

            selectedModel?.let{
                postMyCarResponse.modelCd = it.modelCd
                postMyCarResponse.modelNm = it.modelNm
            }

            selectedModelDetail?.let{
                postMyCarResponse.modelDetailCd = it.modelDetailCd
                postMyCarResponse.modelDetailNm = it.modelDetailNm
            }?: run{
                postMyCarResponse.modelDetailCd = null
                postMyCarResponse.modelDetailNm = null
            }

            selectedGrade?.let{
                postMyCarResponse.gradeCd = it.gradeCd
                postMyCarResponse.gradeNm = it.gradeNm
            }?: run {
                postMyCarResponse.gradeCd = null
                postMyCarResponse.gradeNm = null
            }

            selectedGradeDetail?.let{
                postMyCarResponse.gradeDetailCd = it.gradeDetailCd
                postMyCarResponse.gradeDetailNm = it.gradeDetailNm
            }?: run{
                postMyCarResponse.gradeDetailCd = null
                postMyCarResponse.gradeDetailNm = null
            }

            val carName = listOfNotNull(
                postMyCarResponse.makerNm,
                if (postMyCarResponse.modelDetailNm.isNullOrEmpty()) postMyCarResponse.modelNm else null, // modelDetailNm이 없을 때만 modelNm 추가
                postMyCarResponse.modelDetailNm,
                postMyCarResponse.gradeNm,
                postMyCarResponse.gradeDetailNm
            ).filterNot { it.isNullOrEmpty() } // null이나 빈 문자열을 필터링
                .joinToString(" ")

            postMyCarResponse.carName = carName

            val intent = Intent(this@CarDetailActivity, LoadCarMoreInfoActivity::class.java)
            intent.putExtra("carInfo",Gson().toJson(postMyCarResponse))
            setResult(RESULT_OK, intent)
            finish()
        }

        ib_arrow_register_car.setOnClickListener {
            finish()
        }

    }

    private fun setInfo(){
        val jsonString = intent.getStringExtra("carInfo")
        val gson = Gson()
        postMyCarResponse = gson.fromJson(jsonString, PostMyCarResponse::class.java)

        if (postMyCarResponse.trimHint.isNullOrEmpty()) {
            // trimHint가 없는 경우, visibility를 GONE으로 설정
            tv_confirm_mycar_info1.visibility = GONE
        } else {
            // trimHint가 있는 경우, 텍스트를 설정하고 visibility를 VISIBLE로 설정
            tv_confirm_mycar_info1.text = postMyCarResponse.trimHint + "\n차량명이 맞으신가요?"
            tv_confirm_mycar_info1.visibility = VISIBLE
        }


        if(postMyCarResponse.makerCd.isNullOrEmpty()){
            setMakerUI()
        }else{
            tv_maker_hint.visibility = GONE
            tv_maker.visibility = VISIBLE
            tv_maker.text = postMyCarResponse.makerNm

            selectedMaker = MakerResponse(postMyCarResponse.makerCd!!, postMyCarResponse.makerNm!!)
        }

        if(postMyCarResponse.modelCd.isNullOrEmpty()){
            if(!postMyCarResponse.makerCd.isNullOrEmpty()){
                setNext(MODEL)
            }
        }else{
            setModelUI()
            tv_model_hint.visibility = GONE
            tv_model.visibility = VISIBLE
            tv_model.text = postMyCarResponse.modelNm

            selectedModel = ModelResponse(postMyCarResponse.modelCd!!, postMyCarResponse.modelNm!!)

            btn_next.isSelected = true
            btn_next.isClickable = true
        }

        if(postMyCarResponse.modelDetailCd.isNullOrEmpty()){
//            setModelDetailUI()
            if(!postMyCarResponse.modelCd.isNullOrEmpty()){
                setNext(MODEL_DETAIL)
            }
        }else{
            setModelDetailUI()
            tv_model_detail_hint.visibility = GONE
            tv_model_detail.visibility = VISIBLE
            tv_model_detail.text = postMyCarResponse.modelDetailNm

            selectedModelDetail = ModelDetailResponse(postMyCarResponse.modelDetailCd!!, postMyCarResponse.modelDetailNm!!)
        }

        if(postMyCarResponse.gradeCd.isNullOrEmpty()){
            if(!postMyCarResponse.modelDetailCd.isNullOrEmpty()){
                setNext(GRADE)
            }
        }else{
            setGradeUI()
            tv_grade_hint.visibility = GONE
            tv_grade.visibility = VISIBLE
            tv_grade.text = postMyCarResponse.gradeNm

            selectedGrade = GradeResponse(postMyCarResponse.gradeCd!!, postMyCarResponse.gradeNm!!)
        }

        if(postMyCarResponse.gradeDetailCd.isNullOrEmpty()){
            if(!postMyCarResponse.gradeCd.isNullOrEmpty()){
                setNext(GRADE_DETAIL)
            }
        }else{
            setGradeDetailUI()
            tv_grade_detail_hint.visibility = GONE
            tv_grade_detail.visibility = VISIBLE
            tv_grade_detail.text = postMyCarResponse.gradeDetailNm

            selectedGradeDetail = GradeDetailResponse(postMyCarResponse.gradeDetailCd!!, postMyCarResponse.gradeDetailNm!!)
        }


    }

    private fun setSelector(key:String){
        var parentCode:String? = null
        when(key){
            MAKER -> {
                parentCode = null
            }

            MODEL -> {
                parentCode = selectedMaker?.makerCd
            }

            MODEL_DETAIL -> {
                parentCode = selectedModel?.modelCd

            }

            GRADE -> {
                parentCode = selectedModelDetail?.modelDetailCd

            }

            GRADE_DETAIL -> {
                parentCode = selectedGrade?.gradeCd
            }

            FUEL -> {
                parentCode = null
            }
        }

        apiService().getCharanchaCode("Bearer " + PreferenceUtil.getPref(this@CarDetailActivity, PreferenceUtil.ACCESS_TOKEN, "")!!, key, parentCode).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val jsonString = response.body()?.string()

                val gson = Gson()
                val type: Type = object : TypeToken<List<CarDetailResponse>>() {}.type
                val carDetails:List<CarDetailResponse> = gson.fromJson(jsonString, type)

                val itemList: MutableList<CarDetail> = ArrayList()

                when(key){
                    MAKER -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.makerCd, carDetail.makerNm))
                    }

                    MODEL -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.modelCd, carDetail.modelNm))
                    }

                    MODEL_DETAIL -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.modelDetailCd, carDetail.modelDetailNm))

                    }

                    GRADE -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.gradeCd, carDetail.gradeNm))

                    }

                    GRADE_DETAIL -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.gradeDetailCd, carDetail.gradeDetailNm))
                    }

                    FUEL -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.code, carDetail.codeNm))
                    }
                }

                // adapter 생성
                val adapter = CarDetailAdapter(this@CarDetailActivity, R.layout.edit_fuel_textview, itemList, key)

                // listView에 adapter 연결
                listView.adapter = adapter

                layout_select.visibility = VISIBLE
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("teestsetestst","testeststestst :: " + t.toString())
            }

        })
    }

    override fun onBackPressed() {
        if(layout_select.visibility == VISIBLE){
            layout_select.visibility = GONE
        }else{
            finish()
        }
    }

    inner class CarDetailAdapter(context: Context, resource: Int, items: List<CarDetail>, val key:String) : ArrayAdapter<CarDetail>(context, resource, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.edit_fuel_textview, parent, false)

            val carDetail = getItem(position)

            carDetail?.let{
                val tv_fuel = view.findViewById<TextView>(R.id.tv_fuel)
                tv_fuel.text = carDetail.name

                tv_fuel.setOnClickListener {
                    when(key){
                        MAKER -> {
                            tv_maker_hint.visibility = GONE
                            tv_maker.visibility = VISIBLE
                            tv_maker.text = carDetail.name

                            selectedMaker = MakerResponse(carDetail.code, carDetail.name)
                            selectedModel = null
                            selectedModelDetail = null
                            selectedGrade = null
                            selectedGradeDetail = null

                            layout_select.visibility = GONE

                            setNext(MODEL)
                        }

                        MODEL -> {
                            tv_model_hint.visibility = GONE
                            tv_model.visibility = VISIBLE
                            tv_model.text = carDetail.name

                            selectedModel = ModelResponse(carDetail.code, carDetail.name)
                            selectedModelDetail = null
                            selectedGrade = null
                            selectedGradeDetail = null

                            layout_select.visibility = GONE

                            btn_next.isSelected = true
                            btn_next.isClickable = true

                            setNext(MODEL_DETAIL)
                        }

                        MODEL_DETAIL -> {
                            tv_model_detail_hint.visibility = GONE
                            tv_model_detail.visibility = VISIBLE
                            tv_model_detail.text = carDetail.name

                            selectedModelDetail = ModelDetailResponse(carDetail.code, carDetail.name)
                            selectedGrade = null
                            selectedGradeDetail = null

                            layout_select.visibility = GONE

                            setNext(GRADE)
                        }

                        GRADE -> {
                            tv_grade_hint.visibility = GONE
                            tv_grade.visibility = VISIBLE
                            tv_grade.text = carDetail.name

                            selectedGradeDetail = null

                            selectedGrade = GradeResponse(carDetail.code, carDetail.name)

                            layout_select.visibility = GONE

                            setNext(GRADE_DETAIL)
                        }

                        GRADE_DETAIL -> {
                            tv_grade_detail_hint.visibility = GONE
                            tv_grade_detail.visibility = VISIBLE
                            tv_grade_detail.text = carDetail.name

                            selectedGradeDetail = GradeDetailResponse(carDetail.code, carDetail.name)

                            layout_select.visibility = GONE
                        }

                        FUEL -> {

                        }
                    }
                }
            }

            return view
        }
    }

    private fun setMakerUI(){
        tv_maker_hint.visibility = VISIBLE
        tv_maker.visibility = GONE
        btn_maker.visibility = VISIBLE

        tv_model_title.visibility = GONE
        tv_model_hint.visibility = VISIBLE
        tv_model.visibility = GONE
        btn_model.visibility = GONE

        tv_model_detail_title.visibility = GONE
        tv_model_detail_hint.visibility = VISIBLE
        tv_model_detail.visibility = GONE
        btn_model_detail.visibility = GONE

        tv_grade_title.visibility = GONE
        tv_grade_hint.visibility = VISIBLE
        tv_grade.visibility = GONE
        btn_grade.visibility = GONE

        tv_grade_detail_title.visibility = GONE
        tv_grade_detail_hint.visibility = VISIBLE
        tv_grade_detail.visibility = GONE
        btn_grade_detail.visibility = GONE


    }

    private fun setModelUI(){
        tv_model_title.visibility = VISIBLE
        tv_model_hint.visibility = VISIBLE
        tv_model.visibility = GONE
        btn_model.visibility = VISIBLE

        tv_model_detail_title.visibility = GONE
        tv_model_detail_hint.visibility = VISIBLE
        tv_model_detail.visibility = GONE
        btn_model_detail.visibility = GONE

        tv_grade_title.visibility = GONE
        tv_grade_hint.visibility = VISIBLE
        tv_grade.visibility = GONE
        btn_grade.visibility = GONE

        tv_grade_detail_title.visibility = GONE
        tv_grade_detail_hint.visibility = VISIBLE
        tv_grade_detail.visibility = GONE
        btn_grade_detail.visibility = GONE

        btn_next.isSelected = false
        btn_next.isClickable = false


    }

    private fun setModelDetailUI(){
        tv_model_detail_title.visibility = VISIBLE
        tv_model_detail_hint.visibility = VISIBLE
        tv_model_detail.visibility = GONE
        btn_model_detail.visibility = VISIBLE

        tv_grade_title.visibility = GONE
        tv_grade_hint.visibility = VISIBLE
        tv_grade.visibility = GONE
        btn_grade.visibility = GONE

        tv_grade_detail_title.visibility = GONE
        tv_grade_detail_hint.visibility = VISIBLE
        tv_grade_detail.visibility = GONE
        btn_grade_detail.visibility = GONE

    }

    private fun setGradeUI(){
        tv_grade_title.visibility = VISIBLE
        tv_grade_hint.visibility = VISIBLE
        tv_grade.visibility = GONE
        btn_grade.visibility = VISIBLE

        tv_grade_detail_title.visibility = GONE
        tv_grade_detail_hint.visibility = VISIBLE
        tv_grade_detail.visibility = GONE
        btn_grade_detail.visibility = GONE

    }

    private fun setGradeDetailUI(){
        tv_grade_detail_title.visibility = VISIBLE
        tv_grade_detail_hint.visibility = VISIBLE
        tv_grade_detail.visibility = GONE
        btn_grade_detail.visibility = VISIBLE
    }

    private fun setNext(key:String){
        var parentCode:String? = null
        when(key){
            MAKER -> {
                parentCode = null
            }

            MODEL -> {
                parentCode = selectedMaker?.makerCd
            }

            MODEL_DETAIL -> {
                parentCode = selectedModel?.modelCd

            }

            GRADE -> {
                parentCode = selectedModelDetail?.modelDetailCd

            }

            GRADE_DETAIL -> {
                parentCode = selectedGrade?.gradeCd
            }

            FUEL -> {
                parentCode = null
            }
        }

        Log.d("testestestsetest","testestestest :: key" + key)
        Log.d("testestestsetest","testestestest :: parentCode" + parentCode)


        apiService().getCharanchaCode("Bearer " + PreferenceUtil.getPref(this@CarDetailActivity, PreferenceUtil.ACCESS_TOKEN, "")!!, key, parentCode).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val jsonString = response.body()?.string()

                val gson = Gson()
                val type: Type = object : TypeToken<List<CarDetailResponse>>() {}.type
                val carDetails:List<CarDetailResponse> = gson.fromJson(jsonString, type)

                val itemList: MutableList<CarDetail> = ArrayList()

                when(key){
                    MAKER -> {
                        if(carDetails.size > 0){
                            for(carDetail in carDetails)
                                itemList.add(CarDetail(carDetail.makerCd, carDetail.makerNm))
                        }
                    }

                    MODEL -> {
                        if(carDetails.size > 0){
                            setModelUI()

                            for(carDetail in carDetails)
                                itemList.add(CarDetail(carDetail.modelCd, carDetail.modelNm))
                        }
                    }

                    MODEL_DETAIL -> {
                        if(carDetails.size > 0){
                            setModelDetailUI()

                            for(carDetail in carDetails)
                                itemList.add(CarDetail(carDetail.modelDetailCd, carDetail.modelDetailNm))
                        }

                    }

                    GRADE -> {
                        if(carDetails.size > 0){
                            setGradeUI()

                            for(carDetail in carDetails)
                                itemList.add(CarDetail(carDetail.gradeCd, carDetail.gradeNm))
                        }


                    }

                    GRADE_DETAIL -> {
                        if(carDetails.size > 0){
                            setGradeDetailUI()

                            for(carDetail in carDetails)
                                itemList.add(CarDetail(carDetail.gradeDetailCd, carDetail.gradeDetailNm))
                        }
                    }

                    FUEL -> {
                        for(carDetail in carDetails)
                            itemList.add(CarDetail(carDetail.code, carDetail.codeNm))
                    }
                }




            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("teestsetestst","testeststestst :: " + t.toString())
            }

        })
    }

}
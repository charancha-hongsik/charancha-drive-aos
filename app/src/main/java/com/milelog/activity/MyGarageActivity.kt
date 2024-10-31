package com.milelog.activity

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.DividerItemDecoration
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.GetMyCarInfoItem
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.room.entity.MyCarsEntity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class MyGarageActivity:BaseRefreshActivity() {
    lateinit var rv_garage: RecyclerView
    lateinit var btn_add:LinearLayout
    lateinit var ib_arrow_register_car: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garage)

        init()

    }

    override fun onResume() {
        super.onResume()
        setMyCarInfo()
    }

    private fun init(){
        rv_garage = findViewById(R.id.rv_garage)
        btn_add = findViewById(R.id.btn_add)
        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)

        rv_garage.layoutManager = LinearLayoutManager(this@MyGarageActivity)
        val dividerItemDecoration = DividerItemDecoration(this@MyGarageActivity, R.color.gray_50, dpToPx(32f)) // 색상 리소스와 구분선 높이 설정
        rv_garage.addItemDecoration(dividerItemDecoration)

        setClickListener()

    }

    private fun setClickListener(){
        btn_add.setOnClickListener {
            apiService().getMyCarCount("Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200 || response.code() == 201){
                        if(response.body()!!.string().toInt() < 10){
                            startActivity(Intent(this@MyGarageActivity, RegisterCarActivity::class.java).putExtra("add",true))
                        }else{
                            Toast.makeText(this@MyGarageActivity, "자동차는 최대 10개까지 추가할 수 있습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }else if(response.code() == 401){
                        logout()
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody>,
                    t: Throwable
                ) {

                }
            })
        }

        ib_arrow_register_car.setOnClickListener {
            finish()
        }
    }

    private fun setMyCarInfo(){
        apiService().getMyCarInfo("Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val getMyCarInfoResponses = Gson().fromJson(jsonString, GetMyCarInfoResponse::class.java)

                    val myCarsListOnServer: MutableList<MyCarsEntity> = mutableListOf()
                    val myCarsListOnDevice:MutableList<MyCarsEntity> = mutableListOf()

                    PreferenceUtil.getPref(this@MyGarageActivity, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                        if(it != "") {
                            val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                            myCarsListOnDevice.addAll(Gson().fromJson(it, type))
                        }
                    }

                    if(getMyCarInfoResponses.items.size > 0){
                        rv_garage.adapter = GarageAdapter(context = this@MyGarageActivity, cars = getMyCarInfoResponses.items.toMutableList())


                        for(car in getMyCarInfoResponses.items){
                            myCarsListOnServer.add(MyCarsEntity(car.id, car.carName, car.licensePlateNumber, null,null))
                        }

                        PreferenceUtil.putPref(this@MyGarageActivity, PreferenceUtil.MY_CAR_ENTITIES, Gson().toJson(updateMyCarList(myCarsListOnServer, myCarsListOnDevice)))

                    }else{
                        startActivity(Intent(this@MyGarageActivity, SplashActivity::class.java))
                        finish()
                    }


                }else{

                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {

            }
        })
    }

    fun updateMyCarList(
        myCarsListOnServer: MutableList<MyCarsEntity>,
        myCarsListOnDevice: MutableList<MyCarsEntity>
    ): MutableList<MyCarsEntity> {
        // 1. 유지할 리스트: 서버에 있는 차량만 남김
        val retainedCars = myCarsListOnDevice.filter { deviceCar ->
            myCarsListOnServer.any { serverCar -> serverCar.id == deviceCar.id }
        }.toMutableList()

        // 2. 추가할 차량: 서버에 있는데 장치에 없는 차량 추가
        val newCarsToAdd = myCarsListOnServer.filterNot { serverCar ->
            myCarsListOnDevice.any { deviceCar -> deviceCar.id == serverCar.id }
        }

        // 3. 새 차량을 유지된 차량 리스트에 추가
        retainedCars.addAll(newCarsToAdd)

        // 업데이트된 리스트 반환
        return retainedCars
    }

    class GarageAdapter(
        private val context: Context,
        private val cars: MutableList<GetMyCarInfoItem>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_garage, parent, false)
            GarageViewHolder(view)
            return GarageViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val car = cars[position]

            (holder as GarageViewHolder).tv_car_no.text = car.licensePlateNumber
            holder.tv_car_name.text = car.carName
            holder.tv_car_info.text = car.releaseDt + " (" + car.modelYear.drop(2) + "년형) " + car.fuelNm

            if(car.modelDetailImageUrl.isNullOrEmpty()){
                holder.linear_default.visibility = VISIBLE
                holder.iv_car.visibility = GONE

            }else{
                holder.linear_default.visibility = GONE
                holder.iv_car.visibility = VISIBLE

                Glide.with(context)
                    .asBitmap()
                    .load(car.modelDetailImageUrl)
                    .error(R.drawable.ic_car_default)
                    .placeholder(R.drawable.ic_car_default)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            holder.iv_car.setImageBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // 이 부분은 Glide가 리소스를 해제할 때 호출됩니다.
                        }
                    })

            }


            holder.btn_edit_car.setOnClickListener {
                (context as MyGarageActivity).apiService().getCarInfoinquiryByCarId("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, car.id).enqueue(object:Callback<ResponseBody>{
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code() == 200 || response.code() == 201){
                            val intent = Intent(context, LoadCarMoreInfoActivity::class.java)
                            intent.putExtra("carInfo",response.body()?.string())
                            intent.putExtra("edit",true)
                            intent.putExtra("carId",car.id)
                            context.startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    }

                })
            }
        }

        override fun getItemCount(): Int {
            return cars.size
        }
    }

    class GarageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv_car: ImageView = view.findViewById(R.id.iv_car)
        val tv_car_no: TextView = view.findViewById(R.id.tv_car_no)
        val tv_car_name: TextView = view.findViewById(R.id.tv_car_name)
        val tv_car_info: TextView = view.findViewById(R.id.tv_car_info)
        val btn_edit_car:ImageView = view.findViewById(R.id.btn_edit_car)
        val btn_car_more_info: TextView = view.findViewById(R.id.btn_car_more_info)
        val linear_default:LinearLayout = view.findViewById(R.id.linear_default)
    }
}
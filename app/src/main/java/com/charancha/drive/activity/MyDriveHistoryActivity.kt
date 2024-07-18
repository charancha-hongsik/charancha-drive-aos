package com.charancha.drive.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.charancha.drive.ChosenDate
import com.charancha.drive.DriveHistroyData
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.viewmodel.MyDriveHistoryViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyDriveHistoryActivity: BaseActivity() {
    lateinit var lvHistory:ListView
    lateinit var btn_filter: ImageView
    lateinit var btn_back:ImageView
    private val historyViewModel: MyDriveHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_history)

        init()
    }

    fun init(){
        lvHistory = findViewById(R.id.lv_history)
        btn_filter = findViewById(R.id.btn_filter)
        btn_back = findViewById(R.id.btn_back)

        historyViewModel.init(applicationContext)
        historyViewModel.setAllDriveDateForApp.observe(this@MyDriveHistoryActivity, MyDriveHistoryViewModel.EventObserver {
            var id_list:MutableList<String> = mutableListOf()
            for(drive in it) {
                id_list.add(
                    drive.tracking_id
                )
            }

            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, id_list)

            lvHistory.setOnItemClickListener { adapterView, view, i, l ->
                var intent = Intent(this@MyDriveHistoryActivity, DetailDriveHistoryActivity::class.java)
                intent.putExtra("tracking_id", it[i].tracking_id)
                startActivity(intent)
            }

            // listView에 adapter 연결
            lvHistory.setAdapter(adapter)
        })

        historyViewModel.getAllDrive()
    }

    fun getHistories(){
        apiService().getDrivingHistories(
            "Bearer " + PreferenceUtil.getPref(this@MyDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            10,
            "ASC",
            null,
            null,
            "startTime",
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    class DriveHistoryAdapter(context: Context, histories: List<DriveHistroyData>, val callback:DateCallback ) : ArrayAdapter<DriveHistroyData>(context, 0, histories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItemView = convertView
            if (listItemView == null) {
                listItemView = LayoutInflater.from(context).inflate(R.layout.item_drive_history, parent, false)
            }

            val chosenDate = getItem(position)

            val tvName = listItemView!!.findViewById<TextView>(R.id.tv_date)
            tvName.text = chosenDate?.date
            chosenDate?.selected?.let {
                tvName.isSelected = it

                if(it){
                    TextViewCompat.setTextAppearance(tvName, R.style.B1SBweight600)
                }else{
                    TextViewCompat.setTextAppearance(tvName, R.style.B1RWeight400)

                }
            }

            tvName.setOnClickListener {
                callback.chosenDate(tvName.text.toString())
            }



            return listItemView
        }

        interface DateCallback {
            fun chosenDate(date:String)

        }
    }
}
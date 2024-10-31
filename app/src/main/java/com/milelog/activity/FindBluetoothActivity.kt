package com.milelog.activity

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.FindBluetoothEntity
import com.milelog.PreferenceUtil
import com.milelog.PreferenceUtil.MYCAR
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.room.entity.MyCarsEntity

class FindBluetoothActivity: BaseRefreshActivity() {
    lateinit var rv_find_bluetooth:RecyclerView
    lateinit var btn_hands_free:TextView
    lateinit var getMyCarInfoResponses:List<GetMyCarInfoResponse>
    var handsfreeStatus:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_bluetooth)

        rv_find_bluetooth = findViewById(R.id.rv_find_bluetooth)
        btn_hands_free = findViewById(R.id.btn_hands_free)
        btn_hands_free.setOnClickListener {
            setBluetoothList()
            handsfreeStatus = !handsfreeStatus
        }

        rv_find_bluetooth.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, R.color.gray_50, dpToPx(12f)) // 색상 리소스와 구분선 높이 설정
        rv_find_bluetooth.addItemDecoration(dividerItemDecoration)

        setBluetoothList()
        handsfreeStatus = !handsfreeStatus

    }

    private fun setBluetoothList(){
        if (ActivityCompat.checkSelfPermission(
                this@FindBluetoothActivity,
                BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

            val devices:MutableList<FindBluetoothEntity> = mutableListOf()

            pairedDevices?.forEach { device ->
                Log.d("testestsetset","testsetsetestse deviceClass " + device.bluetoothClass.deviceClass)
                Log.d("testestsetset","testsetsetestse majorDeviceClass " + device.bluetoothClass.majorDeviceClass)
                Log.d("testestsetset","testsetsetestse type " + device.type)

                if(handsfreeStatus){
                    if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE)
                        devices.add(FindBluetoothEntity(device.name, device.address, device.bluetoothClass.deviceClass))
                }else{
                    devices.add(FindBluetoothEntity(device.name, device.address, device.bluetoothClass.deviceClass))
                }

            }

            rv_find_bluetooth.adapter = DetectedStatusAdapter(context = this, findBluetoothEntity = devices)
        }
    }




    class DetectedStatusAdapter(
        private val context: Context,
        private val findBluetoothEntity: MutableList<FindBluetoothEntity>,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_find_bluetooth, parent, false)
            return DetectedStatusViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DetectedStatusViewHolder) {
                val userEntity = findBluetoothEntity[position]

                holder.tv_find_bluetooth_text1.text = userEntity.name
//                holder.tv_find_bluetooth_text2.text = userEntity.macAdress

                val profileStr = when (userEntity.major) {
                    1076 -> "Audio/Video: Camcorder"
                    1056 -> "Audio/Video: Car Audio"
                    1032 -> "Audio/Video: Handsfree"
                    1048 -> "Audio/Video: Headphones"
                    1064 -> "Audio/Video: HiFi Audio"
                    1044 -> "Audio/Video: Loudspeaker"
                    1040 -> "Audio/Video: Microphone"
                    1052 -> "Audio/Video: Portable Audio"
                    1060 -> "Audio/Video: Set Top Box"
                    1024 -> "Audio/Video: Uncategorized"
                    1068 -> "Audio/Video: VCR"
                    1072 -> "Audio/Video: Video Camera"
                    1088 -> "Audio/Video: Video Conferencing"
                    1084 -> "Audio/Video: Video Display and Loudspeaker"
                    1096 -> "Audio/Video: Video Gaming Toy"
                    1080 -> "Audio/Video: Video Monitor"
                    1028 -> "Audio/Video: Wearable Headset"
                    260 -> "Computer: Desktop"
                    272 -> "Computer: Handheld PC/PDA"
                    268 -> "Computer: Laptop"
                    276 -> "Computer: Palm Size PC/PDA"
                    264 -> "Computer: Server"
                    256 -> "Computer: Uncategorized"
                    280 -> "Computer: Wearable"
                    2308 -> "Health: Blood Pressure"
                    2332 -> "Health: Data Display"
                    2320 -> "Health: Glucose"
                    2324 -> "Health: Pulse Oximeter"
                    2328 -> "Health: Pulse Rate"
                    2312 -> "Health: Thermometer"
                    2304 -> "Health: Uncategorized"
                    2316 -> "Health: Weighing"
                    1344 -> "Peripheral: Keyboard"
                    1472 -> "Peripheral: Keyboard/Pointing"
                    1280 -> "Peripheral: Non Keyboard/Non Pointing"
                    1408 -> "Peripheral: Pointing"
                    516 -> "Phone: Cellular"
                    520 -> "Phone: Cordless"
                    532 -> "Phone: ISDN"
                    528 -> "Phone: Modem or Gateway"
                    524 -> "Phone: Smart"
                    512 -> "Phone: Uncategorized"
                    2064 -> "Toy: Controller"
                    2060 -> "Toy: Doll/Action Figure"
                    2068 -> "Toy: Game"
                    2052 -> "Toy: Robot"
                    2048 -> "Toy: Uncategorized"
                    2056 -> "Toy: Vehicle"
                    1812 -> "Wearable: Glasses"
                    1808 -> "Wearable: Helmet"
                    1804 -> "Wearable: Jacket"
                    1800 -> "Wearable: Pager"
                    1792 -> "Wearable: Uncategorized"
                    1796 -> "Wearable: Wrist Watch"
                    else -> "Unknown"
                }

                holder.tv_find_bluetooth_text3.text = profileStr

                holder.tv_find_bluetooth_text1.setOnClickListener {
                    showBottomSheetForEditCar(userEntity.macAdress, userEntity.name)
                }
            }
        }

        fun showBottomSheetForEditCar(macAddress: String, bluetoothName:String) {
            PreferenceUtil.getPref(context as FindBluetoothActivity, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                if(it != ""){
                    // Create a BottomSheetDialog
                    val bottomSheetDialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialog)

                    // Inflate the layout
                    val bottomSheetView = context.layoutInflater.inflate(R.layout.dialog_registered_bluetooth, null)
                    val rv_registered_car = bottomSheetView.findViewById<RecyclerView>(R.id.rv_registered_car)

                    rv_registered_car.layoutManager = LinearLayoutManager(context)
                    val dividerItemDecoration = DividerItemDecoration(context, R.color.gray_50, context.dpToPx(12f)) // 색상 리소스와 구분선 높이 설정
                    rv_registered_car.addItemDecoration(dividerItemDecoration)

                    val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                    val myCarsList: MutableList<MyCarsEntity> = Gson().fromJson(it, type)

                    rv_registered_car.adapter = MyCarEntitiesAdapter(context = context, mycarEntities = myCarsList, macAddress,bluetoothName )

                    // Set the content view of the dialog
                    bottomSheetDialog.setContentView(bottomSheetView)

                    // Show the dialog
                    bottomSheetDialog.show()
                }
            }
        }

        override fun getItemCount(): Int {
            return findBluetoothEntity.size
        }

    }

    class DetectedStatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_find_bluetooth_text1:TextView  = view.findViewById(R.id.tv_find_bluetooth_text1)
        val tv_find_bluetooth_text3:TextView  = view.findViewById(R.id.tv_find_bluetooth_text3)

    }

    class MyCarEntitiesAdapter(
        private val context: Context,
        private val mycarEntities: MutableList<MyCarsEntity>,
        private val macAddress:String,
        private val bluetoothName:String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_registered_bluetooth, parent, false)
            return MyCarEntitiesHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyCarEntitiesHolder) {
                val myCarsEntity = mycarEntities[position]

                holder.tv_car_name.text = myCarsEntity.name + " " +myCarsEntity.number + "-> " + myCarsEntity.bluetooth_name
                holder.tv_car_name.setOnClickListener {

                    PreferenceUtil.getPref(context, PreferenceUtil.MY_CAR_ENTITIES,"")?.let {
                        if (it != "") {
                            val myCarsListOnDevice:MutableList<MyCarsEntity> = mutableListOf()
                            val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                            myCarsListOnDevice.addAll(Gson().fromJson(it, type))

                            myCarsListOnDevice.forEach { car ->
                                if (car.bluetooth_mac_address == macAddress) {
                                    car.bluetooth_mac_address = null
                                    car.bluetooth_name = null
                                }
                            }

                            myCarsListOnDevice.get(position).bluetooth_mac_address = macAddress
                            myCarsListOnDevice.get(position).bluetooth_name = bluetoothName

                            PreferenceUtil.putPref(context, PreferenceUtil.MY_CAR_ENTITIES, Gson().toJson(myCarsListOnDevice))
                        }
                    }

                    PreferenceUtil.putPref(context, MYCAR, macAddress)

                    Toast.makeText(context,
                        myCarsEntity.name +"차량이 " + bluetoothName + " 블루투스와 연결됐어요." , Toast.LENGTH_SHORT).show()
                }

            }
        }

        override fun getItemCount(): Int {
            return mycarEntities.size
        }
    }

    class MyCarEntitiesHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_name:TextView  = view.findViewById(R.id.tv_car_name)
    }
}
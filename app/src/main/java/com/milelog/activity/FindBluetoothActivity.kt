package com.milelog.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.POST_NOTIFICATIONS
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CustomDialog
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.FindBluetoothEntity
import com.milelog.PreferenceUtil
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.CORPORATE
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.room.entity.MyCarsEntity

class FindBluetoothActivity: BaseRefreshActivity() {
    lateinit var rv_find_bluetooth:RecyclerView
    lateinit var rv_connected_car:RecyclerView
    lateinit var btn_hands_free:TextView
    lateinit var layout_bluetooth:LinearLayout
    lateinit var layout_no_bluetooth:LinearLayout
    lateinit var btn_find_bluetooth:TextView
    lateinit var getMyCarInfoResponses:List<GetMyCarInfoResponse>
    var handsfreeStatus:Boolean = false
    lateinit var btn_back:ImageView
    var permissionState = false
    lateinit var btn_find_bluetooth2:TextView
    lateinit var layout_no_find_bluetooth:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_bluetooth)

        rv_find_bluetooth = findViewById(R.id.rv_find_bluetooth)
        layout_bluetooth = findViewById(R.id.layout_bluetooth)
        layout_no_bluetooth = findViewById(R.id.layout_no_bluetooth)
        rv_connected_car = findViewById(R.id.rv_connected_car)
        btn_find_bluetooth = findViewById(R.id.btn_find_bluetooth)
        btn_find_bluetooth2 = findViewById(R.id.btn_find_bluetooth2)
        layout_no_find_bluetooth = findViewById(R.id.layout_no_find_bluetooth)
        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener {
            finish()
        }
        val dividerItemDecoration = DividerItemDecoration(this, R.color.white_op_100, dpToPx(12f)) // 색상 리소스와 구분선 높이 설정

        rv_find_bluetooth.layoutManager = LinearLayoutManager(this)
        rv_find_bluetooth.addItemDecoration(dividerItemDecoration)

        rv_connected_car.layoutManager = LinearLayoutManager(this)
        rv_connected_car.addItemDecoration(dividerItemDecoration)

        btn_find_bluetooth.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                setList()
            }

        })

        btn_find_bluetooth2.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                setList()
            }

        })
        setList()
    }

    override fun onResume() {
        super.onResume()
        if(permissionState){
            if(ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
                setList()
                permissionState = false
            }else{
                setNoPermissionUI()
            }
        }
    }


    private fun setList(){
        if(ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission(mutableListOf(
                    BLUETOOTH_CONNECT
                ).apply {

                }.toTypedArray(),0)
            }else{
                setHasPermissionUI()
            }
        }else{
            setHasPermissionUI()
        }
    }

    private fun setConnectedCarList(){
        PreferenceUtil.getPref(this, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
            if(it != ""){
                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                var myCarsList: List<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)
                myCarsList = myCarsList.filterNot { it.bluetooth_mac_address.isNullOrEmpty() }

                rv_connected_car.adapter = ConnectedCarAdapter(context = this, mycarEntities = myCarsList.toMutableList())

            }
        }
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
                if(handsfreeStatus){
                    if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE)
                        devices.add(FindBluetoothEntity(device.name, device.address, device.bluetoothClass.deviceClass))
                }else{
                    devices.add(FindBluetoothEntity(device.name, device.address, device.bluetoothClass.deviceClass))
                }

            }

            rv_find_bluetooth.adapter = DetectedStatusAdapter(context = this, findBluetoothEntity = devices)

            Toast.makeText(this, "등록 가능한 블루투스 기기를 불러왔어요.", Toast.LENGTH_SHORT).show()
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


                holder.tv_find_bluetooth_text1.setOnClickListener {
                    showBottomSheetForEditCar(userEntity.macAdress, userEntity.name)
                }

                holder.btn_find_bluetooth_text.setOnClickListener {
                    showBottomSheetForEditCar(userEntity.macAdress, userEntity.name)
                }
            }
        }

        fun showBottomSheetForEditCar(macAddress: String, bluetoothName:String) {
            PreferenceUtil.getPref(context as FindBluetoothActivity, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                if(it != ""){

                    val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                    val myCarsList: MutableList<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)

                    Log.d("testeststest","testestestes :: " + myCarsList.size)


                    if(myCarsList.size > 1){
                        // Create a BottomSheetDialog
                        val bottomSheetDialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialog)

                        // Inflate the layout
                        val bottomSheetView = context.layoutInflater.inflate(R.layout.dialog_registered_bluetooth, null)
                        val rv_registered_car = bottomSheetView.findViewById<RecyclerView>(R.id.rv_registered_car)

                        rv_registered_car.layoutManager = LinearLayoutManager(context)
                        val dividerItemDecoration = DividerItemDecoration(context, R.color.white_op_100, context.dpToPx(8f)) // 색상 리소스와 구분선 높이 설정
                        rv_registered_car.addItemDecoration(dividerItemDecoration)


                        rv_registered_car.adapter = MyCarEntitiesAdapter(context = context, mycarEntities = myCarsList, macAddress,bluetoothName, bottomSheetDialog)

                        // Set the content view of the dialog
                        bottomSheetDialog.setContentView(bottomSheetView)

                        // Show the dialog
                        bottomSheetDialog.show()
                    }else{
                        myCarsList.first().bluetooth_mac_address = macAddress
                        myCarsList.first().bluetooth_name = bluetoothName

                        PreferenceUtil.putPref(context, PreferenceUtil.MY_CAR_ENTITIES, GsonBuilder().serializeNulls().create().toJson(myCarsList))

                        context.setConnectedCarList()

                        Toast.makeText(context,
                            myCarsList.first().name +"블루투스가 연결됐어요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return findBluetoothEntity.size
        }

    }

    class DetectedStatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_find_bluetooth_text1:TextView  = view.findViewById(R.id.tv_find_bluetooth_text1)
        val btn_find_bluetooth_text:LinearLayout = view.findViewById(R.id.btn_find_bluetooth_text)

    }

    class MyCarEntitiesAdapter(
        private val context: Context,
        private val mycarEntities: MutableList<MyCarsEntity>,
        private val macAddress:String,
        private val bluetoothName:String,
        private val bottomSheetDialog: BottomSheetDialog
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_connected_bluetooth, parent, false)
            return MyCarEntitiesHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyCarEntitiesHolder) {
                val myCarsEntity = mycarEntities[position]

                holder.tv_no_mycar.visibility = GONE
                holder.tv_car_name.text = myCarsEntity.fullName
                holder.tv_car_no.text = myCarsEntity.number

                myCarsEntity.type?.let{
                    if(it.equals(CORPORATE)){
                        holder.iv_corp.visibility = VISIBLE
                    }else{
                        holder.iv_corp.visibility = GONE

                    }
                }

                holder.layout_car.setOnClickListener {

                    CustomDialog(context, "블루투스 변경", "등록된 블루투스 기기를 \n변경하시겠습니까?", "변경","취소",  object : CustomDialog.DialogCallback{
                        override fun onConfirm() {
                            PreferenceUtil.getPref(context, PreferenceUtil.MY_CAR_ENTITIES,"")?.let {
                                if (it != "") {
                                    val myCarsListOnDevice:MutableList<MyCarsEntity> = mutableListOf()
                                    val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                                    myCarsListOnDevice.addAll(GsonBuilder().serializeNulls().create().fromJson(it, type))

                                    myCarsListOnDevice.forEach { car ->
                                        if (car.bluetooth_mac_address == macAddress) {
                                            car.bluetooth_mac_address = null
                                            car.bluetooth_name = null
                                        }
                                    }

                                    myCarsListOnDevice.get(position).bluetooth_mac_address = macAddress
                                    myCarsListOnDevice.get(position).bluetooth_name = bluetoothName

                                    PreferenceUtil.putPref(context, PreferenceUtil.MY_CAR_ENTITIES, GsonBuilder().serializeNulls().create().toJson(myCarsListOnDevice))

                                    (context as FindBluetoothActivity).setConnectedCarList()

                                    Toast.makeText(context,
                                        myCarsEntity.name +"블루투스가 연결됐어요", Toast.LENGTH_SHORT).show()

                                    bottomSheetDialog.dismiss()
                                }
                            }
                        }

                        override fun onCancel() {

                        }

                    }).show()
                }

            }
        }

        override fun getItemCount(): Int {
            return mycarEntities.size
        }
    }

    class MyCarEntitiesHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_name:TextView  = view.findViewById(R.id.tv_car_name)
        val iv_corp: ImageView = view.findViewById(R.id.iv_corp)
        val tv_car_no:TextView = view.findViewById(R.id.tv_car_no)
        val tv_no_mycar:TextView = view.findViewById(R.id.tv_no_mycar)
        val layout_car:LinearLayout = view.findViewById(R.id.layout_car)
    }

    class ConnectedCarAdapter(
        private val context: Context,
        private var mycarEntities: MutableList<MyCarsEntity>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
        }

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_connected_car, parent, false)
            return ConnectedCarHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ConnectedCarHolder) {
                val myCarsEntity = mycarEntities[position]

                holder.tv_car_name.text = myCarsEntity.fullName
                holder.tv_car_number.text = myCarsEntity.number
                holder.tv_car_bluetooth_name.text = myCarsEntity.bluetooth_name

                holder.btn_delete.setOnClickListener {
                    CustomDialog(context, "블루투스 삭제", "등록된 블루투스 기기를 \n삭제하시겠습니까?", "삭제","취소",  object : CustomDialog.DialogCallback{
                        override fun onConfirm() {
                            PreferenceUtil.getPref(context, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                                if(it != ""){

                                    val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                                    var myCarsList: List<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)

                                    val myCarListForEdit = myCarsList.find { myCarsEntity.bluetooth_mac_address.equals(it.bluetooth_mac_address) }

                                    myCarListForEdit?.bluetooth_name = null
                                    myCarListForEdit?.bluetooth_mac_address = null

                                    PreferenceUtil.putPref(context, PreferenceUtil.MY_CAR_ENTITIES, GsonBuilder().serializeNulls().create().toJson(myCarsList))

                                    (context as FindBluetoothActivity).setConnectedCarList()
                                }
                            }
                        }

                        override fun onCancel() {

                        }

                    }).show()
                }

                myCarsEntity.type?.let{
                    if(it.equals(CORPORATE)){
                        holder.layout_corp.visibility = VISIBLE
                    }else{
                        holder.layout_corp.visibility = GONE
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return mycarEntities.size
        }
    }

    class ConnectedCarHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_car_name:TextView  = view.findViewById(R.id.tv_car_name)
        val tv_car_number:TextView = view.findViewById(R.id.tv_car_number)
        val tv_car_bluetooth_name:TextView = view.findViewById(R.id.tv_car_bluetooth_name)

        val layout_corp: LinearLayout = view.findViewById(R.id.layout_corp)
        val btn_delete:ImageView = view.findViewById(R.id.btn_delete)
    }

    private fun checkPermission(permissions: Array<String>, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions,code)
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            setHasPermissionUI()
        } else {
            CustomDialog(
                this,
                "블루투스",
                "동작 및 피트니스 서비스를 사용할 수 없습니다. 기기의 ‘설정 > 개인정보 보호'에서 동작 및 피트니스 서비스를 켜주세요 (필수 권한)",
                "설정으로 이동",
                "취소",
                object : CustomDialog.DialogCallback {
                    override fun onConfirm() {
                        val openSettingsIntent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                data = uri
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        permissionState = true
                        startActivity(openSettingsIntent)
                    }

                    override fun onCancel() {
                        setNoPermissionUI()
                    }

                }).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setNoPermissionUI(){
        PreferenceUtil.getPref(this@FindBluetoothActivity, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
            if(it != ""){
                val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                var myCarsList: List<MyCarsEntity> = GsonBuilder().serializeNulls().create().fromJson(it, type)
                myCarsList = myCarsList.filterNot { it.bluetooth_mac_address.isNullOrEmpty() }

                if(myCarsList.size > 0){
                    setBluetoothList()
                    setConnectedCarList()

                    layout_no_bluetooth.visibility = GONE
                    layout_bluetooth.visibility = VISIBLE
                    rv_find_bluetooth.visibility = VISIBLE
                    layout_no_find_bluetooth.visibility = GONE
                }else{
                    layout_no_bluetooth.visibility = VISIBLE
                    layout_bluetooth.visibility = GONE
                    rv_find_bluetooth.visibility = GONE
                    layout_no_find_bluetooth.visibility = VISIBLE
                }


            }
        }
    }

    private fun setHasPermissionUI(){
        setBluetoothList()
        setConnectedCarList()

        layout_no_bluetooth.visibility = GONE
        layout_bluetooth.visibility = VISIBLE
        rv_find_bluetooth.visibility = VISIBLE
        layout_no_find_bluetooth.visibility = GONE
    }

}
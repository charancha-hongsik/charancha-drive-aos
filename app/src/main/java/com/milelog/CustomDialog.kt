package com.milelog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.milelog.activity.BaseActivity

class CustomDialog(context: Context, val title:String? = null, val contents:String, val confirmBtnText:String, val cancelBtnText:String, val dialogCallback: DialogCallback): Dialog(context) {
    lateinit var layout_confirm:ConstraintLayout
    lateinit var layout_cancel:ConstraintLayout
    lateinit var tv_dialog_title:TextView
    lateinit var tv_dialog_contents:TextView
    lateinit var tv_cancel:TextView
    lateinit var tv_comfirm:TextView

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom)

        layout_confirm = findViewById(R.id.layout_confirm)
        layout_cancel = findViewById(R.id.layout_cancel)
        tv_dialog_title = findViewById(R.id.tv_dialog_title)
        tv_dialog_contents = findViewById(R.id.tv_dialog_contents)
        tv_comfirm = findViewById(R.id.tv_comfirm)
        tv_cancel = findViewById(R.id.tv_cancel)


        layout_confirm.setOnClickListener(object :BaseActivity.OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                dialogCallback.onConfirm()
                dismiss()
            }
        })

        layout_cancel.setOnClickListener(object:BaseActivity.OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                dialogCallback.onCancel()
                dismiss()
            }
        })

        if(title != null){
            tv_dialog_title.visibility = VISIBLE
            tv_dialog_title.text = title
        }else{
            tv_dialog_title.visibility = GONE
        }
        tv_dialog_contents.text = contents
        tv_comfirm.text = confirmBtnText
        tv_cancel.text = cancelBtnText

    }

    interface DialogCallback {
        fun onConfirm()
        fun onCancel()
    }



}
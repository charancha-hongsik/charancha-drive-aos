package com.charancha.drive

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class CustomDialogNoCancel(context: Context, val title:String, val contents:String, val confirmBtnText:String, val dialogCallback: DialogCallback): Dialog(context) {
    lateinit var layout_confirm:ConstraintLayout
    lateinit var tv_dialog_title:TextView
    lateinit var tv_dialog_contents:TextView
    lateinit var tv_comfirm:TextView

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom_no_cancel)

        layout_confirm = findViewById(R.id.layout_confirm)
        tv_dialog_title = findViewById(R.id.tv_dialog_title)
        tv_dialog_contents = findViewById(R.id.tv_dialog_contents)
        tv_comfirm = findViewById(R.id.tv_comfirm)


        layout_confirm.setOnClickListener {
            dialogCallback.onConfirm()
            dismiss()
        }

        tv_dialog_title.text = title
        tv_dialog_contents.text = contents
        tv_comfirm.text = confirmBtnText

    }

    interface DialogCallback {
        fun onConfirm()
    }



}
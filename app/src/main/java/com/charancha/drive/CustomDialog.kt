package com.charancha.drive

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class CustomDialog(context: Context, val title:String, val contents:String, val confirmBtnText:String, val cancelBtnText:String, val dialogCallback: DialogCallback): Dialog(context) {
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


        layout_confirm.setOnClickListener {
            dialogCallback.onConfirm()
            dismiss()
        }

        layout_cancel.setOnClickListener {
            dialogCallback.onCancel()
            dismiss()
        }

        tv_dialog_title.text = title
        tv_dialog_contents.text = contents
        tv_comfirm.text = confirmBtnText
        tv_cancel.text = cancelBtnText

    }

    interface DialogCallback {
        fun onConfirm()
        fun onCancel()
    }



}
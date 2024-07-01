package com.charancha.drive

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout

class CustomDialog(context: Context, val dialogCallback: DialogCallback): Dialog(context) {
    lateinit var layout_confirm:ConstraintLayout
    lateinit var layout_cancel:ConstraintLayout



    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom)

        layout_confirm = findViewById(R.id.layout_confirm)
        layout_cancel = findViewById(R.id.layout_cancel)

        layout_confirm.setOnClickListener {
            dialogCallback.onConfirm()
            dismiss()
        }

        layout_cancel.setOnClickListener {
            dialogCallback.onCancel()
            dismiss()
        }
    }

    interface DialogCallback {
        fun onConfirm()
        fun onCancel()
    }



}
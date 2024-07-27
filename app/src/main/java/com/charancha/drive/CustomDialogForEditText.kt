package com.charancha.drive

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class CustomDialogForEditText(context: Context, val title:String, val subtitle:String, val hint:String, val confirmBtnText:String, val cancelBtnText:String, val dialogCallback: DialogCallback): Dialog(context) {
    lateinit var layout_confirm:ConstraintLayout
    lateinit var layout_cancel:ConstraintLayout
    lateinit var tv_dialog_title:TextView
    lateinit var tv_dialog_subtitle:TextView
    lateinit var tv_cancel:TextView
    lateinit var tv_comfirm:TextView
    lateinit var et_dialog_contents: EditText

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom_for_et)

        layout_confirm = findViewById(R.id.layout_confirm)
        layout_cancel = findViewById(R.id.layout_cancel)
        tv_dialog_title = findViewById(R.id.tv_dialog_title)
        tv_dialog_subtitle = findViewById(R.id.tv_dialog_subtitle)
        tv_comfirm = findViewById(R.id.tv_comfirm)
        tv_cancel = findViewById(R.id.tv_cancel)
        et_dialog_contents = findViewById(R.id.et_dialog_contents)



        layout_confirm.setOnClickListener {
            if(et_dialog_contents.text.isNotEmpty()){
                dialogCallback.onConfirm(et_dialog_contents.text.toString())
                dismiss()
            }else{
                showCustomToast(context, "1글자 이상 입력하세요.")
            }
        }

        layout_cancel.setOnClickListener {
            dialogCallback.onCancel()
            dismiss()
        }

        tv_dialog_title.text = title
        tv_dialog_subtitle.text = subtitle
        tv_comfirm.text = confirmBtnText
        tv_cancel.text = cancelBtnText
        et_dialog_contents.hint = hint

        et_dialog_contents.setOnFocusChangeListener { view, b ->
            if(b){
                et_dialog_contents.hint = ""
                showKeyboard(et_dialog_contents)
            }else{

            }
        }

        et_dialog_contents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let{
                    if(p0.length>=7){

                    }else{

                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })



    }

    interface DialogCallback {
        fun onConfirm(contents:String)
        fun onCancel()
    }

    fun showKeyboard(editText: EditText) {
        // EditText에 포커스 주기
        editText.requestFocus()

        // InputMethodManager를 통해 키보드 올리기
        val imm = editText.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun showCustomToast(context: Context, message: String) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_toast, null)

        // Set the text in the custom layout
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message

        // Create and display the toast
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }



}
package com.example.lostarkupgradeapplication

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast

class CustomToast(private val context: Context) {
    lateinit var toast: Toast

    fun createToast(message: String, isLength: Boolean) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null)

        val txtContent = view.findViewById<TextView>(R.id.txtContent)
        txtContent.text = message

        toast = Toast(context)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.setGravity(Gravity.BOTTOM, 0, 200)
        if (isLength) {
            toast.duration = Toast.LENGTH_LONG
        } else {
            toast.duration = Toast.LENGTH_SHORT
        }
        toast.view = view
    }

    fun show() {
        if (toast != null) {
            toast.show()
        }
    }
}
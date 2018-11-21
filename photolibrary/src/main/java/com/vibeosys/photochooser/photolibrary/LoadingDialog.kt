package com.vibeosys.photochooser.photolibrary

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import android.view.Gravity
import android.R.attr.gravity



class LoadingDialog(var context:Context){

    var loadingDialog:Dialog?=null

    init {

        loadingDialog = Dialog(context)
        loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog?.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        loadingDialog?.setCancelable(false)
        loadingDialog?.setContentView(R.layout.loading_dialog)

    }

    open fun show()
    {
        loadingDialog?.show()
    }
    open fun dismiss()
    {
        loadingDialog?.dismiss()
    }


}
package com.vibeosys.photochooser.photolibrary

import android.view.View
import android.widget.ImageView

interface ImageClickListener {

    fun imageClick(filePath:String,imageView: View)
}
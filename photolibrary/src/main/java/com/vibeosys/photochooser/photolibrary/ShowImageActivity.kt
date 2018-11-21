package com.vibeosys.photochooser.photolibrary

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import java.io.*

import com.bumptech.glide.Glide

class ShowImageActivity : AppCompatActivity() {

    private var imageView: ImageView? = null
    private var correctBtn: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)
        imageView = findViewById<ImageView>(R.id.imageView) as ImageView
        correctBtn = findViewById<ImageView>(R.id.correctBtn) as ImageView

        val filePath = intent.getStringExtra("myImage")
        Glide.with(this).load(File(filePath)).fitCenter().into(imageView!!)

        correctBtn?.setOnClickListener {
            val intent = Intent()
            intent.putExtra(TakePictureActivity.FILE_PATH, filePath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }
}

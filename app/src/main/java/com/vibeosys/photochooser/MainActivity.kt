package com.vibeosys.photochooser


import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.vibeosys.photochooser.photolibrary.TakePictureActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gotoNext.setOnClickListener {
            startActivityForResult(Intent(this, TakePictureActivity::class.java), TakePictureActivity.REQUEST_ID)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TakePictureActivity.REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
            val filePath =data?.getStringExtra(TakePictureActivity.FILE_PATH)
            Glide.with(this).load(File(filePath)).fitCenter().into(image!!)
        }



    }
}

package com.vibeosys.photochooser

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import java.util.*
import kotlin.collections.ArrayList

class Main2Activity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
       getAllImages()

    }

    private fun getAllImages() {
        val imageList = ArrayList<String>()
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)

            var size = cursor.count

            if (size == 0) {

            } else {
                var thumbId = 0
                while (cursor.moveToNext()) {
                    val columIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val path = cursor.getString(columIndex)
                    imageList.add(path)
                    var fileName = path.substring(path.lastIndexOf("/") + 1, path.length)

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

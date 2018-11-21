package com.vibeosys.photochooser.photolibrary

import android.content.ContentResolver
import android.provider.MediaStore
import java.util.*

class GetImageList {

    companion object {
        open fun getAllImages(contentResolver:ContentResolver):ArrayList<String> {
            val imageList = ArrayList<String>()
            try {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                var cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC")

                var size = cursor.count

                if (size == 0) {

                } else {
                    var thumbId = 0
                    while (cursor.moveToNext()) {
                        val columIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                        val path = cursor.getString(columIndex)
                        imageList.add(path)

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
           return  imageList
        }
    }
}
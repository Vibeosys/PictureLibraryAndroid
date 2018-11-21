package com.vibeosys.photochooser.photolibrary

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.File


import java.util.*


/**
 * Created by chetan on 17-04-2018.
 */

class ImagesAdapter(context: Context, imageList: ArrayList<String>,var imageClickListener: ImageClickListener) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    private val mContext = context
    private var mImageList = imageList
    var isUp = false;

    fun setIsUp(isUp: Boolean) {

        this.isUp = isUp
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var imageView = v.findViewById<ImageView>(R.id.imageView)


    }

    override fun onBindViewHolder(viewHolder: ImagesAdapter.ViewHolder, position: Int) {

        if (isUp) {
            val params = viewHolder.imageView.getLayoutParams() as ViewGroup.MarginLayoutParams
            params.width = getHeightInDp(130, mContext)
            viewHolder.imageView.layoutParams = params

        } else {
         slideDown(view =viewHolder.imageView)
        }

        try {
            Glide.with(mContext).load(File(mImageList.get(position))).fitCenter().into(viewHolder.imageView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewHolder.imageView?.setOnClickListener {
            imageClickListener.imageClick( mImageList.get(position),it)
        }
    }

    fun slideDown(view: View) {

        val anim = ValueAnimator.ofInt(view.measuredWidth, AnimUtilClass.getHeightInDp(80, view.context))
        anim.addUpdateListener { valueAnimator ->
            val params = view.getLayoutParams() as ViewGroup.MarginLayoutParams
            params.width = getHeightInDp(80, mContext)
            view.layoutParams = params
        }
        anim.duration = 700
        anim.start()
    }

    fun slideUp(view: View) {

        val anim = ValueAnimator.ofInt(view.measuredWidth, AnimUtilClass.getHeightInDp(130, view.context))
        anim.addUpdateListener { valueAnimator ->
            val params = view.getLayoutParams() as ViewGroup.MarginLayoutParams
            params.width = getHeightInDp(130, mContext)
            view.layoutParams = params
        }
        anim.duration = 700
        anim.start()
    }

    fun getHeightInDp(dps: Int, context: Context): Int {
        val scale = context.getResources().getDisplayMetrics().density
        val pixels = (dps * scale + 0.5f)
        return pixels.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.item_image, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return mImageList.size
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }


}
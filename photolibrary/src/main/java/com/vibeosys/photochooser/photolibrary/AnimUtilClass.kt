package com.vibeosys.photochooser.photolibrary

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import android.animation.ValueAnimator
import android.content.Context
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.util.DisplayMetrics






class AnimUtilClass {

    companion object {

        fun slideLeft(view: View) {
            view.setVisibility(View.VISIBLE)
            val animate = TranslateAnimation(
                    view.width.toFloat()-300, // fromXDelta
                    0f, // toXDelta
                    0f, // fromYDelta
                    0f)                // toYDelta
            animate.duration = 500
            animate.fillAfter = true
            view.startAnimation(animate)
        }

        fun slideRight(view: View) {
            view.setVisibility(View.VISIBLE)
            val animate = TranslateAnimation(
                    0f, // fromXDelta
                    view.width.toFloat(), // toXDelta
                    0f, // fromYDelta
                    0f) // toYDelta
            animate.duration = 500
            animate.fillAfter = true
            view.startAnimation(animate)
        }

        fun setUnRevealActivity(savedInstanceState: Bundle?, root_layout: View, revealX: Int, revealY: Int, activity: Activity) {
            if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                root_layout.setVisibility(View.INVISIBLE)


                val viewTreeObserver = root_layout.getViewTreeObserver()
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            revealActivity(revealX, revealY, root_layout, activity)
                            root_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                        }
                    })
                }
            } else {
                root_layout.setVisibility(View.VISIBLE);
            }
        }


        protected fun revealActivity(x: Int, y: Int, root_layout: View, activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val finalRadius = (Math.max(root_layout.getWidth(), root_layout.getHeight()) * 1.1) as Double
                val radius = finalRadius.toFloat()
                // create the animator for this view (the start radius is zero)
                val circularReveal = ViewAnimationUtils.createCircularReveal(root_layout, x, y, 0f, radius)
                circularReveal.duration = 500
                circularReveal.interpolator = AccelerateInterpolator()
                root_layout.setVisibility(View.VISIBLE)
                circularReveal.start()
            } else {
                activity.finish()
            }
        }


        fun unRevealActivity(activity: Activity, root_layout: View, revealX: Int, revealY: Int) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                activity.finish()
            } else {
                val finalRadius = (Math.max(root_layout.getWidth(), root_layout.getHeight()) * 1.1) as Double
                val radius = finalRadius.toFloat()
                val circularReveal = ViewAnimationUtils.createCircularReveal(
                        root_layout, revealX, revealY, radius, 0f)

                circularReveal.duration = 800
                circularReveal.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        root_layout.setVisibility(View.INVISIBLE)
                        activity.finish()
                    }
                })
                circularReveal.start()
            }
        }


        fun slideUp(view: View,fullHeight:Int) {
           /* view.setVisibility(View.VISIBLE)
            val animate = TranslateAnimation(
                    0f, // fromXDelta
                    0f, // toXDelta
                    view.getHeight().toFloat(), // fromYDelta
                    0f)                // toYDelta
            animate.duration = 500
            animate.fillAfter = true
            view.startAnimation(animate)*/

            val anim = ValueAnimator.ofInt(view.getMeasuredHeight(), fullHeight)
            anim.addUpdateListener { valueAnimator ->
                val height = valueAnimator.animatedValue as Int
                val params = view.getLayoutParams() as MarginLayoutParams
                params.bottomMargin = 0
                params.height = height
                view.setLayoutParams(params)
            }
            anim.duration = 700
            anim.start()
        }


        fun slideDown(view: View) {

            val anim = ValueAnimator.ofInt(view.getMeasuredHeight(), getHeightInDp(130,view.context))
            anim.addUpdateListener { valueAnimator ->
                val height = valueAnimator.animatedValue as Int
                val params = view.getLayoutParams() as MarginLayoutParams
                params.bottomMargin = getHeightInDp(100,view.context)
                params.height = height
                view.setLayoutParams(params)
            }
            anim.duration = 700
            anim.start()
        }


        fun getHeightInDp( dps:Int,context: Context):Int
        {
            val scale = context.getResources().getDisplayMetrics().density
            val pixels = (dps * scale + 0.5f)
            return  pixels.toInt()
        }
    }
    }
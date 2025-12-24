package com.helow.runner4.run.ui.list

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(context: Context, recyclerView: RecyclerView, private val mListener: OnItemClickListener) :
    RecyclerView.OnItemTouchListener {
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int) {}
        fun onLongItemClick(view: View, position: Int) {}
    }

    private var mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent) = true

            override fun onLongPress(e: MotionEvent) {
                val child: View? = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null)
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
            }
        })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView: View? = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
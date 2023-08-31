package com.e510.commons.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class E510ViewPager extends ViewPager {

    private boolean swipeLocked;

    public E510ViewPager(Context context) {
        super(context);
    }

    public E510ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean getSwipeLocked() {
        return swipeLocked;
    }

    public void setSwipeLocked(boolean swipeLocked) {
        this.swipeLocked = swipeLocked;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !swipeLocked && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !swipeLocked && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return !swipeLocked && super.canScrollHorizontally(direction);
    }

    @Override
    public void setCurrentItem(int item) {
        if (swipeLocked) {
            super.setCurrentItem(item, false);
        } else {
            super.setCurrentItem(item);
        }
    }
}
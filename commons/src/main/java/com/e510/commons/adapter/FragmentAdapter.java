package com.e510.commons.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.e510.commons.fragment.BaseFragment;

import java.util.ArrayList;

public class FragmentAdapter extends FragmentStatePagerAdapter {

    Context context;
    ArrayList<String> titles;
    ArrayList<BaseFragment> fragments;
    private int[] imageResId;

    private int imagesWidth;
    private int imagesHeight;


    public FragmentAdapter(FragmentManager fm, Context context, ArrayList<BaseFragment> fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }

    public FragmentAdapter(FragmentManager fm, Context context, ArrayList<String> titles, ArrayList<BaseFragment> fragments) {
        super(fm);
        this.context = context;
        this.titles = titles;
        this.fragments = fragments;
    }


    public FragmentAdapter(FragmentManager fm, Context context, int[] images, ArrayList<BaseFragment> fragments) {
        super(fm);
        this.context = context;
        this.imageResId = images;
        this.fragments = fragments;
    }

    public FragmentAdapter(FragmentManager fm, Context context, int[] images, int imagesWidth, int imagesHeight, ArrayList<BaseFragment> fragments) {
        super(fm);
        this.context = context;
        this.imageResId = images;
        this.imagesWidth = imagesWidth;
        this.imagesHeight = imagesHeight;
        this.fragments = fragments;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (titles != null) {
            return titles.get(position);
        }

        if (imageResId != null) {
            Drawable image = context.getResources().getDrawable(imageResId[position]);
            int width = imagesWidth > 0 ? imagesWidth : image.getIntrinsicWidth();
            int height = imagesHeight > 0 ? imagesHeight : image.getIntrinsicHeight();
            image.setBounds(0, 0, width, height);
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }

        return "";
    }


    @Override
    public int getCount() {
        return fragments.size();
    }


    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }
}
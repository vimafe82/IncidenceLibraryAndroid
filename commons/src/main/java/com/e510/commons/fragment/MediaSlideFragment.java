package com.e510.commons.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.e510.commons.R;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;
import com.e510.commons.view.TouchImageView;
import com.e510.commons.view.video.E510VideoView;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class MediaSlideFragment extends BaseFragment {
    private static final String TAG = makeLogTag(MediaSlideFragment.class);

    private CirclePageIndicator circlePageIndicator;
    private ViewPager viewPager;
    private SliderPagerAdapter adapter;

    public static final String KEY_MEDIAS = "KEY_MEDIAS";
    private ArrayList<String> medias;

    public static final String KEY_POSITION = "KEY_POSITION";
    private int selectedPosition;

    public static MediaSlideFragment newInstance(ArrayList<String> medias, int selectedPosition){
        MediaSlideFragment fragment = new MediaSlideFragment();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_MEDIAS, medias);
        bundle.putInt(KEY_POSITION, selectedPosition);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            medias = getArguments().getStringArrayList(KEY_MEDIAS);
            selectedPosition = getArguments().getInt(KEY_POSITION);
        }

        if (medias == null)
        {
            medias = new ArrayList<>();
        }
    }

    @Override
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_media_slide, container, false);
        setupUI(view);

        return  view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        RelativeLayout layoutRightClose = rootView.findViewById(R.id.layoutRightClose);
        ImageView imgRightClose = rootView.findViewById(R.id.imgRightClose);
        DrawableCompat.setTint(imgRightClose.getDrawable(), Color.WHITE);
        layoutRightClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeThis();
            }
        });


        circlePageIndicator = rootView.findViewById(R.id.circlePageIndicator);
        viewPager = rootView.findViewById(R.id.view_pager);

        int colorPrimary = Color.parseColor("#"+ AppConfiguration.getInstance().appearance.colors.primary);
        circlePageIndicator.setFillColor(colorPrimary);

        viewPager.setPageMargin(Utils.dpToPx(10));
        adapter = new SliderPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        circlePageIndicator.setViewPager(viewPager);

        if (selectedPosition > 0 && medias.size() > selectedPosition)
        {
            viewPager.postDelayed(new Runnable() {

                @Override
                public void run() {
                    viewPager.setCurrentItem(selectedPosition);
                }
            }, 10);
        }
    }

    public void loadImageUrl(String url, TouchImageView imageView)
    {
    }

    public boolean isVideo(String url)
    {
        boolean res = false;

        if (url != null)
        {
            if (url.endsWith(".mp4") || url.endsWith(".mov") || url.endsWith(".avi") || url.endsWith(".mkv"))
            {
                res = true;
            }
        }

        return res;
    }

    class SliderPagerAdapter extends PagerAdapter {

        public SliderPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.media_slide, container, false);
            E510VideoView videoView  = view.findViewById(R.id.videoView);
            TouchImageView imgView = view.findViewById(R.id.imgView);


            String mediaUrl = medias.get(position);

            if (isVideo(mediaUrl))
            {
                videoView.setVisibility(View.VISIBLE);
                imgView.setVisibility(View.VISIBLE);
                videoView.setVideoURL(mediaUrl);
            }
            else
            {
                videoView.setVisibility(View.GONE);
                imgView.setVisibility(View.VISIBLE);

                loadImageUrl(mediaUrl, imgView);
            }

            container.addView(view);

            return view;
        }

        @Override
        public int getCount()
        {
            return medias.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}

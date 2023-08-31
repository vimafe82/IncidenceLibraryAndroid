package es.incidence.core.fragment.welcome;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;

import es.incidence.core.Constants;
import es.incidence.core.R;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.fragment.sign.SignHomeFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.ICirclePageIndicator;

public class WelcomeFragment extends IFragment
{
    private static final String TAG = makeLogTag(WelcomeFragment.class);

    private ICirclePageIndicator circlePageIndicator;
    private ViewPager viewPager;
    private SliderPagerAdapter adapter;
    private RelativeLayout layoutVideo;
    private VideoView videoView;
    private ImageView videoMuteOn;
    private ImageView videoMuteOff;
    private RelativeLayout layoutVideoBackground;

    public static WelcomeFragment newInstance()
    {
        WelcomeFragment fragment = new WelcomeFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootWelcome;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView)
    {
        super.setupUI(rootView);

        getBaseActivity().hideToolbar();

        RelativeLayout layoutVideoClose = rootView.findViewById(R.id.layoutVideoClose);
        layoutVideoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null)
                {
                    videoView.stopPlayback();
                    showInitial();
                }
            }
        });
        rootView.findViewById(R.id.layoutAlpha).setVisibility(View.GONE);
        ContentLoadingProgressBar myProgress = (ContentLoadingProgressBar) rootView.findViewById(R.id.progress);
        myProgress.setIndeterminateTintList(ColorStateList.valueOf(Utils.getColor(getContext(), R.color.white)));

        layoutVideo = rootView.findViewById(R.id.layoutVideo);
        videoView = rootView.findViewById(R.id.videoView);
        circlePageIndicator = rootView.findViewById(R.id.circlePageIndicator);
        viewPager = rootView.findViewById(R.id.view_pager);
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

        videoMuteOn = rootView.findViewById(R.id.videoMuteOn);
        videoMuteOff = rootView.findViewById(R.id.videoMuteOff);
        layoutVideoBackground = rootView.findViewById(R.id.layoutVideoBackground);
    }

    @Override
    public void loadData()
    {
        //layoutVideo.setVisibility(View.GONE);
        layoutVideo.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
        circlePageIndicator.setVisibility(View.GONE);

        showHud();
        Api.getHomeVideo(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {

                String urlVideo = null;

                if (response.isSuccess())
                {
                    //ArrayList<TutorialVideo> temp = response.getList("data", TutorialVideo.class);
                    String vid = response.get("video");
                    if (vid != null)
                    {
                        urlVideo = vid;
                    }
                }

                if (urlVideo != null)
                {
                    //layoutVideo.setVisibility(View.VISIBLE);
                    Context context = getContext();
                    if (context != null) {
                        MediaController mediaController = new MediaController(context);
                        mediaController.setVisibility(View.GONE);
                        mediaController.setAnchorView(videoView);

                        Uri vidUri = Uri.parse(urlVideo);
                        videoView.setVideoURI(vidUri);
                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                showInitial();
                                return false;
                            }
                        });
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                showInitial();
                            }
                        });
                        videoView.setMediaController(mediaController);
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                hideHud();
                                updateVideoControls(mp);
                                videoView.requestFocus();
                                videoView.start();
                            }
                        });
                    }
                }
                else
                {
                    hideHud();
                    getBaseActivity().showToolbar();
                    viewPager.setVisibility(View.VISIBLE);
                    circlePageIndicator.setVisibility(View.VISIBLE);
                    layoutVideo.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateVideoControls(MediaPlayer mp) {
        layoutVideoBackground.setVisibility(View.GONE);
        videoMuteOn.setVisibility(View.VISIBLE);
        videoMuteOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoMuteOn.setVisibility(View.GONE);
                videoMuteOff.setVisibility(View.VISIBLE);

                mp.setVolume(0F, 0F);
            }
        });
        videoMuteOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoMuteOff.setVisibility(View.GONE);
                videoMuteOn.setVisibility(View.VISIBLE);

                mp.setVolume(1F, 1F);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        try
        {
            menu.clear();
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_welcome, menu);
            MenuItem closeMenuItem = menu.findItem(R.id.menuWelcomeClose);
            DrawableCompat.setTint(closeMenuItem.getIcon(), Utils.getColor(getContext(), android.R.color.white));
        }
        catch (Exception e)
        {
            LogUtil.logE(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.menuWelcomeClose)
        {
            showInitial();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showInitial()
    {
        mListener.showInitialFragment(R.id.layout_activity_main, SignHomeFragment.newInstance(0, null));
        getBaseActivity().setToolbarColor(Utils.getColor(getContext(), R.color.incidence100), Utils.getColor(getContext(), R.color.black700), true);
    }

    private class SliderPagerAdapter extends PagerAdapter {

        public SliderPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.slide_welcome, container, false);
            ImageView imgView = view.findViewById(R.id.imgView);
            TextView txtTitle = view.findViewById(R.id.txtSlideTitle);
            TextView txtSubtitle = view.findViewById(R.id.txtSlideSubtitle);
            IButton btn = view.findViewById(R.id.btn);

            FontUtils.setTypeValueText(txtTitle, Constants.FONT_SEMIBOLD, getContext());
            FontUtils.setTypeValueText(txtSubtitle, Constants.FONT_REGULAR, getContext());
            FontUtils.setTypeValueText(btn, Constants.FONT_SEMIBOLD, getContext());

            imgView.setImageDrawable(Utils.getDrawable(getContext(), getImage(position)));
            txtTitle.setText(getTitle(position));
            txtSubtitle.setText(getSubtitle(position));
            btn.setText(getButtonText(position));

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (position == getCount() - 1)
                    {
                        showInitial();
                    }
                    else
                    {
                        viewPager.setCurrentItem(position+1);
                    }
                }
            });

            container.addView(view);

            return view;
        }

        @Override
        public int getCount()
        {
            return 3;
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

        private Integer getImage(int position)
        {
            Integer res = null;

            if (position == 0)
            {
                res = R.drawable.foto1;
            }
            else if (position == 1)
            {
                res = R.drawable.foto2;
            }
            else if (position == 2)
            {
                res = R.drawable.foto3;
            }

            return res;
        }

        private String getTitle(int position)
        {
            String res = "";

            if (position == 0)
            {
                res = getString(R.string.welcome_slide_1_title);
            }
            else if (position == 1)
            {
                res = getString(R.string.welcome_slide_2_title);
            }
            else if (position == 2)
            {
                res = getString(R.string.welcome_slide_3_title);
            }

            return res;
        }

        private String getSubtitle(int position)
        {
            String res = "";

            if (position == 0)
            {
                res = getString(R.string.welcome_slide_1_subtitle);
            }
            else if (position == 1)
            {
                res = getString(R.string.welcome_slide_2_subtitle);
            }
            else if (position == 2)
            {
                res = getString(R.string.welcome_slide_3_subtitle);
            }

            return res;
        }
        private String getButtonText(int position)
        {
            String res = "";

            if (position == 0)
            {
                res = getString(R.string.next);
            }
            else if (position == 1)
            {
                res = getString(R.string.next);
            }
            else if (position == 2)
            {
                res = getString(R.string.create_account);
            }

            return res;
        }
    }
}

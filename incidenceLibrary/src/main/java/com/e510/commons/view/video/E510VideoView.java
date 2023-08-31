package com.e510.commons.view.video;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.widget.ContentLoadingProgressBar;

import com.e510.incidencelibrary.R;
import com.e510.commons.utils.DateUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;

public class E510VideoView extends RelativeLayout
{
    private ContentLoadingProgressBar progress;
    private VideoView video;
    private RelativeLayout layoutControls;
    private TextView txtLeft;
    private TextView txtRight;
    private Button btnPlay;
    private Button btnFullScreen;

    private Context context;
    private String url;
    private MediaPlayer mediaPlayer;

    private Handler handlerHideControls;

    public E510VideoView(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public E510VideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public E510VideoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr)
    {
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_video_view, this, true);

        progress = view.findViewById(R.id.progress);
        video = view.findViewById(R.id.video);
        layoutControls = view.findViewById(R.id.layoutControls);
        layoutControls.setVisibility(View.GONE);
        txtLeft = view.findViewById(R.id.txtLeft);
        txtRight = view.findViewById(R.id.txtRight);
        btnPlay = view.findViewById(R.id.btnPlay);
        btnFullScreen = view.findViewById(R.id.btnFullScreen);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutControls.getVisibility() == View.VISIBLE)
                {
                    layoutControls.setVisibility(View.GONE);
                }
                else
                {
                    if (handlerHideControls != null)
                    {
                        handlerHideControls.removeCallbacksAndMessages(null);
                    }

                    layoutControls.setVisibility(View.VISIBLE);

                    if (video.isPlaying()) {
                        hideControlsDelayed();
                    }
                }
            }
        });

        btnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (video.isPlaying())
                {
                    video.pause();
                    btnPlay.setBackground(Utils.getDrawable(context, android.R.drawable.ic_media_play));
                }
                else
                {
                    video.start();
                    btnPlay.setBackground(Utils.getDrawable(context, android.R.drawable.ic_media_pause));
                    checkCurrent();
                    hideControlsDelayed();
                }
            }
        });

        btnFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null && url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "video/*");
                    context.startActivity(intent);
                }
            }
        });

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mediaPlayer = mp;

                progress.setVisibility(View.GONE);
                layoutControls.setVisibility(View.VISIBLE);

                if (mediaPlayer != null)
                {
                    txtLeft.setText(DateUtils.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                    txtRight.setText(DateUtils.milliSecondsToTimer(mediaPlayer.getDuration()));
                }
            }
        });

    }

    public void setVideoURL(String url)
    {
        this.url = url;
        /*
        try {
            MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
            Bitmap bmp = null;
            retriever.setDataSource(url, new HashMap<String, String>());
            bmp = retriever.getFrameAtTime();
            mVideoWidth = bmp.getWidth();
            mVideoHeight = bmp.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        video.setVideoURI(Uri.parse(url));
        video.seekTo(1);
    }

    private void checkCurrent()
    {
        try
        {
            if (txtLeft != null && mediaPlayer != null && video != null)
            {
                txtLeft.setText(DateUtils.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));

                if (video.isPlaying())
                {
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            checkCurrent();
                        }
                    }, 1000);
                }
                else if (btnPlay != null && context != null)
                {
                    video.stopPlayback();
                    video.setVideoURI(Uri.parse(url));
                    btnPlay.setBackground(Utils.getDrawable(context, android.R.drawable.ic_media_play));
                }
            }
        }
        catch (Exception e)
        {
            LogUtil.logE("videoView", e.getMessage());
        }
    }

    private void hideControlsDelayed()
    {
        try
        {
            handlerHideControls = new Handler();
            handlerHideControls.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (layoutControls != null)
                    {
                        layoutControls.setVisibility(View.GONE);
                    }
                }
            }, 3000);
        }
        catch (Exception e)
        {
            LogUtil.logE("videoView", e.getMessage());
        }
    }
}
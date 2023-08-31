package es.incidence.core.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.e510.commons.activity.BaseActivity;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import es.incidence.core.R;
import es.incidence.core.utils.view.INavigation;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class FullscreenYoutubeActivity extends BaseActivity {

    private YouTubePlayer youTubePlayer;

    private boolean isFullscreen = false;

    private INavigation navigation;

    private String title;
    private String code;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_example);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFullscreen) {
                    // if the player is in fullscreen, exit fullscreen
                    youTubePlayer.toggleFullscreen();
                } else {
                    finish();
                }
            }
        });

        Bundle b = getIntent().getExtras();
        code = b.getString("code");
        title = b.getString("title");

        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        FrameLayout fullscreenViewContainer = findViewById(R.id.full_screen_view_container);

        // we need to initialize manually in order to pass IFramePlayerOptions to the player
        youTubePlayerView.enableBackgroundPlayback(false);

        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(1)
                .autoplay(1)
                .fullscreen(1)
                .build();

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                FullscreenYoutubeActivity.this.youTubePlayer = youTubePlayer;

                String videoId = code;
                youTubePlayer.loadVideo(videoId, 0);

                youTubePlayer.addListener(new YouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    }

                    @Override
                    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState playerState) {
                        Log.e("YouTubePlayer", "onStateChange: " + playerState.toString());

                        if (PlayerConstants.PlayerState.ENDED == playerState) {
                            youTubePlayer.cueVideo(code, 0);
                        }
                    }

                    @Override
                    public void onPlaybackQualityChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackQuality playbackQuality) {

                    }

                    @Override
                    public void onPlaybackRateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackRate playbackRate) {

                    }

                    @Override
                    public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError playerError) {
                        Log.e("YouTubePlayer", "ERROR: " + playerError.toString());
                    }

                    @Override
                    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float v) {

                    }

                    @Override
                    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float v) {

                    }

                    @Override
                    public void onVideoLoadedFraction(@NonNull YouTubePlayer youTubePlayer, float v) {

                    }

                    @Override
                    public void onVideoId(@NonNull YouTubePlayer youTubePlayer, @NonNull String s) {

                    }

                    @Override
                    public void onApiChange(@NonNull YouTubePlayer youTubePlayer) {

                    }
                });
            }

        }, iFramePlayerOptions);

        youTubePlayerView.addFullscreenListener(new FullscreenListener() {
            @Override
            public void onEnterFullscreen(@NonNull View fullscreenView, @NonNull Function0<Unit> function0) {
                isFullscreen = true;

                // the video will continue playing in fullscreenView
                youTubePlayerView.setVisibility(View.GONE);
                fullscreenViewContainer.setVisibility(View.VISIBLE);
                fullscreenViewContainer.addView(fullscreenView);

                // optionally request landscape orientation
                // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }

            @Override
            public void onExitFullscreen() {
                isFullscreen = false;

                // the video will continue playing in the player
                youTubePlayerView.setVisibility(View.VISIBLE);
                fullscreenViewContainer.setVisibility(View.GONE);
                fullscreenViewContainer.removeAllViews();
            }
        });

        navigation = findViewById(R.id.inavigation);
        //navigation.init(this, title, true);
        //navigation.init(YoutubeActivity.this)
        navigation.setTitle(title);
        navigation.setBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //onBackPressedDispatcher.addCallback(onBackPressedCallback)

        getLifecycle().addObserver(youTubePlayerView);
    }
}

package com.jwplayer.opensourcedemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.cast.CastManager;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.ads.Ad;
import com.longtailvideo.jwplayer.media.ads.AdBreak;
import com.longtailvideo.jwplayer.media.ads.AdSource;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class ExtraCredit extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener, View.OnClickListener {

    private JWPlayerView mPlayerView;
    private Button loadBtn, playBtn;
    private TextView outputTextView;
    private ImageView mImage;
    private EditText inputURL;
    private List<PlaylistItem> pi = new ArrayList<>();
    private String videoURL,imageURL;

    /**
     * Reference to the {@link CastManager}
     */
    private CastManager mCastManager;

    /**
     * Stored instance of CoordinatorLayout
     * http://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.html
     */
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayerview);

        initViews();
        mPlayerView.addOnFullscreenListener(this);
        new KeepScreenOnHandler(mPlayerView, getWindow());
        new JWEventHandler(mPlayerView, outputTextView, mImage);
        mCastManager = CastManager.getInstance();
    }

    private void initViews() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_jwplayerview);
        mPlayerView = (JWPlayerView)findViewById(R.id.jwplayer);
        outputTextView = (TextView)findViewById(R.id.output);
        mImage = (ImageView)findViewById(R.id.image);
        inputURL = (EditText) findViewById(R.id.input);
        loadBtn = (Button)findViewById(R.id.loadBtn);
        playBtn = (Button)findViewById(R.id.playBtn);
        videoURL = "";
        imageURL = "";

        loadBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Set fullscreen when the device is rotated to landscape
        mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        super.onResume();
        mPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFullscreen(boolean fullscreen) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (fullscreen) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        }

        // When going to Fullscreen we want to set fitsSystemWindows="false"
        mCoordinatorLayout.setFitsSystemWindows(!fullscreen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_jwplayerview, menu);
        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.loadBtn:
                videoURL = inputURL.getText().toString();
                inputURL.setText("");
                pi.add(addVideo(videoURL));
                break;
            case R.id.playBtn:
                if(pi.size()==0) pi.add(addVideo(videoURL));
                inputURL.setText("");
                startToPlay();
                break;
        }
    }

    private PlaylistItem addVideo(String video) {

        if(video == null || video.equals("")) {
            videoURL = makeURL("EasyVideo.mp4");
            imageURL = makeURL("EasyImage.jpg");
        } else {
            videoURL = makeURL("MediumVideo.mp4");
            imageURL = makeURL("MediumImage.jpg");
        }

        return new PlaylistItem.Builder()
                .file(videoURL)
                .image(imageURL)
                .title("JW Player")
                .description("A video player testing video.")
                .build();
    }

    private static String makeURL(String img) {
        String urlString = "https://s3.amazonaws.com/bob.jwplayer.com/~test/assets/";
        return urlString+img ;
    }

    private void startToPlay() {

        // Create advertising schedule
        List<AdBreak> adSchedule = new ArrayList<AdBreak>();

        Ad ad = new Ad(AdSource.VAST, "https://playertest.longtailvideo.com/vast-30s-ad.xml");
        AdBreak adBreak = new AdBreak("pre", ad);

        adSchedule.add(adBreak);

        // Create video
        PlaylistItem video = new PlaylistItem(makeURL(null));
        // Set advertising schedule to your video
        video.setAdSchedule(adSchedule);


        // Create second video
        PlaylistItem video2 = new PlaylistItem("http://playertest.longtailvideo.com/jwpromo/jwpromo.m3u8");

        // Create different advertising schedule
        List<AdBreak> adSchedule2 = new ArrayList<AdBreak>();

        Ad ad2 = new Ad(AdSource.VAST, "https://playertest.longtailvideo.com/vast-30s-ad.xml");
        AdBreak adBreak2 = new AdBreak("10", ad2);

        adSchedule2.add(adBreak2);

        // Set advertising schedule to your video
        video2.setAdSchedule(adSchedule2);

        List<PlaylistItem> playlist = new ArrayList<PlaylistItem>();
        playlist.add(video);
        playlist.add(video2);

        // Create your player config
        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .playlist(playlist)
                .build();

        // Setup your player with the config
        mPlayerView.setup(playerConfig);
        mPlayerView.load(playlist);
    }

}

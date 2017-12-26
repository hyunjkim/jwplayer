package com.jwplayer.opensourcedemo;

import android.app.DialogFragment;
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

import com.jwplayer.opensourcedemo.dialog.DisplayMessage;
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

public class ExtraCredit extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener, View.OnClickListener{

    private JWPlayerView mPlayerView;
    private Button loadBtn, playBtn;
    private TextView outputTextView;
    private ImageView mImage;
    private EditText inputURL;
    private List<PlaylistItem> pi = new ArrayList<>();
    private String videoURL,imageURL;
    private boolean isAd = true
    private DialogFragment newFragment;

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
        new JWEventHandler(mPlayerView, outputTextView, mImage, dialogListener);
        mCastManager = CastManager.getInstance();
    }

    /*
    * INITIALIZE VIEWS FOR USERS
    */
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

        // VIDEO SHOWED ERROR, SO CODE WAS ADDED HERE
        pi.add(addVideo(videoURL));
    }
    public void startDialog(){
        DialogFragment newFragment = new DisplayMessage();
        newFragment.show(getFragmentManager(),"hello");
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

    /*
    * USER EITHER LOADS A VIDEO OR PLAYS THE BUTTON AND LOADS THE DEFAULT VIDEO
    */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.loadBtn:
                videoURL = inputURL.getText().toString();
                inputURL.setText("");
                pi.add(addVideo(videoURL));
                break;
            case R.id.playBtn:
                if(pi.size()== 0) pi.add(addVideo(videoURL));
                inputURL.setText("");
                startToPlay();
                break;
        }
    }

    private PlaylistItem addVideo(String video) {
        // Create advertising schedule
        List<AdBreak> adSchedule = new ArrayList<AdBreak>();
        PlaylistItem playlist;
        /*
        * EITHER USER PROVIDES A VIDEO THEY WANT TO WATCH OR BY DEFAULT WE SHOW A MEDIUMVIDEO
        */
        if(video == null || video.equals("")) {
            videoURL = makeURL("EasyVideo.mp4");
            imageURL = makeURL("EasyImage.jpg");
        } else {
            videoURL = makeURL("MediumVideo.mp4");
            imageURL = makeURL("MediumImage.jpg");
        }
        /*
        * ALL ADVERTISEMENTS ARE ADDED HERE
        */
        adSchedule.add(advertisement(isAd));
        isAd = !isAd;
        playlist = new PlaylistItem.Builder()
                .file(videoURL)
                .image(imageURL)
                .title("JW Player")
                .description("A video player testing video.")
                .build();

        // Set advertising schedule to your video
        playlist.setAdSchedule(adSchedule);

        return playlist;
    }

    /*
    * URL had the same beginning, here is a helper function
    */
    private static String makeURL(String img) {
        String urlString = "https://s3.amazonaws.com/bob.jwplayer.com/~test/assets/";
        return urlString+img ;
    }

    /*
    * ADVERTISEMENT HELPER FUNCTION
    */
    private AdBreak advertisement(boolean isAd){
        Ad ad = new Ad(AdSource.VAST, "https://playertest.longtailvideo.com/vast-30s-ad.xml");
        return isAd? new AdBreak("pre", ad):new AdBreak("10", ad);
    }
    /*
    * WHEN USER CLICKS PLAY, WE BEGIN TO LOAD THE PLAYLIST
    */
    private void startToPlay() {

        // Create your player config
        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .playlist(pi)
                .build();

        // Setup your player with the config
        mPlayerView.setup(playerConfig);
        mPlayerView.load(pi);
    }

}

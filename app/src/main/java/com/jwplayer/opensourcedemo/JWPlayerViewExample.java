package com.jwplayer.opensourcedemo;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jwplayer.opensourcedemo.dialog.DisplayMessage;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.cast.CastManager;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class JWPlayerViewExample extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener, View.OnClickListener {

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

		//initiate the views & button click listeners
		initViews();

		// Handle hiding/showing of ActionBar
		mPlayerView.addOnFullscreenListener(this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayerView, getWindow());

		// Instantiate the JW Player event handler class
		/*
          An instance of our event handling class
         */

        new JWEventHandler(mPlayerView, outputTextView, mImage);

		// Get a reference to the CastManager
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

	/**
	 * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
	 *
	 * @param fullscreen true if the player is fullscreen
	 */
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
		// Register the MediaRouterButton on the JW Player SDK
		mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.switch_to_fragment:
				Intent i = new Intent(this, JWPlayerFragmentExample.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
		/*
		  Reference to the {@link JWPlayerView}
		 */
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
        mPlayerView.load(pi);
    }

}

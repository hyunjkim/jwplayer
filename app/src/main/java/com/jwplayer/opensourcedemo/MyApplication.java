package com.jwplayer.opensourcedemo;

import android.app.Application;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.cast.CastManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the CastManager.
        // The CastManager must be initialized in the Application's context to prevent
        // issues with garbage collection.

        /**
         * Note:
         As an alternative to storing the license key in your AndroidManifest.xml
         you can set the it programmatically by calling JWPlayerView.setLicenseKey(Context, String)
         before the JWPlayerView is instantiated.

         Valid license editions include Ads, Enterprise and Trial.
         The player will throw an OnSetupError and display an Invalid License Key message on the screen
         if an invalid license key is provided.
         */

        JWPlayerView.setLicenseKey(this,getString(R.string.jw_key));
        CastManager.initialize(this);
    }
}

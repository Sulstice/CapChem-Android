package com.example.zola.capchem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseUser;

/**
 * Created by rufflez on 7/8/14.
 */
public class DispatchActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState){
        Parse.initialize(this, "JF95yzvMDH3APNmwgPdeY9wrIxbNtCqprK4C5PjY", "ZrwcJA8QIIr8L1lVWfcqsvft1THdFk33fEiZcUV3");
        super.onCreate(savedInstanceState);
        // Check if there is current user info
        if (ParseUser.getCurrentUser() != null) {
            // Start an intent for the logged in activity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Start and intent for the logged out activity
            startActivity(new Intent(this, SignUpOrLoginActivity.class));
        }
    }
}
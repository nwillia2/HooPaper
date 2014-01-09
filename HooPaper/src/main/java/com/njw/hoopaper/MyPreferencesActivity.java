package com.njw.hoopaper;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by nwillia2 on 05/01/2014.
 */
public class MyPreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wallpaper_settings);
    }
}

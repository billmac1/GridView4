package com.example.billmac1.gridview4;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by billmac1
 * */
public class MyPreferenceFragment extends PreferenceFragment {


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
    }

}

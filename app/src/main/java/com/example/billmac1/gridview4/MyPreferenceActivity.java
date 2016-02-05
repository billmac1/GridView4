package com.example.billmac1.gridview4;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by billmac1
 */
public class MyPreferenceActivity extends PreferenceActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

}

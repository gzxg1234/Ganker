package com.sanron.ganker.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sanron.ganker.R;

public class MainActivity extends AppCompatActivity {


    MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getName());
        if (mMainFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, MainFragment.newInstance(), MainFragment.class.getName())
                    .commit();
        }

    }
}

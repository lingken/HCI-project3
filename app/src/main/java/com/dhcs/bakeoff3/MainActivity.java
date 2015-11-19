package com.dhcs.bakeoff3;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
//        AssetManager assetManager = getApplicationContext().getAssets();
//        Fragment fragment = new TypeByPage(assetManager);

        Fragment fragment = new DragWithAnchor();
//        Fragment fragment = new Test();
//        Fragment fragment = new ZoomKeyboard(assetManager);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }
}

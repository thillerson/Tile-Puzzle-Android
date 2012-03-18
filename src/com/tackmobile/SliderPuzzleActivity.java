package com.tackmobile;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.SurfaceView;

public class SliderPuzzleActivity extends RoboActivity {
	
	@InjectView(R.id.surface) SurfaceView surface;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
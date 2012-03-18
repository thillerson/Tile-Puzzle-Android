package com.tackmobile;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.os.Bundle;

public class SliderPuzzleActivity extends RoboActivity {
	
	@InjectView(R.id.gameboard) GameboardView gameboard;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
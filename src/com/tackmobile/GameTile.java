package com.tackmobile;

import com.tackmobile.GameboardView.Coordinate;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

public class GameTile extends ShapeDrawable {
	
	public Coordinate coordinate;

	public GameTile(Coordinate coordinate) {
		super( new RoundRectShape( new float[]{4,4,4,4,4,4,4,4}, null, null ) );
		getPaint().setColor(Color.RED);
		this.coordinate = coordinate;
	}

}

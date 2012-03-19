package com.tackmobile;

import com.tackmobile.GameboardView.Coordinate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;

public class GameTile extends View {
	
	protected Drawable drawable;
	public Coordinate coordinate;

	public GameTile(Context context, Coordinate coordinate) {
		super(context);
		this.coordinate = coordinate;
		ShapeDrawable sd = new ShapeDrawable( new RoundRectShape( new float[]{4,4,4,4,4,4,4,4}, null, null ) );
		sd.getPaint().setColor(Color.RED);
		drawable = sd;
		setBackgroundDrawable(drawable);
	}
	
	@Override
	public String toString() {
		return String.format("<GameTile at row: %d, col: %d", coordinate.row, coordinate.column);
	}

}

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
	protected boolean empty; 

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
		return String.format("<GameTile at row: %d, col: %d, x: %f, y: %f", coordinate.row, coordinate.column, getX(), getY());
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
		if (empty) {
			setBackgroundDrawable(null);
			setAlpha(0);
		}
	}

	public boolean isInRowOrColumnOf(GameTile otherTile) {
		return (coordinate.sharesAxisWith(otherTile.coordinate));
	}

}

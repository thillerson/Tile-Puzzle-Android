package com.tackmobile;

import com.tackmobile.GameboardView.Coordinate;

import android.content.Context;
import android.widget.ImageView;

public class GameTile extends ImageView {
	
	public Coordinate coordinate;
	protected boolean empty; 

	public GameTile(Context context, Coordinate coordinate) {
		super(context);
		this.coordinate = coordinate;
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

	public boolean isToRightOf(GameTile tile) {
		return coordinate.isToRightOf(tile.coordinate);
	}

	public boolean isToLeftOf(GameTile tile) {
		return coordinate.isToLeftOf(tile.coordinate);
	}

	public boolean isAbove(GameTile tile) {
		return coordinate.isAbove(tile.coordinate);
	}

	public boolean isBelow(GameTile tile) {
		return coordinate.isBelow(tile.coordinate);
	}

}

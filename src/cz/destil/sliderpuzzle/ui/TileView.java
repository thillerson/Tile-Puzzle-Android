package cz.destil.sliderpuzzle.ui;

import android.content.Context;
import android.os.Build;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cz.destil.sliderpuzzle.data.Coordinate;

/**
 * 
 * ImageView displaying tile of the puzzle. Contains useful functions for
 * comparing with other tiles.
 * 
 * Based on:
 * https://github.com/thillerson/Android-Slider-Puzzle/blob/master/src/
 * com/tackmobile/GameTile.java
 * 
 * @author David Vavra
 */
public class TileView extends ImageView {

	public Coordinate coordinate;
	public int originalIndex;
	public int numberOfDrags;
	private boolean empty;

	public TileView(Context context, int originalIndex) {
		super(context);
		this.originalIndex = originalIndex;
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

	public boolean isInRowOrColumnOf(TileView otherTile) {
		return (coordinate.sharesAxisWith(otherTile.coordinate));
	}

	public boolean isToRightOf(TileView tile) {
		return coordinate.isToRightOf(tile.coordinate);
	}

	public boolean isToLeftOf(TileView tile) {
		return coordinate.isToLeftOf(tile.coordinate);
	}

	public boolean isAbove(TileView tile) {
		return coordinate.isAbove(tile.coordinate);
	}

	public boolean isBelow(TileView tile) {
		return coordinate.isBelow(tile.coordinate);
	}

	/**
	 * Sets X Y coordinate for the view - works on all Android versions.
	 * 
	 * @param x
	 * @param y
	 */
	public void setXY(float x, float y) {
		if (Build.VERSION.SDK_INT >= 11) {
			// native and more precise
			setX(x);
			setY(y);
		} else {
			// emulated on older versions of Android
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
			params.leftMargin = (int) x;
			params.topMargin = (int) y;
			setLayoutParams(params);
		}
	}

	/**
	 * @return get x position for all versions of Android
	 */
	public float getXPos() {
		if (Build.VERSION.SDK_INT >= 11) {
			// native and more precise
			return getX();
		} else {
			// emulated on older versions of Android
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
			return params.leftMargin;
		}
	}

	/**
	 * @return get y position for all versions of Android
	 */
	public float getYPos() {
		if (Build.VERSION.SDK_INT >= 11) {
			// native and more precise
			return getY();
		} else {
			// emulated on older versions of Android
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
			return params.topMargin;
		}
	}

}

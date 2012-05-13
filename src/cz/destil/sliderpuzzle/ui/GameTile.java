package cz.destil.sliderpuzzle.ui;

import cz.destil.sliderpuzzle.data.Coordinate;
import android.content.Context;
import android.widget.ImageView;

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
public class GameTile extends ImageView {

	public Coordinate coordinate;
	private boolean empty;

	public GameTile(Context context, Coordinate coordinate) {
		super(context);
		this.coordinate = coordinate;
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
